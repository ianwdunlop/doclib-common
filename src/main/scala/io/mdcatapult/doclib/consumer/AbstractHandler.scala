package io.mdcatapult.doclib.consumer

import com.typesafe.scalalogging.LazyLogging
import io.mdcatapult.doclib.consumer.HandlerLogStatus._
import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.flag.FlagContext
import io.mdcatapult.doclib.messages.SupervisorMsg
import io.mdcatapult.doclib.metrics.Metrics.handlerCount
import io.mdcatapult.doclib.models.{ConsumerConfig, DoclibDoc}
import io.mdcatapult.klein.queue.{Envelope, Sendable}
import io.mdcatapult.util.concurrency.LimitedExecution
import org.bson.types.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


abstract class AbstractHandler[T <: Envelope](implicit consumerConfig: ConsumerConfig, ec: ExecutionContext)
  extends LazyLogging {

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
  def handle(message: T): Future[Option[HandlerResult]]


  def postHandleProcess[R <: HandlerResult](documentId: String,
                                            handlerResult: Future[Option[R]],
                                            flagContext: FlagContext)
  : Future[Option[R]] = {
    postHandleProcess(documentId, handlerResult, flagContext, None, None)
  }

  def postHandleProcess[R <: HandlerResult](documentId: String,
                                            handlerResult: Future[Option[R]],
                                            flagContext: FlagContext,
                                            collection: MongoCollection[DoclibDoc])
  : Future[Option[R]] = {
    postHandleProcess(documentId, handlerResult, flagContext, None, Option(collection))
  }

  def postHandleProcess[R <: HandlerResult](documentId: String,
                                            handlerResult: Future[Option[R]],
                                            flagContext: FlagContext,
                                            supervisorQueue: Sendable[SupervisorMsg])
  : Future[Option[R]] = {
    postHandleProcess(documentId, handlerResult, flagContext, Option(supervisorQueue), None)
  }

  def postHandleProcess[R <: HandlerResult](documentId: String,
                                            handlerResult: Future[Option[R]],
                                            flagContext: FlagContext,
                                            supervisorQueue: Sendable[SupervisorMsg],
                                            collection: MongoCollection[DoclibDoc])
  : Future[Option[R]] = {
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
                                                    handlerResult: Future[Option[R]],
                                                    flagContext: FlagContext,
                                                    supervisorQueueOpt: Option[Sendable[SupervisorMsg]],
                                                    collectionOpt: Option[MongoCollection[DoclibDoc]])
  : Future[Option[R]] = {
    handlerResult.andThen {
      case Success(handlerResultOpt) =>
        handlerSuccess(documentId, handlerResultOpt, supervisorQueueOpt)

      case Failure(doclibException: DoclibDocException) =>
        failureWithDoclibDocException(doclibException, flagContext)

      case Failure(exception) if collectionOpt.isDefined =>
        failureWithDefinedCollection(collectionOpt.get, documentId, flagContext)

        logger.error(
          loggerMessage(Failed, UnknownError, documentId),
          exception
        )

      case Failure(e) =>
        genericFailure(e, documentId)
    }
  }

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
          loggerMessage(Failed, NoDocumentError, documentId)
        )
    }
  }

  private def failureWithDefinedCollection(collection: MongoCollection[DoclibDoc],
                                           documentId: String,
                                           flagContext: FlagContext): Unit = {
    incrementHandlerCount(UnknownError)

    findDocById(collection, documentId)
      .onComplete {
        case Success(doclibDocOpt) => doclibDocOpt match {
          case Some(doc) =>
            writeErrorFlag(flagContext, doc)
          case None =>
            logger.error(loggerMessage(Failed, NoDocumentError, documentId))
        }
        case Failure(e) =>
          logger.error(loggerMessage(Failed, NoDocumentError, documentId), e)
      }
  }

  private def genericFailure(exception: Throwable, documentId: String): Unit = {
    incrementHandlerCount(UnknownError)

    logger.error(
      loggerMessage(Failed, UnknownError, documentId),
      exception
    )
  }

  private def failureWithDoclibDocException(doclibDocException: DoclibDocException, flagContext: FlagContext): Unit = {
    incrementHandlerCount(DoclibDocumentException)
    val doclibDoc = doclibDocException.getDoc
    writeErrorFlag(flagContext, doclibDoc)

    logger.error(
      loggerMessage(Failed, DoclibDocumentException, doclibDoc._id.toString),
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
                loggerMessage(Failed, ErrorFlagWriteError, doclibDoc._id.toString),
                exception
              )
          }
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
    val labelsWithConsumerInfo = Seq(consumerConfig.name, consumerConfig.queue) ++ labels
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


case class GenericHandlerResult(doclibDoc: DoclibDoc) extends HandlerResult

case class HandlerResultWithDerivatives(doclibDoc: DoclibDoc, derivatives: Option[List[String]]) extends HandlerResult
