package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class FragmentOccurrenceSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(FragmentOccurrence(
      entityType = "entityType",
      schema = "schema",
      characterStart = 1,
      characterEnd = 2,
      wordIndex = 3,
    ),
      """{
        |"entityType": "entityType",
        |"entityGroup": null,
        |"schema": "schema",
        |"characterStart": 1,
        |"characterEnd": 2,
        |"wordIndex": 3,
        |"fragment": null,
        |"correctedValue": null,
        |"correctedValueHash": null,
        |"resolvedEntity": null,
        |"resolvedEntityHash": null,
        |"type": "fragment"}""".stripMargin, classOf[FragmentOccurrence])
  }
}
