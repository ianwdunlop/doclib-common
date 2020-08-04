package io.mdcatapult.doclib.models.document

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.models.ner.Fixture.{docId, docIdMongo, uuid, uuidMongoBinary}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TextFragmentSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(TextFragment(
      _id = uuid,
      document = docId,
      index = 1,
      startAt = 2,
      endAt = 3,
      length = 1
    ),
      s"""{
        |"_id": $uuidMongoBinary,
        |"document": $docIdMongo,
        |"index": 1,
        |"startAt": 2,
        |"endAt": 3,
        |"length": 1,
        |}""".stripMargin)
  }
}
