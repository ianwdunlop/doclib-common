package io.mdcatapult.doclib.models

import io.mdcatapult.doclib.models.metadata.{MetaInt, MetaString}
import io.mdcatapult.doclib.models.ner.Fixture._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParentChildSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Derivative" can "be encoded and decoded successfully to BSON" in {
    val id = uuid
    val parentId = uuid
    val childId = childDocUuid
    val metadataMap = List(MetaString("doi", "10.1101/327015"), MetaInt("a-value", 10))

    roundTrip(ParentChildMapping(
      _id = id,
      parent = parentId,
      child = Some(childId),
      childPath = "/a/path/to/child",
      metadata = Some(metadataMap),
      consumer = Some("consumer")
    ),
      s"""{
        |"_id": $uuidMongoBinary,
        |"parent": $uuidMongoBinary,
        |"child": $childDocUuidMongoBinary,
        |"childPath": "/a/path/to/child",
        |"metadata": [{"key": "doi", "value": "10.1101/327015"}, {"key": "a-value", "value": 10}],
        |"consumer": "consumer"
        |}""".stripMargin)
  }

}