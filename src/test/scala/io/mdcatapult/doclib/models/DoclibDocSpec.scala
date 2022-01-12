package io.mdcatapult.doclib.models

import java.time.LocalDateTime

import io.mdcatapult.doclib.messages.PrefetchMsg
import io.mdcatapult.doclib.models.metadata.{MetaInt, MetaString}
import org.mongodb.scala.bson.ObjectId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DoclibDocSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  val docId: ObjectId = new ObjectId("5d9f0662679b3e75b2781c94")
  val docIdMongo = """{"$oid": "5d9f0662679b3e75b2781c94"}"""

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(DoclibDoc(
        _id = docId,
        source = "/path/to/file.txt",
        hash = "01234567890",
        mimetype = "text/plain",
        created = LocalDateTime.parse("2019-10-01T12:00:00"),
        updated = LocalDateTime.parse("2019-10-01T12:00:01")
    ),
      s"""{
        |"_id": $docIdMongo,
        |"source": "/path/to/file.txt",
        |"hash": "01234567890",
        |"mimetype": "text/plain",
        |"created": {"$$date": 1569931200000},
        |"updated": {"$$date": 1569931201000},
        |"derivative": false,
        |"attrs": null,
        |"doclib": [],
        |"tags": null,
        |"derivatives": null,
        |"origin": null,
        |"metadata": null,
        |"uuid": null,
        |"rogueFile": null
        |}""".stripMargin)
  }

  "String metadata can be added to DoclibDoc and" should "be able to be decoded" in {
    val metadataMap: List[MetaString] = List(MetaString("doi", "10.1101/327015"))
    val prefetchMsg: PrefetchMsg = PrefetchMsg("/a/file/somewhere.pdf", None, Some(List("a-tag")), Some(metadataMap), None)
    val fetchedMetadata = prefetchMsg.metadata
    assert(fetchedMetadata.get.length == 1)
    assert(fetchedMetadata.get.head.getKey == "doi")
    assert(fetchedMetadata.get.head.getValue == "10.1101/327015")
  }

  "Integer metadata can be added to DoclibDoc and" should "be able to be decoded" in {
    val metadataMap: List[MetaInt] = List(MetaInt("a-value", 10))
    val prefetchMsg: PrefetchMsg = PrefetchMsg("/a/file/somewhere.pdf", None, Some(List("a-tag")), Some(metadataMap), None)
    val fetchedMetadata = prefetchMsg.metadata
    assert(fetchedMetadata.get.length == 1)
    assert(fetchedMetadata.get.head.getKey == "a-value")
    assert(fetchedMetadata.get.head.getValue == 10)
  }

  "Mixed types of metadata can be added to DoclibDoc and" should "be able to be decoded" in {
    val metadataMap = List(MetaString("doi", "10.1101/327015"), MetaInt("a-value", 10))
    val prefetchMsg: PrefetchMsg = PrefetchMsg("/a/file/somewhere.pdf", None, Some(List("a-tag")), Some(metadataMap), None)
    val fetchedMetadata = prefetchMsg.metadata
    assert(fetchedMetadata.get.length == 2)
    assert(fetchedMetadata.get.head.getKey == "doi")
    assert(fetchedMetadata.get.head.getValue == "10.1101/327015")
    assert(fetchedMetadata.get(1).getKey == "a-value")
    assert(fetchedMetadata.get(1).getValue == 10)
  }

}
