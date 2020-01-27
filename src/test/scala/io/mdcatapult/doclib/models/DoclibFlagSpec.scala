package io.mdcatapult.doclib.models

import java.time.LocalDateTime

import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}

class DoclibFlagSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Origin" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(DoclibFlag(
      key = "test",
      version = ConsumerVersion(
        number = "2.0.0",
        major = 2,
        minor = 0,
        patch = 0,
        hash = "01234567890"),
      started = LocalDateTime.parse("2019-10-01T12:00:00"),
      ended = Some(LocalDateTime.parse("2019-10-01T12:00:00")),
      errored = Some(LocalDateTime.parse("2019-10-01T12:00:00")),
      state = Some(DoclibFlagState(value = "12345", updated = LocalDateTime.parse("2019-10-01T12:00:00")))
    ),
      """{
        |"key": "test",
        |"version": {"number": "2.0.0", "major": 2, "minor": 0, "patch": 0, "hash": "01234567890"},
        |"started": {"$date": 1569931200000},
        |"ended": {"$date": 1569931200000},
        |"errored": {"$date": 1569931200000},
        |"state": {"value": "12345", "updated": {"$date": 1569931200000}}
        |}""".stripMargin, classOf[DoclibFlag])

  }

}
