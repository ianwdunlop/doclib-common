package io.mdcatapult.doclib.bson

import io.mdcatapult.doclib.models.ner.{DocumentOccurrence, Occurrence}
import org.mongodb.scala.bson.ObjectId

class NerOccurenceCodecSpec extends CodecSpec{

  "NerOccurenceCodec" should "encode & decode" in {

    val original = DocumentOccurrence(
      entityType = "entityTypeValue",
      schema = "schemaValue",
      characterStart = 1,
      characterEnd = 2,
      fragment = Some(new ObjectId)
    )
    val decodedDocument = roundTrip[Occurrence](original, new NerOccurrenceCodec())

    decodedDocument shouldBe a[DocumentOccurrence]
    decodedDocument should equal(original)

  }

}
