package io.mdcatapult.doclib.models

import io.mdcatapult.doclib.models.metadata.MetaString
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}

class DerivativeSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

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
        |}]}""".stripMargin, classOf[Derivative])
  }

}
