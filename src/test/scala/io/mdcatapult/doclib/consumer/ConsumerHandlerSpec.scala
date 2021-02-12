package io.mdcatapult.doclib.consumer
import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.messages.PrefetchMsg
import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.util.time.nowUtc
import org.bson.types.ObjectId
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsumerHandlerSpec extends AnyFlatSpecLike {

  implicit val config: Config = ConfigFactory.parseString(
    """
      |consumer {
      |  name = dummy
      |  concurrency: 1
      |  queue: none
      |}
      |version {
      |   number: 1
      |   major: 1
      |   minor: 1
      |   patch: 1
      |   hash: 1
      |}
    """.stripMargin)

  case class ExampleReturn(doclibDoc: DoclibDoc, pathsOpt: Option[List[String]]) extends HandlerReturn

  class MyConsumerHandler extends ConsumerHandler[PrefetchMsg] {

    override def handle(message: PrefetchMsg, key: String): Future[Option[ExampleReturn]] = {
      val doc = DoclibDoc(
        _id = new ObjectId(),
        source = message.source,
        hash = "12345",
        created = nowUtc.now(),
        updated = nowUtc.now(),
        mimetype = "text/plain")

      Future {
        Some(ExampleReturn(doc, None))
      }
    }
  }

  "A handler" can "extend the ConsumerHandler with a custom Envelope" in {
    val myHandler = new MyConsumerHandler()
    val prefetchMsg = PrefetchMsg("a-source")
    whenReady(myHandler.handle(prefetchMsg, "a-key"), Timeout(Span(20, Seconds))) { result =>
      assert(result.get.doclibDoc.source == "a-source")
    }
  }



}
