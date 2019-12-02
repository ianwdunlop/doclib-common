package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class NerDocumentSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "NerDocument" can "be encoded and decoded successfully to BSON" in {
    roundTrip(NerDocument(
      _id =  new ObjectId("5d9f0662679b3e75b2781c93"),
      value = "value",
      hash = "01234567890",
      total = 2,
      document = new ObjectId("5d9f0662679b3e75b2781c94"),
    ),
      """{
        |"_id": {"$oid": "5d9f0662679b3e75b2781c93"},
        |"value": "value",
        |"hash": "01234567890",
        |"total": 2,
        |"document": {"$oid": "5d9f0662679b3e75b2781c94"},
        |"fragment": null,
        |"occurrences": null,
        |"schemas": null,
        |"type": "document"}""".stripMargin, classOf[NerDocument])
  }
}
