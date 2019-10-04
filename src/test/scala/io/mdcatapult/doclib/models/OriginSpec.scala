package io.mdcatapult.doclib.models

import java.time.LocalDateTime

import io.lemonlabs.uri.Uri
import io.mdcatapult.doclib.models.metadata.MetaString
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}

class OriginSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Origin" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(Origin(
      scheme = "https",
      uri = Some(Uri.parse("https://new.bbc.co.uk")),
      metadata = Some(List(MetaString("key", "value"))),
      headers = None
    ),
      """{
        |"scheme": "https",
        |"uri": "https://new.bbc.co.uk",
        |"headers": null,
        |"metadata": [{
        |   "key": "key",
        |   "value": "value"
        |}]}""".stripMargin, classOf[Origin])

  }

}
