package io.mdcatapult.doclib.models.metadata

import io.mdcatapult.doclib.models.BsonCodecCompatible
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetaDoubleSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(MetaDouble(
      key = "key",
      value = 2.0
    ),
      """{
        |  "key": "key",
        |  "value": 2.0
        |}""".stripMargin)
  }
}
