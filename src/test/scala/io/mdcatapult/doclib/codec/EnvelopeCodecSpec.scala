package io.mdcatapult.doclib.codec

import io.mdcatapult.doclib.messages.{ArchiveMsg, DoclibMsg, EmptyMsg, NerMsg, PrefetchMsg, SupervisorMsg}
import io.mdcatapult.doclib.models.Origin
import io.mdcatapult.doclib.models.metadata._
import io.mdcatapult.klein.queue.Envelope

class EnvelopeCodecSpec extends CodecSpec{

  "EnvelopeSpec" should "encode & decode a Prefetch Message" in {
    val original = PrefetchMsg(
      source = "path/to/file",
      origins = Some(List(Origin(scheme="file", hostname=Some("example.com")))),
      tags = Some(List("tag1", "tag2")),
      metadata = Some(List(MetaString("metakey1", "metavalue1"), MetaInt("metakey2", 100))),
      derivative = Some(false)
    )
    val decodedMsg = roundTrip[Envelope](original, new EnvelopeCodec)
    decodedMsg shouldBe a[PrefetchMsg]
    decodedMsg should equal(original)
  }

  it should "return a JSON string representation of a Doclib Message" in {
    val doclibMsg = DoclibMsg(
      id = "5d970056b3e8083540798f90"
    )
    doclibMsg.toJsonString() should be("{\"id\":\"5d970056b3e8083540798f90\"}")
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

  it should "encode & decode a Supervisor Message" in {
    val original = SupervisorMsg(
      id = "5d970056b3e8083540798f90",
      reset = Some(List("cheese"))
    )
    val decodedMsg = roundTrip[Envelope](original, new EnvelopeCodec())
    decodedMsg shouldBe a[SupervisorMsg]
    decodedMsg should equal(original)
  }

  it should "encode & decode an Empty Message" in {
    val original = EmptyMsg()
    val decodedMsg = roundTrip[Envelope](original, new EnvelopeCodec())
    decodedMsg shouldBe a[EmptyMsg]
    decodedMsg should equal(original)
  }

}
