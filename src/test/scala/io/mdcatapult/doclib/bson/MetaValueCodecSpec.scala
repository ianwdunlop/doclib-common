package io.mdcatapult.doclib.bson

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.mdcatapult.doclib.models.metadata._

class MetaValueCodecSpec extends CodecSpec{

  val codec = new MetaValueCodec()

  "MetaValueCodec" can "encode & decode MetaString" in {
    val original = MetaString("theKey", "theValue")
    val decodedDocument = roundTrip[MetaValueUntyped](original, codec)

    decodedDocument shouldBe a[MetaString]
    decodedDocument should equal(original)
  }
  it can "encode & decode MetaBoolean" in {
    val original = MetaBoolean("theKey", value = true)
    val decodedDocument = roundTrip[MetaValueUntyped](original, codec)

    decodedDocument shouldBe a[MetaBoolean]
    decodedDocument should equal(original)
  }
  it can "encode & decode MetaDouble" in {
    val original = MetaDouble("theKey", 2.0)
    val decodedDocument = roundTrip[MetaValueUntyped](original, codec)

    decodedDocument shouldBe a[MetaDouble]
    decodedDocument should equal(original)
  }
  it can "encode & decode MetaInt" in {
    val original = MetaInt("theKey", 1)
    val decodedDocument = roundTrip[MetaValueUntyped](original, codec)

    decodedDocument shouldBe a[MetaInt]
    decodedDocument should equal(original)
  }
  it can "encode & decode MetaDateTime" in {
    val now = LocalDateTime.now()
    val original = MetaDateTime("theKey", now)
    val decodedDocument = roundTrip[MetaValueUntyped](original, codec)

    decodedDocument shouldBe a[MetaDateTime]
    // compares formatted date time as translation to BSON datetime loses precision
    val retrieved = decodedDocument.asInstanceOf[MetaDateTime].value
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    retrieved.format(formatter) should equal(now.format(formatter))

  }
}
