package io.mdcatapult.doclib.models.document

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class TextFragmentSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(TextFragment(
      _id = new ObjectId("5d9f0662679b3e75b2781c93"),
      document = new ObjectId("5d9f0662679b3e75b2781c94"),
      index = 1,
      startAt = 2,
      endAt = 3,
      length = 1
    ),
      """{
        |"_id": {"$oid": "5d9f0662679b3e75b2781c93"},
        |"document": {"$oid": "5d9f0662679b3e75b2781c94"},
        |"index": 1,
        |"startAt": 2,
        |"endAt": 3,
        |"length": 1,
        |}""".stripMargin, classOf[TextFragment])
  }
}
