package io.mdcatapult.doclib.consumer

import com.spingo.op_rabbit.properties.MessageProperty
import com.typesafe.scalalogging.{LazyLogging, Logger}
import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.messages.{PrefetchMsg, SupervisorMsg}
import io.mdcatapult.doclib.metrics.Metrics.handlerCount
import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.klein.queue.{EnvelopeWithId, Sendable}
import io.mdcatapult.util.concurrency.SemaphoreLimitedExecution
import io.mdcatapult.util.time.nowUtc
import org.bson.types.ObjectId
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.slf4j.{Logger => UnderlyingLogger}

import java.util
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

class ConsumerHandlerSpec extends AnyFlatSpecLike
  with MockFactory
  with BeforeAndAfterEach
  with HandlerDependencies with LazyLogging {

  case class TestMessage(id: String) extends EnvelopeWithId

  private val awaitDuration = 5 seconds

  import actorSystem.dispatcher

  private val prefetchMsg = PrefetchMsg("a-source")

  private val testDoclibDoc = DoclibDoc(
    _id = new ObjectId(),
    source = prefetchMsg.source,
    hash = "12345",
    created = nowUtc.now(),
    updated = nowUtc.now(),
    mimetype = "text/plain"
  )

  private val pathsOpt = Option(List("a/cool/path", "some/other/path"))

  private val handlerReturnSuccess: Future[Option[GenericHandlerReturn]] = Future(Option(GenericHandlerReturn(testDoclibDoc, pathsOpt)))
  private val handlerReturnEmptySuccess: Future[Option[GenericHandlerReturn]] = Future(None)
  private val handlerReturnFailure: Future[Option[GenericHandlerReturn]] = Future(Option(throw new Exception("error")))
  private val handlerReturnDoclibExceptionFailure: Future[Option[GenericHandlerReturn]] =
    Future(Option(throw new DoclibDocException(testDoclibDoc, "oh dear")))

  val underlyingMockLogger: UnderlyingLogger = mock[UnderlyingLogger]

  class MyConsumerHandler(val readLimiter: SemaphoreLimitedExecution) extends ConsumerHandler[PrefetchMsg] {
    override def handle(message: PrefetchMsg, key: String): Future[Option[GenericHandlerReturn]] = {
      handlerReturnSuccess
    }

    override lazy val logger: Logger = Logger(underlyingMockLogger)
  }

  val handler = new MyConsumerHandler(readLimiter)
  private val testSupervisorMsg = SupervisorMsg(id = testDoclibDoc._id.toHexString)
  private val postHandleMessage = TestMessage(testDoclibDoc._id.toHexString)

  "The postHandleProcess method" should
    "send a message to the supervisor, call log.info, and increment the correct prometheus collector " +
      "given a defined supervisor queue, and a successful handler return value" in {

    val supervisorStub = stub[Sendable[SupervisorMsg]]
    (supervisorStub.send _).when(testSupervisorMsg, Seq.empty[MessageProperty]).returns(())

    Await.result(
      handler.postHandleProcess(
        message = postHandleMessage,
        handlerReturn = handlerReturnSuccess,
        supervisorQueueOpt = Option(supervisorStub),
        flagContext = flagContext,
        collectionOpt = Option(collection)
      ),
      awaitDuration
    )

    prometheusCollectorCalledWithLabelValue("handler_count", "success") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, Seq.empty[MessageProperty]).once()
    (underlyingMockLogger.info(_: String)).verify(_: String).once()
  }

  it should "not send a message to the supervisor, call log.info, and increment the correct prometheus collector " +
    "given an undefined supervisor queue, and a successful handler return value" in {
    val supervisorStub = stub[Sendable[SupervisorMsg]]
    (supervisorStub.send _).when(testSupervisorMsg, Seq.empty[MessageProperty]).returns(())

    Await.result(
      handler.postHandleProcess(
        message = postHandleMessage,
        handlerReturn = handlerReturnSuccess,
        supervisorQueueOpt = None,
        flagContext = flagContext,
        collectionOpt = Option(collection)
      ),
      awaitDuration
    )

    prometheusCollectorCalledWithLabelValue("handler_count", "success") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, Seq.empty[MessageProperty]).never()
    (underlyingMockLogger.info(_: String)).verify(_: String).once()
  }

  it should "increment the correct prometheus collector, and call log.error" +
    "given an undefined successful handler return value " in {

    Await.result(
      handler.postHandleProcess(
        message = postHandleMessage,
        handlerReturn = handlerReturnEmptySuccess,
        supervisorQueueOpt = None,
        flagContext = flagContext,
        collectionOpt = None
      ),
      awaitDuration
    )

    prometheusCollectorCalledWithLabelValue("handler_count", "error_no_document") shouldBe true
    (underlyingMockLogger.error(_: String)).verify(_: String).once()
  }

  it should "call the expected prometheus collector, log.error, and not send a message to the supervisor " +
    "given a handler return failure and an undefined supervisor queue" in {
    val testSupervisorMsg = SupervisorMsg(id = testDoclibDoc._id.toHexString)

    val emptySeq: Seq[MessageProperty] = Seq()
    val supervisorStub = stub[Sendable[SupervisorMsg]]
    (supervisorStub.send _).when(testSupervisorMsg, emptySeq).returns(())

    intercept[Exception] {
      Await.result(
        handler.postHandleProcess(
          message = postHandleMessage,
          handlerReturn = handlerReturnFailure,
          supervisorQueueOpt = None,
          flagContext = flagContext,
          collectionOpt = None
        ),
        awaitDuration
      )
    }

    prometheusCollectorCalledWithLabelValue("handler_count", "unknown_error") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, emptySeq).never()
    (underlyingMockLogger.error(_: String)).verify(_: String).once()
  }

  it should "asdf" in {

    Await.result(collection.insertOne(testDoclibDoc).toFuture(), awaitDuration)
    val doca = Await.result(handler.findDocById(collection, postHandleMessage.id, readLimiter), awaitDuration)

    val futureResult =
      for {
        //        _ <- collection.insertOne(testDoclibDoc).toFuture()
        _ <- handler.postHandleProcess(
          message = postHandleMessage,
          handlerReturn = handlerReturnDoclibExceptionFailure,
          supervisorQueueOpt = None,
          flagContext = flagContext,
          collectionOpt = None
        )
      } yield ()

    val blah = intercept[Exception] {
      Await.result(futureResult, awaitDuration)
    }
    Thread.sleep(2000)

    //    (underlyingMockLogger.error(_: String)).verify(_: String).once()

    prometheusCollectorCalledWithLabelValue("handler_count", "doclib_doc_exception") shouldBe true

    val doc = Await.result(handler.findDocById(collection, postHandleMessage.id, readLimiter), awaitDuration)

    assert(doca == doc)

    val test = doc
  }


  private def prometheusCollectorCalledWithLabelValue(collectorName: String, labelValue: String): Boolean = {
    defaultPrometheusRegistry
      .filteredMetricFamilySamples(util.Set.of(collectorName))
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

  // clear the handlerCount collector to only have one sample to search from when querying the prometheus registry
  override def beforeEach(): Unit = {
    handlerCount.clear()
  }
}
