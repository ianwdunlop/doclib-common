package io.mdcatapult.doclib.consumer
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.amqp.scaladsl.CommittableReadResult
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
