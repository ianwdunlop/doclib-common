package io.mdcatapult.doclib.consumer

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.flag.FlagContext
import io.mdcatapult.doclib.messages.SupervisorMsg
import io.mdcatapult.klein.queue.{Envelope, EnvelopeWithId, Sendable}
import io.mdcatapult.doclib.metrics.Metrics.handlerCount
import io.mdcatapult.doclib.models.{ConsumerNameAndQueue, DoclibDoc, DoclibDocExtractor}
import io.mdcatapult.util.concurrency.LimitedExecution
import io.mdcatapult.util.models.Version
import org.bson.types.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class ConsumerHandler[T <: Envelope](implicit config: Config, ec: ExecutionContext) extends LazyLogging {

  val version: Version = Version.fromConfig(config) // used for mongoFlagStore
  val docExtractor: DoclibDocExtractor = DoclibDocExtractor()
  val readLimiter: LimitedExecution

  /**
    * Process a message and return a DoclibDoc. The handle method is passed into a
    * Queue when subscribing to it and acts as a callback for any messages that the
    * queue receives. The key (ie exchange) is extracted from the message within
    * the Queue subscribe method.
    *
    * @param message One of the various Envelopes eg PrefetchMsg, SupervisorMsg
    * @param key     AMQP Exchange
    * @return DoclibDoc
    */
  def handle(message: T, key: String): Future[Option[HandlerReturn]]

  def postHandleProcess[A <: EnvelopeWithId, B <: HandlerReturn]
  (
    message: A,
    handlerReturn: Future[Option[B]],
    supervisorQueueOpt: Option[Sendable[SupervisorMsg]],
    flagContext: FlagContext,
    collectionOpt: Option[MongoCollection[DoclibDoc]],
  )
  (implicit consumerNameAndQueue: ConsumerNameAndQueue)
  : Future[Option[B]] = {

    handlerReturn.andThen {

      case Success(handlerReturnOpt) =>
        handlerReturnOpt match {
          case Some(handlerReturn) =>
            incrementHandlerCount("success")

            // if our handler has a defined supervisor queue, send a message to this queue with the id of the doclibDoc
            if (supervisorQueueOpt.isDefined) {
              supervisorQueueOpt.get.send(SupervisorMsg(id = handlerReturn.doclibDoc._id.toHexString))
            }

            // TODO do we need to log any info about paths? trying to cover edge cases
            if (handlerReturn.pathsOpt.isDefined) {
              logger.info(f"COMPLETE: ${message.id} - Paths ${handlerReturn.pathsOpt.get.length}")
            } else {
              logger.info(f"COMPLETE: ${message.id} - ${handlerReturn.doclibDoc._id}")
            }

          case None =>
            incrementHandlerCount("error_no_document")
            logger.error(s"ERROR: ${message.id} - no document found")
        }

      case Failure(doclibException: DoclibDocException) =>
        incrementHandlerCount("doclib_doc_exception")

        val doclibDoc = doclibException.getDoc
        logger.error(s"ERROR: doclib_doc_exception doclib doc id: ${doclibDoc._id}")

        flagContext.error(doclibDoc, noCheck = true).andThen {
          case Failure(e) =>
            logger.error(s"ERROR: couldn't write error flag for doclib doc id: ${doclibDoc._id}", e)
        }

      case Failure(e) if collectionOpt.isDefined =>
        incrementHandlerCount("unknown_error")
        failureWithCollection(e, collectionOpt.get, message.id, flagContext)

      case Failure(e) =>
        // TODO the leadmine handler tests require a document to write the error flag to at this stage,
        // TODO so maybe wrap in a DoclibDocException to be caught further up
        incrementHandlerCount("unknown_error")
        logger.error("error during handle process", e)
    }
  }

  def failureWithCollection(e: Throwable, collection: MongoCollection[DoclibDoc], messageId: String, flagContext: FlagContext): Unit = {
    logger.error("error during handle process", e)

    findDocById(collection, messageId, readLimiter)
      .onComplete {
        case Success(doclibDocOpt) => doclibDocOpt match {
          case Some(doc) =>
            flagContext.error(doc, noCheck = true)
              .andThen {
                case Failure(e) => logger.error("error attempting error flag write", e)
              }
          case None =>
            val exceptionMessage = s"$messageId - no document found"
            logger.error(exceptionMessage, new Exception(exceptionMessage))
        }
        case Failure(e) => logger.error(s"error retrieving document", e)
      }
  }

  def incrementHandlerCount(labels: String*)(implicit consumerNameAndQueue: ConsumerNameAndQueue): Unit = {
    val labelsWithConsumerInfo = Seq(consumerNameAndQueue.name, consumerNameAndQueue.queue) ++ labels
    handlerCount.labels(labelsWithConsumerInfo: _*).inc()
  }

  def logReceived(messageId: String): Unit = {
    logger.info(f"RECEIVED: $messageId")
  }

  def findDocById(collection: MongoCollection[DoclibDoc],
                  messageId: String,
                  readLimit: LimitedExecution): Future[Option[DoclibDoc]] = {
    readLimit(collection, "fetch document by id") { collection =>
      collection
        .find(equal("_id", new ObjectId(messageId)))
        .first()
        .toFutureOption()
    }
  }
}

// a handler's return type can extend this trait to avoid .asInstanceOf casting in tests
trait HandlerReturn {
  val doclibDoc: DoclibDoc
  val pathsOpt: Option[List[String]]
}

// generic class which covers most handler return types
case class GenericHandlerReturn(doclibDoc: DoclibDoc,
                                pathsOpt: Option[List[String]] = None) extends HandlerReturn
