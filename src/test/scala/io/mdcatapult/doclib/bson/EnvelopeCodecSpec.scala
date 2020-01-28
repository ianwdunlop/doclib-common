package io.mdcatapult.doclib.bson

import io.mdcatapult.doclib.messages.{ArchiveMsg, DoclibMsg, NerMsg, PrefetchMsg}
import io.mdcatapult.doclib.models.Origin
import io.mdcatapult.doclib.models.metadata._
import io.mdcatapult.klein.queue.Envelope

class EnvelopeCodecSpec extends CodecSpec{

  "EnvelopeSpec" should "encode & decode a Prefetch Message" in {

    val original = PrefetchMsg(
      source = "path/to/file",
      origin = Some(List(Origin(scheme="file", hostname=Some("example.com")))),
      tags = Some(List("tag1", "tag2")),
      metadata = Some(List(MetaString("metakey1", "metavalue1"), MetaInt("metakey2", 100))),
      derivative = Some(false)
    )
    val decodedMsg = roundTrip[Envelope](original, new EnvelopeCodec)
    decodedMsg shouldBe a[PrefetchMsg]
    decodedMsg should equal(original)
  }

  it should "encode & decode a Doclib Message" in {
    val original = DoclibMsg(
      id = "5d970056b3e8083540798f90"
    )
    val decodedMsg = roundTrip[Envelope](original, new EnvelopeCodec())
    decodedMsg shouldBe a[DoclibMsg]
    decodedMsg should equal(original)
  }

  it should "encode & decode a Archive Message" in {
    val original = ArchiveMsg(
      id = Some("5d970056b3e8083540798f90"),
      source = Some("path/to/file")
    )
    val decodedMsg = roundTrip[Envelope](original, new EnvelopeCodec())
    decodedMsg shouldBe a[ArchiveMsg]
    decodedMsg should equal(original)
  }

  it should "encode & decode a Ner Message" in {
    val original = NerMsg(
      id = "5d970056b3e8083540798f90",
      requires = Some(List("cheese"))
    )
    val decodedMsg = roundTrip[Envelope](original, new EnvelopeCodec())
    decodedMsg shouldBe a[NerMsg]
    decodedMsg should equal(original)
  }

}
