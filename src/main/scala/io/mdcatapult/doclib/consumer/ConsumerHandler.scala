package io.mdcatapult.doclib.consumer

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.flag.FlagContext
import io.mdcatapult.doclib.messages.SupervisorMsg
import io.mdcatapult.doclib.metrics.Metrics.handlerCount
import io.mdcatapult.doclib.models.{ConsumerNameAndQueue, DoclibDoc, DoclibDocExtractor}
import io.mdcatapult.klein.queue.{Envelope, EnvelopeWithId, Sendable}
import io.mdcatapult.util.concurrency.LimitedExecution
import io.mdcatapult.util.models.Version
import org.bson.types.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


abstract class ConsumerHandler[T <: Envelope](implicit config: Config, ec: ExecutionContext) extends LazyLogging {

  val readLimiter: LimitedExecution
  val writeLimiter: LimitedExecution

  val version: Version = Version.fromConfig(config) // used for mongoFlagStore
  val docExtractor: DoclibDocExtractor = DoclibDocExtractor()

  /**
    * Process a message and return a HandlerResult. The handle method is passed into a
    * Queue when subscribing to it and acts as a callback for any messages that the
    * queue receives. The key (ie exchange) is extracted from the message within
    * the Queue subscribe method.
    *
    * @param message One of the various Envelopes eg PrefetchMsg, SupervisorMsg
    * @param key     AMQP Exchange
    * @return
    */
  def handle(message: T, key: String): Future[Option[HandlerResult]]

  /**
    * Standardises post processing of a doclib consumer handler's result.
    * This may include writing error flags to a document, calling log.info or log.error,
    * and incrementing the prometheus handler count with the correct labels.
    *
    * @param message              a message with id field from one of the various messages passed in to the handle method
    * @param handlerResult        a handler result which must contain a doclibDoc, and a list of optional derived paths
    * @param supervisorQueueOpt   an optional supervisor queue if a message should be sent to the supervisor queue after a successful handle process
    * @param flagContext          the mongo flag context used to find doclib documents
    * @param collectionOpt        an optional mongo collection used to find doclib documents
    * @param consumerNameAndQueue a consumers name and queue as an implicit parameter
    * @tparam E EnvelopeWithId
    * @tparam R HandlerResult
    * @return
    */
  def postHandleProcess[E <: EnvelopeWithId, R <: HandlerResult]
  (
    message: E,
    handlerResult: Future[Option[R]],
    supervisorQueueOpt: Option[Sendable[SupervisorMsg]],
    flagContext: FlagContext,
    collectionOpt: Option[MongoCollection[DoclibDoc]],
  )
  (implicit consumerNameAndQueue: ConsumerNameAndQueue)
  : Future[Option[R]] = {

    handlerResult.andThen {

      case Success(handlerResultOpt) =>
        handlerResultOpt match {
          case Some(handlerResult) =>
            incrementHandlerCount("success")

            // if our handler has a defined supervisor queue, send a message to this queue with the id of the doclibDoc
            if (supervisorQueueOpt.isDefined) {
              supervisorQueueOpt.get.send(SupervisorMsg(id = handlerResult.doclibDoc._id.toHexString))
            }

            // TODO do we need to log any info about paths? trying to cover edge cases
            if (handlerResult.pathsOpt.isDefined) {
              logger.info(s"SUCCESS - number of derived paths ${handlerResult.pathsOpt.get.length} for: ${handlerResult.doclibDoc._id}")
            } else {
              logger.info(s"SUCCESS - for message ${message.id} doclibDoc id: ${handlerResult.doclibDoc._id}")
            }

          case None =>
            incrementHandlerCount("error_no_document")
            logger.error(s"FAILURE - no document found for message: ${message.id}")
        }

      case Failure(doclibException: DoclibDocException) =>
        incrementHandlerCount("doclib_doc_exception")

        val doclibDoc = doclibException.getDoc

        writeErrorFlag(flagContext, doclibDoc)
        logger.error(s"FAILURE - doclib_doc_exception for doclibDoc ${doclibDoc._id}", doclibException)

      case Failure(e) if collectionOpt.isDefined =>
        incrementHandlerCount("unknown_error")
        failureWithDefinedCollection(e, collectionOpt.get, message.id, flagContext)
        logger.error(s"FAILURE - unknown_error during handle process for message: ${message.id}", e)

      case Failure(e) =>
        // TODO the leadmine handler tests require a document to write the error flag to at this stage,
        // TODO so maybe in leadmine code wrap in a DoclibDocException to be caught further up
        incrementHandlerCount("unknown_error")
        logger.error(s"FAILURE - unknown_error during handle process for message: ${message.id}", e)
    }
  }

  /**
    * Given the postHandleProcess method is called with a defined collection, and the handler result is a failure
    * attempt to write an error flag for the document from collection
    *
    * @param e           exception
    * @param collection  the mongo collection used to locate the document to write the error flag to
    * @param messageId   the message id passed to the handler and postHandleProcess methods
    * @param flagContext flag context
    */
  def failureWithDefinedCollection(e: Throwable, collection: MongoCollection[DoclibDoc],
                                   messageId: String,
                                   flagContext: FlagContext): Unit = {

    findDocById(collection, messageId)
      .onComplete {
        case Success(doclibDocOpt) => doclibDocOpt match {
          case Some(doc) =>
            writeErrorFlag(flagContext, doc)
          case None =>
            val exceptionMessage = s"FAILURE - no document found for: $messageId"
            logger.error(exceptionMessage, new Exception(exceptionMessage))
        }
        case Failure(e) =>
          logger.error(s"FAILURE - error retrieving document: $messageId", e)
      }
  }

  def writeErrorFlag(flagContext: FlagContext, doclibDoc: DoclibDoc): Unit = {
    writeLimiter(flagContext, "write error flag") {
      flagContext =>
        flagContext.error(doclibDoc, noCheck = true)
          .andThen {
            case Failure(e) =>
              logger.error(s"FAILURE: couldn't write error flag for doclib doc id: ${doclibDoc._id}", e)
          }
    }
  }

  def findDocById(collection: MongoCollection[DoclibDoc],
                  messageId: String): Future[Option[DoclibDoc]] = {
    readLimiter(collection, "fetch document by id") { collection =>
      collection
        .find(equal("_id", new ObjectId(messageId)))
        .first()
        .toFutureOption()
    }
  }

  /**
    * Increments the handler count with the given labels
    *
    * @note this should only be used once per call to the handle method in order to derive the correct prometheus metrics
    * @param labels               a list of label values used to increment the handler count
    * @param consumerNameAndQueue used to identify the consumer and queue
    */
  def incrementHandlerCount(labels: String*)(implicit consumerNameAndQueue: ConsumerNameAndQueue): Unit = {
    val labelsWithConsumerInfo = Seq(consumerNameAndQueue.name, consumerNameAndQueue.queue) ++ labels
    handlerCount.labels(labelsWithConsumerInfo: _*).inc()
  }

  def logReceived(messageId: String): Unit = {
    logger.info(f"RECEIVED - message id: $messageId")
  }

}

// a handler's result type can extend this trait to avoid .asInstanceOf casting in tests
trait HandlerResult {
  val doclibDoc: DoclibDoc
  val pathsOpt: Option[List[String]]
}

// generic class which covers most handler result types
case class GenericHandlerResult(doclibDoc: DoclibDoc,
                                pathsOpt: Option[List[String]] = None) extends HandlerResult
