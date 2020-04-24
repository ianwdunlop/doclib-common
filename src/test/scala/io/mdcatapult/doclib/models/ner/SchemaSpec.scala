package io.mdcatapult.doclib.models.ner

import java.time.LocalDateTime

import io.mdcatapult.doclib.models.BsonCodecCompatible
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SchemaSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

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
        |"lastRun": {"$date": 1569931200000}}""".stripMargin)
  }
}
