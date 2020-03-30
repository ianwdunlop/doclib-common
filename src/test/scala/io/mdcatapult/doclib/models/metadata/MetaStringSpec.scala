package io.mdcatapult.doclib.models.metadata

import io.mdcatapult.doclib.models.{BsonCodecCompatible, Derivative}
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetaStringSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(MetaString(
      key = "key",
      value = "value"
    ),
      """{
        |  "key": "key",
        |  "value": "value"
        |}""".stripMargin, classOf[Derivative])
  }
}
