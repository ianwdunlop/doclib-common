package io.mdcatapult.doclib.models.metadata

import java.time.LocalDateTime

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class MetaDateTimeSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(MetaDateTime(
      key = "key",
      value = LocalDateTime.parse("2019-10-01T12:00:00")
    ),
      """{
        |  "key": "key",
        |  "value": {"$date": 1569931200000}
        |}""".stripMargin, classOf[MetaDateTime])
  }
}
