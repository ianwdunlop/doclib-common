package io.mdcatapult.doclib.bson

import java.util.UUID

import io.mdcatapult.doclib.models.ner.{DocumentOccurrence, Occurrence}

class NerOccurenceCodecSpec extends CodecSpec{

  "NerOccurenceCodec" should "encode & decode" in {
    val original = DocumentOccurrence(
      _id = UUID.randomUUID(),
      characterStart = 1,
      characterEnd = 2,
      fragment = Some(UUID.randomUUID())
    )
    val decodedDocument = roundTrip[Occurrence](original, new NerOccurrenceCodec())

    decodedDocument shouldBe a[DocumentOccurrence]
    decodedDocument should equal(original)

  }

}
