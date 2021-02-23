package io.mdcatapult.doclib.consumer

import com.typesafe.scalalogging.LazyLogging
import io.mdcatapult.doclib.consumer.HandleLogStatus.loggerMessage
import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.flag.FlagContext
import io.mdcatapult.doclib.messages.SupervisorMsg
import io.mdcatapult.doclib.metrics.Metrics.handlerCount
import io.mdcatapult.doclib.models.{ConsumerNameAndQueue, DoclibDoc}
import io.mdcatapult.klein.queue.{Envelope, Sendable}
import io.mdcatapult.util.concurrency.LimitedExecution
import org.bson.types.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


abstract class ConsumerHandler[T <: Envelope](implicit ec: ExecutionContext) extends LazyLogging {

  val readLimiter: LimitedExecution
  val writeLimiter: LimitedExecution

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


  def postHandleProcess[R <: HandlerResult](messageId: String,
                                            handlerResult: Future[Option[R]],
                                            flagContext: FlagContext)
                                           (implicit consumerNameAndQueue: ConsumerNameAndQueue)
  : Future[Option[R]] = {
    postHandleProcess(messageId, handlerResult, flagContext, None, None)
  }


  def postHandleProcess[R <: HandlerResult](messageId: String,
                                            handlerResult: Future[Option[R]],
                                            flagContext: FlagContext,
                                            collection: MongoCollection[DoclibDoc])
                                           (implicit consumerNameAndQueue: ConsumerNameAndQueue)
  : Future[Option[R]] = {
    postHandleProcess(messageId, handlerResult, flagContext, None, Option(collection))
  }

  def postHandleProcess[R <: HandlerResult](messageId: String,
                                            handlerResult: Future[Option[R]],
                                            supervisorQueue: Sendable[SupervisorMsg],
                                            flagContext: FlagContext)
                                           (implicit consumerNameAndQueue: ConsumerNameAndQueue)
  : Future[Option[R]] = {
    postHandleProcess(messageId, handlerResult, flagContext, Option(supervisorQueue), None)
  }

  def postHandleProcess[R <: HandlerResult](messageId: String,
                                            handlerResult: Future[Option[R]],
                                            supervisorQueue: Sendable[SupervisorMsg],
                                            flagContext: FlagContext,
                                            collection: MongoCollection[DoclibDoc])
                                           (implicit consumerNameAndQueue: ConsumerNameAndQueue)
  : Future[Option[R]] = {
    postHandleProcess(messageId, handlerResult, flagContext, Option(supervisorQueue), Option(collection))
  }


  /**
    * Standardises post processing of a doclib consumer handler's result.
    * This may include writing error flags to a document, calling log.info or log.error,
    * and incrementing the prometheus handler count with the correct labels.
    *
    * @param messageId            a messageId from one of the various messages passed in to the handle method
    * @param handlerResult        a handler result which must contain a doclibDoc, and a list of optional derived paths
    * @param supervisorQueueOpt   an optional supervisor queue if a message should be sent to the supervisor queue after a successful handle process
    * @param flagContext          the mongo flag context used to find doclib documents
    * @param collectionOpt        an optional mongo collection used to find doclib documents
    * @param consumerNameAndQueue a consumers name and queue as an implicit parameter
    * @tparam R HandlerResult
    * @return
    */
  private def postHandleProcess[R <: HandlerResult](messageId: String,
                                                    handlerResult: Future[Option[R]],
                                                    flagContext: FlagContext,
                                                    supervisorQueueOpt: Option[Sendable[SupervisorMsg]],
                                                    collectionOpt: Option[MongoCollection[DoclibDoc]])
                                                   (implicit consumerNameAndQueue: ConsumerNameAndQueue): Future[Option[R]] = {
    handlerResult.andThen {
      case Success(handlerResultOpt) => handlerSuccess(messageId, handlerResultOpt, supervisorQueueOpt)
      case Failure(doclibException: DoclibDocException) => failureWithDoclibDocException(doclibException, flagContext)
      case Failure(exception) if collectionOpt.isDefined =>
        incrementHandlerCount("unknown_error")

        failureWithDefinedCollection(collectionOpt.get, messageId, flagContext)

        logger.error(
          loggerMessage(Failed, "unknown_error", messageId),
          exception
        )
      case Failure(e) =>
        // TODO the leadmine handler tests require a document to write the error flag to at this stage,
        // TODO so maybe in leadmine code wrap in a DoclibDocException to be caught further up
        genericFailure(e, messageId)
    }
  }

  private def handlerSuccess[R <: HandlerResult](messageId: String,
                                                 handlerResultOpt: Option[R],
                                                 supervisorQueueOpt: Option[Sendable[SupervisorMsg]])
                                                (implicit consumerNameAndQueue: ConsumerNameAndQueue): Unit = {
    handlerResultOpt match {
      case Some(handlerResult) =>
        if (supervisorQueueOpt.isDefined) {
          supervisorQueueOpt.get.send(SupervisorMsg(id = handlerResult.doclibDoc._id.toHexString))
        }

        incrementHandlerCount("success")
        logger.info(loggerMessage(Completed, messageId, handlerResult.doclibDoc._id))
      case None =>
        incrementHandlerCount("error_no_document")
        logger.error(loggerMessage(Failed, "error_no_document", messageId))
    }
  }

  private def failureWithDefinedCollection(collection: MongoCollection[DoclibDoc],
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

  private def genericFailure(exception: Throwable, messageId: String)
                            (implicit consumerNameAndQueue: ConsumerNameAndQueue): Unit = {
    incrementHandlerCount("unknown_error")

    logger.error(
      loggerMessage(Failed, "unknown_error", messageId),
      exception
    )
  }

  private def failureWithDoclibDocException(doclibDocException: DoclibDocException, flagContext: FlagContext)
                                           (implicit consumerNameAndQueue: ConsumerNameAndQueue): Unit = {
    incrementHandlerCount("doclib_doc_exception")
    val doclibDoc = doclibDocException.getDoc
    writeErrorFlag(flagContext, doclibDoc)

    logger.error(
      loggerMessage(Failed, "doclib_doc_exception for doclibDoc", doclibDoc._id.toString),
      doclibDocException
    )
  }


  def writeErrorFlag(flagContext: FlagContext, doclibDoc: DoclibDoc): Unit = {
    writeLimiter(flagContext, "write error flag") {
      flagContext =>
        flagContext.error(doclibDoc, noCheck = true)
          .andThen {
            case Failure(exception) =>
              logger.error(
                loggerMessage(Failed, "could not write error flag", doclibDoc._id),
                exception
              )
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

  /**
    * Log when a handler receives a message
    *
    * @param messageId the message id of the incoming message
    */
  def logReceived(messageId: String): Unit = {
    logger.info(loggerMessage(Received, messageId))
  }
}


// a handler's result type can extend this trait to avoid .asInstanceOf casting in tests
trait HandlerResult {
  val doclibDoc: DoclibDoc
}

// generic class which covers most handler result types
case class GenericHandlerResult(doclibDoc: DoclibDoc) extends HandlerResult
