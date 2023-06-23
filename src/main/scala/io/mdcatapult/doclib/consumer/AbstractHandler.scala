package io.mdcatapult.doclib.consumer

import akka.stream.alpakka.amqp.scaladsl.CommittableReadResult
import com.typesafe.scalalogging.LazyLogging
import io.mdcatapult.doclib.consumer.HandlerLogStatus._
import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.flag.FlagContext
import io.mdcatapult.doclib.messages.SupervisorMsg
import io.mdcatapult.doclib.metrics.Metrics.handlerCount
import io.mdcatapult.doclib.models.{AppConfig, DoclibDoc}
import io.mdcatapult.klein.queue.{Envelope, Sendable}
import io.mdcatapult.util.concurrency.LimitedExecution
import io.mdcatapult.util.models.result.UpdatedResult
import org.bson.types.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

abstract class AbstractHandler[T <: Envelope](implicit appConfig: AppConfig, ec: ExecutionContext)
  extends LazyLogging {

  type GenericHandlerResult = (CommittableReadResult, Try[HandlerResult])

  val readLimiter: LimitedExecution
  val writeLimiter: LimitedExecution

  /**
    * Process a message and return a HandlerResult. The handle method is passed into a
    * Queue when subscribing to it and acts as a callback for any messages that the
    * queue receives. The key (ie exchange) is extracted from the message within
    * the Queue subscribe method.
    *
    * @param message One of the various Envelopes eg PrefetchMsg, SupervisorMsg
    * @return
    */
  def handle(messageWrapper: CommittableReadResult): Future[(CommittableReadResult, Try[HandlerResult])]

  def postHandleProcess[R <: HandlerResult](documentId: String,
                                            handlerResult: Future[(CommittableReadResult, Try[HandlerResult])],
                                            flagContext: FlagContext)
  : Future[(CommittableReadResult, Try[HandlerResult])] = {
    postHandleProcess(documentId, handlerResult, flagContext, None, None)
  }

  def postHandleProcess[R <: HandlerResult](documentId: String,
                                            handlerResult: Future[(CommittableReadResult, Try[HandlerResult])],
                                            flagContext: FlagContext,
                                            collection: MongoCollection[DoclibDoc])
  : Future[(CommittableReadResult, Try[HandlerResult])] = {
    postHandleProcess(documentId, handlerResult, flagContext, None, Option(collection))
  }

  def postHandleProcess[R <: HandlerResult](documentId: String,
                                            handlerResult: Future[(CommittableReadResult, Try[HandlerResult])],
                                            flagContext: FlagContext,
                                            supervisorQueue: Sendable[SupervisorMsg])
  : Future[(CommittableReadResult, Try[HandlerResult])] = {
    postHandleProcess(documentId, handlerResult, flagContext, Option(supervisorQueue), None)
  }

  def postHandleProcess[R <: HandlerResult](documentId: String,
                                            handlerResult: Future[(CommittableReadResult, Try[HandlerResult])],
                                            flagContext: FlagContext,
                                            supervisorQueue: Sendable[SupervisorMsg],
                                            collection: MongoCollection[DoclibDoc])
  : Future[(CommittableReadResult, Try[HandlerResult])] = {
    postHandleProcess(documentId, handlerResult, flagContext, Option(supervisorQueue), Option(collection))
  }


  /**
    * Standardises post processing of a doclib consumer handler's result.
    * This may include writing error flags to a document, calling log.info or log.error,
    * and incrementing the prometheus handler count with the correct labels.
    *
    * @param documentId         a documentId from one of the various messages passed in to the handle method
    * @param handlerResult      a handler result which must contain a doclibDoc, and extend the HandlerResult trait
    * @param supervisorQueueOpt an optional supervisor queue if a message should be sent to the supervisor queue after a successful handle process
    * @param flagContext        the mongo flag context used to find doclib documents
    * @param collectionOpt      an optional mongo collection used to find doclib documents
    * @tparam R HandlerResult
    * @return
    */
  private def postHandleProcess[R <: HandlerResult](documentId: String,
                                                    handlerResult: Future[GenericHandlerResult],
                                                    flagContext: FlagContext,
                                                    supervisorQueueOpt: Option[Sendable[SupervisorMsg]],
                                                    collectionOpt: Option[MongoCollection[DoclibDoc]])
  : Future[(CommittableReadResult, Try[HandlerResult])] = {
    handlerResult.andThen {
      case Success(handlerResultOpt) =>
        handlerResultOpt._2 match {
          case Success(handlerResult) => handlerSuccess(documentId, Some(handlerResult), supervisorQueueOpt)
          case Failure(doclibException: DoclibDocException) =>
            failureWithDoclibDocException(doclibException, flagContext)
          case Failure(exception) if collectionOpt.isDefined =>
            failureWithDefinedCollection(exception, collectionOpt.get, documentId, flagContext)
          case Failure(exception) =>
            genericFailure(exception, documentId)
        }
    }
  }

  /**
    * Called if the handler result returns a successful future.
    * If the HandlerResult is defined, optionally sends a message to the supervisor,
    * increments the prometheus handler count, and logs a success.
    * If the HandlerResult is not defined, increments the prom. handler count with a no document error, and logs a failure
    *
    * @param documentId         the documentId of the existing or newly created document
    * @param handlerResultOpt   The successful handlerResult
    * @param supervisorQueueOpt An optional supervisor queue if the consumer should send a message to the supervisor
    * @tparam R type must be a subtype of HandlerResult
    */
  private def handlerSuccess[R <: HandlerResult](documentId: String,
                                                 handlerResultOpt: Option[R],
                                                 supervisorQueueOpt: Option[Sendable[SupervisorMsg]]): Unit = {
    handlerResultOpt match {
      case Some(handlerResult) =>
        if (supervisorQueueOpt.isDefined) {
          supervisorQueueOpt.get.send(
            SupervisorMsg(id = handlerResult.doclibDoc._id.toHexString)
          )
        }

        incrementHandlerCount(Completed.toString)
        logger.info(loggerMessage(Completed, documentId))
      case None =>
        incrementHandlerCount(NoDocumentError)
        logger.error(
          loggerMessage(Failed, documentId, NoDocumentError)
        )
    }
  }

  /**
    * If the handlerResult is a failure, increments the prometheus handler count with an unknown error.
    * Then attempts to find the document in the collection, and if successful writes an error flag to that document.
    * In all other cases logs an error.
    *
    * @param collection  The mongo collection to query
    * @param documentId  The documentId to query against the collection
    * @param flagContext The flagContext for writing an error flag
    */
  private def failureWithDefinedCollection(exception: Throwable,
                                           collection: MongoCollection[DoclibDoc],
                                           documentId: String,
                                           flagContext: FlagContext): Unit = {
    incrementHandlerCount(UnknownError)

    findDocById(collection, documentId)
      .onComplete {
        case Failure(_) =>
          logger.error(
            loggerMessage(Failed, documentId, UnknownError),
            exception
          )

        case Success(doclibDocOpt) => doclibDocOpt match {
          case None =>
            logger.error(loggerMessage(Failed, documentId, UnknownError, NoDocumentError))

          case Some(doc) =>
            writeErrorFlag(flagContext, doc).andThen {
              case Failure(_) =>
                logger.error(
                  loggerMessage(Failed, doc._id.toHexString, UnknownError, ErrorFlagWriteError),
                  exception
                )
            }
        }
      }
  }

  /**
    * Without a collection or flag context, increment the prometheus handler with an unknown error and log a message
    *
    * @param exception  The exception to log
    * @param documentId The document id to log
    */
  private def genericFailure(exception: Throwable, documentId: String): Unit = {
    incrementHandlerCount(UnknownError)

    logger.error(
      loggerMessage(Failed, documentId, UnknownError),
      exception
    )
  }

  /**
    * If we have a doclibDocException and a flagContext, increment the prometheus handler with a doclib document exception
    * Then attempt to write an error flag to the document and log an error
    *
    * @param doclibDocException
    * @param flagContext
    */
  private def failureWithDoclibDocException(doclibDocException: DoclibDocException, flagContext: FlagContext): Unit = {
    incrementHandlerCount(DoclibDocumentException)
    val doclibDoc = doclibDocException.getDoc

    writeErrorFlag(flagContext, doclibDoc).onComplete {
      case Failure(exception) =>
        logger.error(
          loggerMessage(Failed, doclibDoc._id.toHexString, DoclibDocumentException, ErrorFlagWriteError),
          exception
        )
      case Success(_) =>
        logger.error(
          loggerMessage(Failed, doclibDoc._id.toHexString, DoclibDocumentException),
          doclibDocException
        )
    }
  }

  def writeErrorFlag(flagContext: FlagContext, doclibDoc: DoclibDoc): Future[UpdatedResult] = {
    writeLimiter(flagContext, "write error flag") {
      flagContext =>
        flagContext.error(doclibDoc, noCheck = true)
    }
  }

  def findDocById(collection: MongoCollection[DoclibDoc],
                  documentId: String): Future[Option[DoclibDoc]] = {
    readLimiter(collection, "fetch document by id") { collection =>
      collection
        .find(equal("_id", new ObjectId(documentId)))
        .first()
        .toFutureOption()
    }
  }

  /**
    * Increments the handler count with the given labels
    *
    * @note this should only be used once per call to the handle method in order to derive the correct prometheus metrics
    * @param labels a list of label values used to increment the handler count
    */
  def incrementHandlerCount(labels: String*): Unit = {
    val labelsWithConsumerInfo = Seq(appConfig.name, appConfig.queue) ++ labels
    handlerCount.labels(labelsWithConsumerInfo: _*).inc()
  }

  /**
    * Log when a message is received
    *
    * @param documentId the document id referenced in the incoming message
    */
  def logReceived(documentId: String): Unit = {
    logger.info(loggerMessage(Received, documentId))
  }
}


trait HandlerResult {
  val doclibDoc: DoclibDoc
}
