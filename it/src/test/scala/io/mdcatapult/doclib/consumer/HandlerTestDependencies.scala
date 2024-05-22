/*
 * Copyright 2024 Medicines Discovery Catapult
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mdcatapult.doclib.consumer

import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.codec.MongoCodecs
import io.mdcatapult.doclib.flag.MongoFlagContext
import io.mdcatapult.doclib.messages.SupervisorMsg
import io.mdcatapult.doclib.models.{AppConfig, DoclibDoc}
import io.mdcatapult.klein.mongo.Mongo
import io.mdcatapult.util.concurrency.SemaphoreLimitedExecution
import io.mdcatapult.util.models.Version
import io.mdcatapult.util.time.nowUtc
import io.prometheus.client.CollectorRegistry
import org.apache.pekko.actor.ActorSystem
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoCollection
import play.api.libs.json.Format


trait HandlerTestDependencies {

  implicit val config: Config = ConfigFactory.load()

  implicit val actorSystem: ActorSystem = ActorSystem("Test")

  import actorSystem.dispatcher

  val version: Version = Version.fromConfig(config)

  implicit val appConfig: AppConfig =
    AppConfig(
      config.getString("consumer.name"),
      config.getInt("consumer.concurrency"),
      config.getString("consumer.queue"),
      Option(config.getString("consumer.exchange"))
    )

  val readLimiter: SemaphoreLimitedExecution = SemaphoreLimitedExecution.create(config.getInt("mongo.read-limit"))
  val writeLimiter: SemaphoreLimitedExecution = SemaphoreLimitedExecution.create(config.getInt("mongo.write-limit"))

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
