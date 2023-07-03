package io.mdcatapult.doclib.consumer

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.amqp.ReadResult
import akka.stream.alpakka.amqp.scaladsl.CommittableReadResult
import akka.util.ByteString
import com.rabbitmq.client.AMQP.BasicProperties
import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.messages.{PrefetchMsg, SupervisorMsg}
import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.klein.queue.{Envelope, Sendable}
import io.mdcatapult.util.time.nowUtc
import org.bson.types.ObjectId
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{Format, Json}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Implemented as a trait over an object, as we need some test data to throw an exception inside a future,
  * which needs the actor system's execution context that is used throughout the test and handler test dependencies
  */
trait HandlerTestData extends MockFactory {

  implicit val actorSystem: ActorSystem

  import actorSystem.dispatcher

  val prefetchMsg: PrefetchMsg = PrefetchMsg("a-source")

  val testDoclibDoc: DoclibDoc = DoclibDoc(
    _id = new ObjectId(),
    source = prefetchMsg.source,
    hash = "12345",
    created = nowUtc.now(),
    updated = nowUtc.now(),
    mimetype = "text/plain"
  )


  case class GenericHandlerResult(doclibDoc: DoclibDoc) extends HandlerResult

  case class GenericCommittableReadResult(msg: String) extends CommittableReadResult {
    override val message: ReadResult = ReadResult(ByteString.apply(""), new com.rabbitmq.client.Envelope(1234, true , "", ""), new BasicProperties.Builder().build())

    override def ack(multiple: Boolean): Future[Done] = {
      Future(Done)
    }

    override def nack(multiple: Boolean, requeue: Boolean): Future[Done] = {
      Future(Done)
    }
  }

  object TestMessage {
    implicit val msgFormatter: Format[TestMessage] = Json.format[TestMessage]

  }
  case class TestMessage(id: String) extends Envelope {
    override def toJsonString(): String = Json.toJson(this).toString()
  }

  val postHandleMessage: TestMessage = TestMessage(testDoclibDoc._id.toHexString)

  val testSupervisorMsg: SupervisorMsg = SupervisorMsg(id = testDoclibDoc._id.toHexString)
  val supervisorStub: Sendable[SupervisorMsg] = stub[Sendable[SupervisorMsg]]

  val handlerResultSuccess: Future[(CommittableReadResult, Try[GenericHandlerResult])] =
    Future((GenericCommittableReadResult("hello"), Success(GenericHandlerResult(testDoclibDoc))))

  val handlerResultFailure: Future[(CommittableReadResult, Try[GenericHandlerResult])] =
    Future((GenericCommittableReadResult("hello"), Failure(new Exception("error"))))

  val handlerResultDoclibExceptionFailure: Future[(CommittableReadResult, Try[GenericHandlerResult])] =
    Future((GenericCommittableReadResult("hello"), Failure(new DoclibDocException(testDoclibDoc, "oh dear"))))

}
