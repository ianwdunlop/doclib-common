package io.mdcatapult.doclib.models.metadata

import io.mdcatapult.doclib.models.BsonCodecCompatible
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetaBooleanSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(MetaBoolean(
      key = "key",
      value = true
    ),
      """{
        |  "key": "key",
        |  "value": true
        |}""".stripMargin)
  }
}
