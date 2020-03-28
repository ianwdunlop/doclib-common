package io.mdcatapult.doclib.util

import java.time.LocalDateTime

import com.mongodb.async.client.{MongoCollection => JMongoCollection}
import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.models.{ConsumerVersion, DoclibDoc, DoclibFlag, DoclibFlagState}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson._
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class DoclibFlagsSpec extends FlatSpec with Matchers with MockFactory {

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


  val wrappedCollection: JMongoCollection[DoclibDoc] = mock[JMongoCollection[DoclibDoc]]
  implicit val collection: MongoCollection[DoclibDoc] = MongoCollection[DoclibDoc](wrappedCollection)

  val codecs: CodecRegistry = MongoCodecs.get
  val now: LocalDateTime = LocalDateTime.now()
  val earlier: LocalDateTime = now.minusHours(1)
  val later: LocalDateTime = now.plusHours(1)

  val newDoc: DoclibDoc = DoclibDoc(
    _id = new ObjectId,
    source = "/path/to/file.txt",
    hash = "0123456789",
    mimetype =  "text/plain",
    created =  now,
    updated =  now,
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
      started = now
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
        started = now
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.1",
          major = 0,
          minor = 0,
          patch = 1,
          hash = "1234567891"),
        started = later
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = earlier
      )
    )
  )


  "A 'started' document" should "return true when testing for the flag" in {
    assert(startedDoc.hasFlag("test"))
  }

  it should "get a valid flag" in {
    val flag = startedDoc.getFlag("test")
    assert(flag.length == 1)
    assert(flag.head.started == now)
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
        started = now,
        state = Some(DoclibFlagState(value = "12345", updated = now))
      ))
    )
    assert(stateDoc.getFlag("test").head.state.get.value == "12345")
    assert(stateDoc.getFlag("test").head.state.get.updated == now)
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
          started = now,
          reset = Some(now)
        )
      )
    )
    assert(resetDoc.getFlag("test").head.reset.get == now)
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
          started = now,
          summary = Some("started")
        )
      )
    )
    assert(resetDoc.getFlag("test").head.summary.get == "started")
  }

}
