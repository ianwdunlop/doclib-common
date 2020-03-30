package io.mdcatapult.doclib.models.metadata

import io.mdcatapult.doclib.models.{BsonCodecCompatible, Derivative}
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetaBooleanSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(MetaBoolean(
      key = "key",
      value = true
    ),
      """{
        |  "key": "key",
        |  "value": true
        |}""".stripMargin, classOf[Derivative])
  }
}
