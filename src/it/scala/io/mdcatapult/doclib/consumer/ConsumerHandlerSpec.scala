package io.mdcatapult.doclib.consumer

import com.spingo.op_rabbit.properties.MessageProperty
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

import java.util
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._
import scala.language.postfixOps


class ConsumerHandlerSpec extends AnyFlatSpecLike
  with MockFactory
  with BeforeAndAfterEach
  with HandlerDependencies {


  import actorSystem.dispatcher

  private val prefetchMsg = PrefetchMsg("a-source")

  private val doc = DoclibDoc(
    _id = new ObjectId(),
    source = prefetchMsg.source,
    hash = "12345",
    created = nowUtc.now(),
    updated = nowUtc.now(),
    mimetype = "text/plain"
  )

  private val pathsOpt = Option(List("a/cool/path", "some/other/path"))

  private val handlerReturnSuccess: Future[Option[GenericHandlerReturn]] = Future(Option(GenericHandlerReturn(doc, pathsOpt)))
  private val handlerReturnFailure: Future[Option[GenericHandlerReturn]] = Future(Option(throw new Exception("error")))

  class MyConsumerHandler(val readLimiter: SemaphoreLimitedExecution) extends ConsumerHandler[PrefetchMsg] {
    override def handle(message: PrefetchMsg, key: String): Future[Option[GenericHandlerReturn]] = {
      handlerReturnSuccess
    }
  }

  val handler = new MyConsumerHandler(readLimiter)

  case class Message(id: String) extends EnvelopeWithId

  private val postHandleMessage = Message("1")

  "The postHandleProcess method" should
    "do some success stuff" in {

    val testSupervisorMsg = SupervisorMsg(id = doc._id.toHexString)

    val emptySeq: Seq[MessageProperty] = Seq()
    val supervisorStub = stub[Sendable[SupervisorMsg]]
    (supervisorStub.send _).when(testSupervisorMsg, emptySeq).returns(())

    Await.result(
      handler.postHandleProcess(
        message = postHandleMessage,
        handlerReturn = handlerReturnSuccess,
        supervisorQueueOpt = Option(supervisorStub),
        flagContext = flagContext,
        collectionOpt = Option(collection)
      ),
      Duration.Inf
    )

    prometheusCollectorCalledWithLabelValue("handler_count", "success") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, emptySeq).once()
  }

  it should "do some failure stuff" in {
    val testSupervisorMsg = SupervisorMsg(id = doc._id.toHexString)

    val emptySeq: Seq[MessageProperty] = Seq()
    val supervisorStub = stub[Sendable[SupervisorMsg]]
    (supervisorStub.send _).when(testSupervisorMsg, emptySeq).returns(())

    intercept[Exception] {
      Await.result(
        handler.postHandleProcess(postHandleMessage, handlerReturnFailure, None, flagContext, None),
        Duration.Inf
      )
    }

    prometheusCollectorCalledWithLabelValue("handler_count", "unknown_error") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, emptySeq).never()
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
