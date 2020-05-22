package io.mdcatapult.doclib.util

import java.time.LocalDateTime

import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.models.{ConsumerVersion, DoclibDoc, DoclibFlag, DoclibFlagState}
import org.mongodb.scala.bson._
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DoclibFlagsSpec extends AnyFlatSpec with Matchers with MockFactory {

  implicit val config: Config = ConfigFactory.parseString(
    """
      |version {
      |  number = "/test"
      |  major = 0
      |  minor = 0
      |  patch = 1
      |  hash = "test"
      |}
    """.stripMargin)

  private val created: LocalDateTime = nowUtc.now()
  private val earlier: LocalDateTime = created.minusHours(1)
  private val later: LocalDateTime = created.plusHours(1)

  val newDoc: DoclibDoc = DoclibDoc(
    _id = new ObjectId(),
    source = "/path/to/file.txt",
    hash = "0123456789",
    mimetype =  "text/plain",
    created =  created,
    updated =  created,
  )

  val startedDoc: DoclibDoc = newDoc.copy(
    doclib = List(DoclibFlag(
      key = "test",
      version = ConsumerVersion(
        number = "0.0.1",
        major = 0,
        minor = 0,
        patch = 1,
        hash = "1234567890"),
      started = Some(created),
      queued = Some(true)
    ))
  )

  val dupeDoc: DoclibDoc = newDoc.copy(
    doclib = List(
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = Some(created)
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.1",
          major = 0,
          minor = 0,
          patch = 1,
          hash = "1234567891"),
        started = Some(later)
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = Some(earlier)
      )
    )
  )


  "A 'started' document" should "return true when testing for the flag" in {
    assert(startedDoc.hasFlag("test"))
  }

  it should "get a valid flag" in {
    val flag = startedDoc.getFlag("test")
    assert(flag.length == 1)
    assert(flag.head.started.get == created)
  }

  it should "fail to get an invalid flag" in {
    val flag = startedDoc.getFlag("dummy")
    assert(flag.isEmpty)
  }

  it can "have a flag state" in {
    val stateDoc: DoclibDoc = newDoc.copy(
      doclib = List(DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.1",
          major = 0,
          minor = 0,
          patch = 1,
          hash = "1234567890"),
        started = Some(created),
        state = Some(DoclibFlagState(value = "12345", updated = created))
      ))
    )
    assert(stateDoc.getFlag("test").head.state.get.value == "12345")
    assert(stateDoc.getFlag("test").head.state.get.updated == created)
  }

  it can "have a reset property" in {
    val resetDoc: DoclibDoc = newDoc.copy(
      doclib = List(
        DoclibFlag(
          key = "test",
          version = ConsumerVersion(
            number = "0.0.2",
            major = 0,
            minor = 0,
            patch = 2,
            hash = "1234567890"),
          started = Some(created),
          reset = Some(created)
        )
      )
    )
    assert(resetDoc.getFlag("test").head.reset.get == created)
  }

  it can "have a summary state property" in {
    val resetDoc: DoclibDoc = newDoc.copy(
      doclib = List(
        DoclibFlag(
          key = "test",
          version = ConsumerVersion(
            number = "0.0.2",
            major = 0,
            minor = 0,
            patch = 2,
            hash = "1234567890"),
          started = Some(created),
          summary = Some("started")
        )
      )
    )
    assert(resetDoc.getFlag("test").head.summary.get == "started")
  }

  "A queued flag" should "return true when testing for queued" in {
    assert(startedDoc.getFlag("test").head.isQueued)
  }

}
