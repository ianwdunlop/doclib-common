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
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.connectors.amqp.scaladsl.CommittableReadResult
import io.mdcatapult.doclib.messages.DoclibMsg
import io.mdcatapult.doclib.models.{DoclibDoc, MessageDoc}
import io.mdcatapult.klein.mongo.Mongo
import io.mdcatapult.klein.queue.Queue
import org.mongodb.scala.bson.ObjectId
import play.api.libs.json.{JsValue, Json}

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

case class GenericHandlerResult(doclibDoc: DoclibDoc) extends HandlerResult

object EchoConsumer extends AbstractConsumer[DoclibMsg, HandlerResult] {

   override def start()(implicit as: ActorSystem, m: Materializer, mongo: Mongo) = {

    val createdInstant = LocalDateTime.now().toInstant(ZoneOffset.UTC)
    val createdTime = LocalDateTime.ofInstant(createdInstant, ZoneOffset.UTC)

    val upstream: Queue[DoclibMsg, HandlerResult] = queue("consumer.queue")
    val collection = mongo.getDatabase(config.getString("mongo.doclib-database")).getCollection[MessageDoc]("echo_test")
    // Try to insert a document into mongo. Return the original message plus the HandleResult containing the inserted doc wrapped in a future
    val businessLogic: CommittableReadResult => Future[(CommittableReadResult, Try[HandlerResult])] = { committableReadResult =>
      val jsonString: JsValue = Json.parse(committableReadResult.message.bytes.utf8String)

      collection.insertOne(MessageDoc(new ObjectId(), Json.fromJson[DoclibMsg](jsonString).get)).toFuture()
      .map {
        res => {
          val id = res.getInsertedId.asObjectId().getValue
          Success(
            GenericHandlerResult(
              DoclibDoc(
                _id = id,
                source = "",
                hash = "",
                created = createdTime,
                updated = createdTime,
                mimetype = "",
                tags = Some(List[String]()),
                uuid = Some(UUID.randomUUID())
              )
            )
          )
        }
      }.recover {
        e => Failure(e)
      }.map(
        result => (committableReadResult, result)
      )
    }
    upstream.subscribe(
      businessLogic
    )
  }
}
