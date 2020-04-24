package io.mdcatapult.doclib.models.document

import java.util.UUID

import io.mdcatapult.doclib.models.BsonCodecCompatible
import org.mongodb.scala.bson.ObjectId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TextFragmentSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  private val uuid = UUID.fromString("dc83cac6-4daa-4a0b-8e52-df1543af1e8f")

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(TextFragment(
      _id = uuid,
      document = new ObjectId("5d9f0662679b3e75b2781c94"),
      index = 1,
      startAt = 2,
      endAt = 3,
      length = 1
    ),
      """{
        |"_id": {"$binary": "3IPKxk2qSguOUt8VQ68ejw==", "$type": "04"},
        |"document": {"$oid": "5d9f0662679b3e75b2781c94"},
        |"index": 1,
        |"startAt": 2,
        |"endAt": 3,
        |"length": 1,
        |}""".stripMargin)
  }
}
