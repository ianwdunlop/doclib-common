package io.mdcatapult.doclib.consumer

import com.spingo.op_rabbit.properties.MessageProperty
import com.typesafe.scalalogging.Logger
import io.mdcatapult.doclib.messages.{PrefetchMsg, SupervisorMsg}
import io.mdcatapult.doclib.metrics.Metrics.handlerCount
import io.mdcatapult.util.concurrency.LimitedExecution
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.slf4j.{Logger => UnderlyingLogger}

import java.util
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._
import scala.language.postfixOps


class ConsumerHandlerSpec extends AnyFlatSpecLike
  with MockFactory
  with BeforeAndAfterEach
  with HandlerTestDependencies
  with HandlerTestData {

  import actorSystem.dispatcher

  private val awaitDuration = 2 seconds

  val handler = new MyConsumerHandler(readLimiter, writeLimiter)
  (supervisorStub.send _).when(testSupervisorMsg, Seq.empty[MessageProperty]).returns(())

  "The postHandleProcess method" should
    "send a message to the supervisor, call log.info, and increment the correct prometheus collector " +
      "given a defined supervisor queue, and a successful handler return value" in {

    Await.result(
      handler.postHandleProcess(
        message = postHandleMessage,
        handlerResult = handlerResultSuccess,
        supervisorQueueOpt = Option(supervisorStub),
        flagContext = flagContext,
        collectionOpt = Option(collection)
      ),
      awaitDuration
    )

    prometheusCollectorCalledWithLabelValue("success") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, Seq.empty[MessageProperty]).once()
    (underlyingMockLogger.info(_: String)).verify(_: String).once()
  }

  it should "not send a message to the supervisor, call log.info, and increment the correct prometheus collector " +
    "given an undefined supervisor queue, and a successful handler return value" in {

    Await.result(
      handler.postHandleProcess(
        message = postHandleMessage,
        handlerResult = handlerResultSuccess,
        supervisorQueueOpt = None,
        flagContext = flagContext,
        collectionOpt = Option(collection)
      ),
      awaitDuration
    )

    prometheusCollectorCalledWithLabelValue("success") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, Seq.empty[MessageProperty]).never()
    (underlyingMockLogger.info(_: String)).verify(_: String).once()
  }

  it should "increment the correct prometheus collector, and call log.error" +
    "given an undefined successful handler return value " in {

    Await.result(
      handler.postHandleProcess(
        message = postHandleMessage,
        handlerResult = handlerResultEmptySuccess,
        supervisorQueueOpt = None,
        flagContext = flagContext,
        collectionOpt = None
      ),
      awaitDuration
    )

    prometheusCollectorCalledWithLabelValue("error_no_document") shouldBe true
    (underlyingMockLogger.error(_: String)).verify(_: String).once()
  }

  it should "call the expected prometheus collector, log.error, and not send a message to the supervisor " +
    "given a handler return failure and an undefined supervisor queue" in {
    val testSupervisorMsg = SupervisorMsg(id = testDoclibDoc._id.toHexString)

    intercept[Exception] {
      Await.result(
        handler.postHandleProcess(
          message = postHandleMessage,
          handlerResult = handlerResultFailure,
          supervisorQueueOpt = None,
          flagContext = flagContext,
          collectionOpt = None
        ),
        awaitDuration
      )
    }

    prometheusCollectorCalledWithLabelValue("unknown_error") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, Seq()).never()
    (underlyingMockLogger.error(_: String)).verify(_: String).once()
  }

  it should "write an error flag to the doclib document, call the prometheus collector with the correct label," +
    "and log.error, given a doclib doc exists in the db, and the handler return value is a doclib doc exception" in {

    val futureResult =
      for {
        _ <- collection.insertOne(testDoclibDoc).toFuture()
        _ <- flagContext.start(testDoclibDoc)
        _ <- handler.postHandleProcess(
          message = postHandleMessage,
          handlerResult = handlerResultDoclibExceptionFailure,
          supervisorQueueOpt = None,
          flagContext = flagContext,
          collectionOpt = None
        )
      } yield ()

    intercept[Exception] {
      Await.result(futureResult, awaitDuration)
    }

    val doclibDocAfterPostHandleProcess =
      Await.result(handler.findDocById(collection, postHandleMessage.id), awaitDuration).get

    prometheusCollectorCalledWithLabelValue("doclib_doc_exception") shouldBe true
    doclibDocAfterPostHandleProcess.doclib.head.errored.isDefined shouldBe true
    (underlyingMockLogger.error(_: String)).verify(_: String).once()
  }

  it should "write an error flag to the doclib document, call the prometheus collector with the correct label," +
    "and log.error, given a doclib doc exists in the db, there is a defined mongo collection, " +
    "and the handler return value is a generic exception" in {

    val futureResult =
      for {
        _ <- collection.insertOne(testDoclibDoc).toFuture()
        _ <- flagContext.start(testDoclibDoc)
        _ <- handler.postHandleProcess(
          message = postHandleMessage,
          handlerResult = handlerResultFailure,
          supervisorQueueOpt = None,
          flagContext = flagContext,
          collectionOpt = Some(collection)
        )
      } yield ()

    intercept[Exception] {
      Await.result(futureResult, awaitDuration)
    }

    Thread.sleep(500) // allow error flag to be written

    val doclibDocAfterPostHandleProcess =
      Await.result(handler.findDocById(collection, postHandleMessage.id), awaitDuration).get

    prometheusCollectorCalledWithLabelValue("unknown_error") shouldBe true
    doclibDocAfterPostHandleProcess.doclib.head.errored.isDefined shouldBe true
    (underlyingMockLogger.error(_: String)).verify(_: String).once()
  }


  // clear the handlerCount collector to only have one sample to search from when querying the prometheus registry
  override def beforeEach(): Unit = {
    Await.result(collection.drop().toFuture(), awaitDuration)
    handlerCount.clear()
  }

  /**
    * Queries the prometheus registry for a label value found on the handler_count collector
    *
    * @note the handlerCount collector should be cleared before running each test if using this method,
    *       as label values are appended to an array
    */
  private def prometheusCollectorCalledWithLabelValue(labelValue: String): Boolean = {
    val prometheusHandlerCountName = "handler_count"

    defaultPrometheusRegistry
      .filteredMetricFamilySamples(util.Set.of(prometheusHandlerCountName))
      .asIterator()
      .asScala
      .exists(collector => {
        val collectorSamples = collector
          .samples
          .asScala

        collectorSamples
          .headOption
          .exists(samples => {
            samples.labelValues.contains(labelValue)
          })
      })
  }

  lazy val underlyingMockLogger: UnderlyingLogger = stub[UnderlyingLogger]

  class MyConsumerHandler(val readLimiter: LimitedExecution,
                          val writeLimiter: LimitedExecution) extends ConsumerHandler[PrefetchMsg] {
    override def handle(message: PrefetchMsg, key: String): Future[Option[GenericHandlerResult]] = {
      handlerResultSuccess
    }

    override lazy val logger: Logger = Logger(underlyingMockLogger)
  }

}
