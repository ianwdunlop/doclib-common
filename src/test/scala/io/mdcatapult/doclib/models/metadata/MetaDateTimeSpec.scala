package io.mdcatapult.doclib.models.metadata

import java.time.LocalDateTime

import io.mdcatapult.doclib.models.BsonCodecCompatible
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetaDateTimeSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(MetaDateTime(
      key = "key",
      value = LocalDateTime.parse("2019-10-01T12:00:00")
    ),
      """{
        |  "key": "key",
        |  "value": {"$date": 1569931200000}
        |}""".stripMargin)
  }
}
