package io.mdcatapult.doclib.models

import io.mdcatapult.doclib.models.metadata.MetaString
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DerivativeSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Derivative" can "be encoded and decoded successfully to BSON" in {
    roundTrip(Derivative(
      `type` = "test",
      path = "/path/to/file/txt",
      metadata = Some(List(MetaString("key", "value")))
    ),
      """{
        |"type": "test",
        |"path": "/path/to/file/txt",
        |"metadata": [{
        |   "key": "key",
        |   "value": "value"
        |}]}""".stripMargin)
  }

}
