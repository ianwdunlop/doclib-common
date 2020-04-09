package io.mdcatapult.doclib.models

import java.util.UUID

import io.mdcatapult.doclib.models.metadata.{MetaInt, MetaString}
import org.bson.types.ObjectId
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParentChildSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Derivative" can "be encoded and decoded successfully to BSON" in {
    val id = UUID.fromString("8f5a687c-79a3-11ea-8558-cf48645c5849")
    val parentId = new ObjectId("5d970056b3e8083540798f90")
    val childId = new ObjectId("5d970056b3e8083540798f94")
    val metadataMap = List(MetaString("doi", "10.1101/327015"), MetaInt("a-value", 10))

    roundTrip(ParentChildMapping(
      _id = id,
      parent = parentId,
      child = Some(childId),
      childPath = "/a/path/to/child",
      metadata = Some(metadataMap)
    ),
      """{
        |"_id": {"$binary": "j1pofHmjEeqFWM9IZFxYSQ==", "$type": "04"},
        |"parent": {"$oid": "5d970056b3e8083540798f90"},
        |"child": {"$oid": "5d970056b3e8083540798f94"},
        |"childPath": "/a/path/to/child",
        |"metadata": [{"key": "doi", "value": "10.1101/327015"}, {"key": "a-value", "value": 10}]
        |}""".stripMargin, classOf[ParentChildMapping])
  }

}