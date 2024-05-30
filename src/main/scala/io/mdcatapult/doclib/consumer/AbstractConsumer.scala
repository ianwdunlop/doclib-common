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

import java.io.File

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import com.typesafe.scalalogging.LazyLogging
import io.mdcatapult.doclib.codec.MongoCodecs
import io.mdcatapult.doclib.util.sanitiseName
import io.mdcatapult.util.models.Version
import io.mdcatapult.klein.mongo.Mongo
import io.mdcatapult.klein.queue.{Envelope, Queue}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import scopt.OParser
import scala.concurrent.ExecutionContext.Implicits.global

abstract class AbstractConsumer[T <: Envelope, M <: HandlerResult](codecProviders: Seq[CodecProvider] = Nil) extends App with LazyLogging {

  private case class ConsumerConfig(config: Config = ConfigFactory.load())

  private def parseArgsWithConfiguration(): ConsumerConfig = {
    val optBuilder = OParser.builder[ConsumerConfig]
    val optParser: OParser[Unit, ConsumerConfig] = {
      import optBuilder._
      OParser.sequence(
        programName("consumer"),
        opt[String]('c', "config")
          .action((x, c) => c.copy(config = ConfigFactory.parseFile(new File(x)).withFallback(c.config)))
          .text("optional: path to additional config for the consumer")
      )
    }
    val consumerConfig = ConsumerConfig()
    OParser.parse(optParser, args, consumerConfig) match {
      case Some(c: ConsumerConfig) => c
      case None => sys.exit(1)
    }
  }

  private val consumerVersion: Version = Version.fromConfig(ConfigFactory.load("version"))
  logger.info(s"Version ${consumerVersion}")

  private val optConfig: ConsumerConfig = parseArgsWithConfiguration()

  implicit val config: Config = optConfig.config

  logger.debug(config.root().render(ConfigRenderOptions.concise()))

  /**
   * Send messages T and subscribe to response M
   * @param property
   * @param s implicit Akka actor
   * @tparam T Envelop containing message that is sent to the queue
   * @tparam M HandlerResult containing response from the queue
   * @return
   */
  def queue(property: String)(implicit s: ActorSystem): Queue[T, M] = {
    val consumerName = config.getString("consumer.name")
    new Queue[T, M](config.getString(property), consumerName = Option(consumerName))
  }

  def start()(implicit as: ActorSystem, m: Materializer, mongo: Mongo): Unit

  private val sanitisedName = sanitiseName(config.getString("consumer.name"))
  val system: ActorSystem = ActorSystem(sanitisedName)
  val m: Materializer = Materializer(system)
  import system.dispatcher

  // Initialise Mongo
  implicit val codecs: CodecRegistry = MongoCodecs.include(codecProviders)
  val mongo: Mongo = new Mongo()
  start()(system, m, mongo)
}
