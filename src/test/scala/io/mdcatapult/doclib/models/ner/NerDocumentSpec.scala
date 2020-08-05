package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.models.ner.Fixture._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NerDocumentSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "NerDocument" can "be encoded and decoded successfully to BSON" in {
    roundTrip(NerDocument(
      _id =  uuid,
      value = "value",
      hash = "01234567890",
      entityType = Some("entity-type"),
      entityGroup = Some("entity-group"),
      resolvedEntity = Some("resolved-entity"),
      resolvedEntityHash = Some("resolved-entity-hash"),
      document = docId,
    ),
      s"""{
        |"_id": $uuidMongoBinary,
        |"value": "value",
        |"hash": "01234567890",
        |"document": $docIdMongo,
        |"entityType": "entity-type",
        |"entityGroup": "entity-group",
        |"resolvedEntity": "resolved-entity",
        |"resolvedEntityHash": "resolved-entity-hash",
        |"schema": null}""".stripMargin)
  }
}
