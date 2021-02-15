package io.mdcatapult.doclib.consumer

import akka.actor.ActorSystem
import com.spingo.op_rabbit.properties.MessageProperty
import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.flag.{FlagContext, MongoFlagStore}
import io.mdcatapult.doclib.messages.{DoclibMsg, PrefetchMsg, SupervisorMsg}
import io.mdcatapult.doclib.metrics.Metrics.handlerCount
import io.mdcatapult.doclib.models.{ConsumerNameAndQueue, DoclibDoc, DoclibDocExtractor}
import io.mdcatapult.klein.mongo.Mongo
import io.mdcatapult.klein.queue.{EnvelopeWithId, Queue, Sendable}
import io.mdcatapult.util.concurrency.SemaphoreLimitedExecution
import io.mdcatapult.util.models.Version
import io.mdcatapult.util.time.nowUtc
import io.prometheus.client.CollectorRegistry
import org.bson.types.ObjectId
import org.mongodb.scala.MongoCollection
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.util
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._
import scala.language.postfixOps


class ConsumerHandlerSpec extends AnyFlatSpecLike with MockFactory {

  implicit val actorSystem: ActorSystem = ActorSystem("Test")

  import actorSystem.dispatcher

  implicit val config: Config = ConfigFactory.load()

  private val prefetchMsg = PrefetchMsg("a-source")

  private val doc = DoclibDoc(
    _id = new ObjectId(),
    source = prefetchMsg.source,
    hash = "12345",
    created = nowUtc.now(),
    updated = nowUtc.now(),
    mimetype = "text/plain"
  )


  val defaultPrometheusRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
  private val pathsOpt = Option(List("a/cool/path", "some/other/path"))

  private implicit val consumerNameAndQueue: ConsumerNameAndQueue =
    ConsumerNameAndQueue(config.getString("consumer.name"), config.getString("consumer.queue"))

  val version: Version = Version.fromConfig(config)

  private val readLimiter = SemaphoreLimitedExecution.create(1)
  private val handlerReturnSuccess: Future[Option[GenericHandlerReturn]] = Future(Option(GenericHandlerReturn(doc, pathsOpt)))
  private val handlerReturnFailure: Future[Option[GenericHandlerReturn]] = Future(Option(throw new Exception("error")))


  val downstream: Sendable[DoclibMsg] = stub[Sendable[DoclibMsg]]
  val archiver: Sendable[DoclibMsg] = stub[Sendable[DoclibMsg]]

  val supervisor: Queue[SupervisorMsg] = Queue(config.getString("doclib.supervisor.queue"), consumerName = Option("test"), errorQueue = None)

  // TODO investigate why declaring MongoFlagStore outside of this fn causes large numbers DoclibDoc objects on the heap
  val mongo: Mongo = new Mongo()
  implicit val collection: MongoCollection[DoclibDoc] = mongo.getCollection(config.getString("mongo.doclib-database"), config.getString("mongo.documents-collection"))

  val flags = new MongoFlagStore(version, DoclibDocExtractor(), collection, nowUtc)
  val flagContext: FlagContext = flags.findFlagContext(Some(consumerNameAndQueue.name))


  class MyConsumerHandler(val readLimiter: SemaphoreLimitedExecution) extends ConsumerHandler[PrefetchMsg] {

    override def handle(message: PrefetchMsg, key: String): Future[Option[GenericHandlerReturn]] = {
      handlerReturnSuccess
    }
  }

  val handler = new MyConsumerHandler(readLimiter)

  case class Message(id: String) extends EnvelopeWithId

  val message = Message("1")

  "The postHandleProcess method" should
    "do some success stuff" in {

    val hc = handlerCount
    val testSupervisorMsg = SupervisorMsg(id = doc._id.toHexString)
    implicit val asdfx = SupervisorMsg.msgFormatter

    val emptySeq: Seq[MessageProperty] = Seq()
    val supervisorStub = stub[Sendable[SupervisorMsg]]
    (supervisorStub.send _).when(testSupervisorMsg, emptySeq).returns(())

    Await.result(
      handler.postHandleProcess(message, handlerReturnSuccess, Option(supervisorStub), flagContext, Option(collection)),
      Duration.Inf
    )

    prometheusCollectorCalledWithLabel("handler_count", "success") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, emptySeq).once()

  }

  it should "do some failure stuff" in {

    val hc = handlerCount
    val testSupervisorMsg = SupervisorMsg(id = doc._id.toHexString)
    implicit val asdfx = SupervisorMsg.msgFormatter

    val emptySeq: Seq[MessageProperty] = Seq()
    val supervisorStub = stub[Sendable[SupervisorMsg]]
    (supervisorStub.send _).when(testSupervisorMsg, emptySeq).returns(())

    intercept[Exception] {
      Await.result(
        handler.postHandleProcess(message, handlerReturnFailure, None, flagContext, Option(collection)),
        Duration.Inf
      )
    }

    prometheusCollectorCalledWithLabel("handler_count", "unknown_error") shouldBe true
    (supervisorStub.send _).verify(testSupervisorMsg, emptySeq).never()
  }

  private def prometheusCollectorCalledWithLabel(collectorName: String, label: String): Boolean = {
    defaultPrometheusRegistry
      .filteredMetricFamilySamples(util.Set.of(collectorName))
      .asIterator()
      .asScala
      .exists(collector => {
        val collectorSamples = collector.samples.asScala

        collectorSamples
          .head
          .labelValues
          .contains(label)
      })
  }
}
