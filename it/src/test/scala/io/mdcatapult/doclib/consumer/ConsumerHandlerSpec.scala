package io.mdcatapult.doclib.consumer

import org.apache.pekko.Done
import org.apache.pekko.stream.connectors.amqp.scaladsl.CommittableReadResult
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
import scala.util.Try


class ConsumerHandlerSpec extends AnyFlatSpecLike
  with MockFactory
  with BeforeAndAfterEach
  with HandlerTestDependencies
  with HandlerTestData {

  import actorSystem.dispatcher

  private val awaitDuration = 5 seconds

  val handler = new MyConsumerHandler(readLimiter, writeLimiter)
  (supervisorStub.send _).when(testSupervisorMsg, None).returns(Future[Done](Done.getInstance()))

  "The postHandleProcess method" should
    "send a message to the supervisor, call log.info, and increment the correct prometheus collector " +
      "given a defined supervisor queue, and a successful handler return value" in {

    Await.result(
      handler.postHandleProcess(
        documentId = postHandleMessage.id,
        handlerResult = handlerResultSuccess,
        mongoFlagContext,
        supervisorStub,
        collection
      ),
      awaitDuration
    )

    prometheusCollectorCalledWithLabelValue("Completed") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, None).once()
    (underlyingMockLogger.info(_: String)).verify(_: String).once()
  }

  it should "not send a message to the supervisor, call log.info, and increment the correct prometheus collector " +
    "given an undefined supervisor queue, and a successful handler return value" in {

    Await.result(
      handler.postHandleProcess(
        documentId = postHandleMessage.id,
        handlerResult = handlerResultSuccess,
        mongoFlagContext,
        collection
      ),
      awaitDuration
    )

    prometheusCollectorCalledWithLabelValue("Completed") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, None).never()
    (underlyingMockLogger.info(_: String)).verify(_: String).once()
  }

  it should "call the expected prometheus collector, log.error, and not send a message to the supervisor " +
    "given a handler return failure and an undefined supervisor queue" in {
    val testSupervisorMsg = SupervisorMsg(id = testDoclibDoc._id.toHexString)

    Await.result(
        handler.postHandleProcess(
          documentId = postHandleMessage.id,
          handlerResult = handlerResultFailure,
          mongoFlagContext,
        ),
        awaitDuration
      )

    prometheusCollectorCalledWithLabelValue("unknown_error") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, None).never()
    (underlyingMockLogger.error(_: String)).verify(_: String).once()
  }

  it should "write an error flag to the doclib document, call the prometheus collector with the correct label," +
    "and log.error, given a doclib doc exists in the db, and the handler return value is a doclib doc exception" in {

    val futureResult =
      for {
        _ <- collection.insertOne(testDoclibDoc).toFuture()
        _ <- mongoFlagContext.start(testDoclibDoc)
        _ <- handler.postHandleProcess(
          documentId = postHandleMessage.id,
          handlerResult = handlerResultDoclibExceptionFailure,
          mongoFlagContext
        )
      } yield ()


    Await.result(futureResult, awaitDuration)

    Thread.sleep(1000) // allow error flag to be written

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
        _ <- mongoFlagContext.start(testDoclibDoc)
        _ <- handler.postHandleProcess(
          documentId = postHandleMessage.id,
          handlerResult = handlerResultFailure,
          mongoFlagContext,
          collection
        )
      } yield ()

    Await.result(futureResult, awaitDuration)

    Thread.sleep(1000) // allow error flag to be written

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
                          val writeLimiter: LimitedExecution) extends AbstractHandler[PrefetchMsg, GenericHandlerResult] {
    override def handle(message: CommittableReadResult): Future[(CommittableReadResult, Try[GenericHandlerResult])] = {
      handlerResultSuccess
    }

    override lazy val logger: Logger = Logger(underlyingMockLogger)
  }

}
