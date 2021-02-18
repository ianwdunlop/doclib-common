package io.mdcatapult.doclib.consumer

import akka.actor.ActorSystem
import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.messages.{PrefetchMsg, SupervisorMsg}
import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.klein.queue.{EnvelopeWithId, Sendable}
import io.mdcatapult.util.time.nowUtc
import org.bson.types.ObjectId
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future

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

  case class TestMessage(id: String) extends EnvelopeWithId

  val postHandleMessage: TestMessage = TestMessage(testDoclibDoc._id.toHexString)

  val pathsOpt: Option[List[String]] = Option(List("a/cool/path", "some/other/path"))
  val testSupervisorMsg: SupervisorMsg = SupervisorMsg(id = testDoclibDoc._id.toHexString)
  val supervisorStub: Sendable[SupervisorMsg] = stub[Sendable[SupervisorMsg]]

  val handlerResultSuccess: Future[Option[GenericHandlerResult]] =
    Future(Option(GenericHandlerResult(testDoclibDoc, pathsOpt)))

  val handlerResultFailure: Future[Option[GenericHandlerResult]] =
    Future(Option(throw new Exception("error")))

  val handlerResultDoclibExceptionFailure: Future[Option[GenericHandlerResult]] =
    Future(Option(throw new DoclibDocException(testDoclibDoc, "oh dear")))

  val handlerResultEmptySuccess: Future[Option[GenericHandlerResult]] = Future(None)
}
