package io.mdcatapult.doclib.models.ner

import java.time.LocalDateTime

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class DocumentOccurrenceSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(DocumentOccurrence(
      entityType = "entityType",
      schema = "schema",
      characterStart = 1,
      characterEnd = 2,
    ),
      """{
        |"entityType": "entityType",
        |"entityGroup": null,
        |"schema": "schema",
        |"characterStart": 1,
        |"characterEnd": 2,
        |"fragment": null,
        |"correctedValue": null,
        |"correctedValueHash": null,
        |"resolvedEntity": null,
        |"type": "document",
        |"resolvedEntityHash": null}""".stripMargin, classOf[DocumentOccurrence])
  }
}
