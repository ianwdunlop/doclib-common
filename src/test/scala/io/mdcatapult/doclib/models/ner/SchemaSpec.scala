package io.mdcatapult.doclib.models.ner

import java.time.LocalDateTime

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class SchemaSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(Schema(
      key = "keyValue",
      tool = "toolValue",
      config = "configValue",
      version = "versionValue",
      lastRun = LocalDateTime.parse("2019-10-01T12:00:00")
    ),
      """{
        |"key": "keyValue",
        |"tool": "toolValue",
        |"config": "configValue",
        |"version": "versionValue",
        |"lastRun": {"$date": 1569931200000}}""".stripMargin, classOf[Schema])
  }
}
