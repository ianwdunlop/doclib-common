package io.mdcatapult.doclib.models.ner

import java.util.UUID

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class NerDocumentSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "NerDocument" can "be encoded and decoded successfully to BSON" in {
    val uuid = UUID.fromString("dc83cac6-4daa-4a0b-8e52-df1543af1e8f")
    roundTrip(NerDocument(
      _id =  uuid,
      value = "value",
      hash = "01234567890",
      entityType = Some("entity-type"),
      entityGroup = Some("entity-group"),
      resolvedEntity = Some("resolved-entity"),
      resolvedEntityHash = Some("resolved-entity-hash"),
      document = new ObjectId("5d9f0662679b3e75b2781c94"),
    ),
      """{
        |"_id": {"$binary": "3IPKxk2qSguOUt8VQ68ejw==", "$type": "04"},
        |"value": "value",
        |"hash": "01234567890",
        |"document": {"$oid": "5d9f0662679b3e75b2781c94"},
        |"entityType": "entity-type",
        |"entityGroup": "entity-group",
        |"resolvedEntity": "resolved-entity",
        |"resolvedEntityHash": "resolved-entity-hash",
        |"schema": null}""".stripMargin, classOf[NerDocument])
  }
}
