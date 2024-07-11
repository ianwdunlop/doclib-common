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

package io.doclib.common.consumer

import java.util.UUID.randomUUID

import org.apache.pekko.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import io.doclib.common.IntegrationFixture.longTimeout
import io.doclib.common.codec.MongoCodecs
import io.doclib.common.messages.DoclibMsg
import io.doclib.common.models.MessageDoc
import io.doclib.mongo.Mongo
import org.bson.codecs.configuration.CodecRegistry
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers


class AbstractConsumerSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with Eventually {

  EchoConsumer.main(Array())

  Thread.sleep(10 * 1000)

  implicit private val system: ActorSystem = ActorSystem("ConsumerLeadmineIntegrationTest")
  import system.dispatcher

  implicit private val config: Config = ConfigFactory.load()

  private val collection = {
    implicit val codecs: CodecRegistry = MongoCodecs.get
    val mongo: Mongo = new Mongo()

    mongo.getDatabase(config.getString("mongo.doclib-database")).getCollection[MessageDoc]("echo_test")
  }

  private val queue = EchoConsumer.queue("consumer.queue")

  "An AbstractConsumer" - {
    "when it is set-up to echo a message from rabbit into Mongo" - {
      val message = DoclibMsg(randomUUID().toString)

      val messageSent =
        for {
          _ <- collection.drop().toFuture()
          _ = Thread.sleep(10000)
          _ = queue.send(message)
        } yield true

      "should find that message in Mongo" in {
        eventually(() => {
          val echoes =
            for {
              _ <- messageSent
              echoes <- collection.find().toFuture()
            } yield echoes

          whenReady(echoes, longTimeout) { echoes: Seq[MessageDoc] =>
            echoes should have length 1
            echoes.headOption.map(_.doclib) should contain(message)
          }
        })
      }
    }
  }
}
