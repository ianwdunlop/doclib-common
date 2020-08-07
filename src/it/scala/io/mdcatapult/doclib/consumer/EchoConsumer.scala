package io.mdcatapult.doclib.consumer
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.spingo.op_rabbit.SubscriptionRef
import io.mdcatapult.doclib.messages.DoclibMsg
import io.mdcatapult.doclib.models.MessageDoc
import io.mdcatapult.klein.mongo.Mongo
import io.mdcatapult.klein.queue.Queue
import org.mongodb.scala.bson.ObjectId

object EchoConsumer extends AbstractConsumer(name = "echo") {

  override def start()(implicit as: ActorSystem, m: Materializer, mongo: Mongo): SubscriptionRef = {

    val upstream: Queue[DoclibMsg] = queue("upstream.queue")
    val collection = mongo.database.getCollection[MessageDoc]("echo_test")

    upstream.subscribe(
      (message: DoclibMsg, _: String) => {
        collection.insertOne(MessageDoc(new ObjectId(), message)).toFuture()
      },
      concurrent = 1,
    )
  }
}
