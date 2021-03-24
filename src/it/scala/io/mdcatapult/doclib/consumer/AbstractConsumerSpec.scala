package io.mdcatapult.doclib.consumer

import java.util.UUID.randomUUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.IntegrationFixture.longTimeout
import io.mdcatapult.doclib.codec.MongoCodecs
import io.mdcatapult.doclib.messages.DoclibMsg
import io.mdcatapult.doclib.models.MessageDoc
import io.mdcatapult.klein.mongo.Mongo
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

  private val queue = EchoConsumer.queue[DoclibMsg]("consumer.queue")

  "An AbstractConsumer" - {
    "when it is set-up to echo a message from rabbit into Mongo" - {
      val message = DoclibMsg(randomUUID().toString)

      val messageSent =
        for {
          _ <- collection.drop().toFuture()
          _ = EchoConsumer.waitForInitialisation(10, TimeUnit.SECONDS)
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
