package io.mdcatapult.doclib.consumer

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.codec.MongoCodecs
import io.mdcatapult.doclib.flag.MongoFlagContext
import io.mdcatapult.doclib.messages.{DoclibMsg, SupervisorMsg}
import io.mdcatapult.doclib.models.{ConsumerConfig, DoclibDoc}
import io.mdcatapult.klein.mongo.Mongo
import io.mdcatapult.klein.queue.Sendable
import io.mdcatapult.util.concurrency.SemaphoreLimitedExecution
import io.mdcatapult.util.models.Version
import io.mdcatapult.util.time.nowUtc
import io.prometheus.client.CollectorRegistry
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoCollection
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Format


trait HandlerTestDependencies extends MockFactory {

  implicit val config: Config = ConfigFactory.load()

  implicit val actorSystem: ActorSystem = ActorSystem("Test")

  import actorSystem.dispatcher

  val version: Version = Version.fromConfig(config)

  implicit val consumerNameAndQueue: ConsumerConfig =
    ConsumerConfig(
      config.getString("consumer.name"),
      config.getInt("consumer.concurrency"),
      config.getString("consumer.queue"),
      config.getString("consumer.exchange")
    )

  val readLimiter: SemaphoreLimitedExecution = SemaphoreLimitedExecution.create(config.getInt("mongo.read-limit"))
  val writeLimiter: SemaphoreLimitedExecution = SemaphoreLimitedExecution.create(config.getInt("mongo.write-limit"))

  val downstream: Sendable[DoclibMsg] = stub[Sendable[DoclibMsg]]
  val archiver: Sendable[DoclibMsg] = stub[Sendable[DoclibMsg]]

  implicit val formatter: Format[SupervisorMsg] = SupervisorMsg.msgFormatter

  implicit val codecs: CodecRegistry = MongoCodecs.get

  val mongo: Mongo = new Mongo()
  implicit val collection: MongoCollection[DoclibDoc] =
    mongo.getCollection(
      databaseName = config.getString("mongo.doclib-database"),
      collectionName = config.getString("mongo.documents-collection")
    )

  val mongoFlagContext = new MongoFlagContext("", version, collection, nowUtc)

  val defaultPrometheusRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
}
