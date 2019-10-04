package io.mdcatapult.doclib.models

import java.time.LocalDateTime

import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}

class DoclibDocSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(DoclibDoc(
        _id = new ObjectId("5d970056b3e8083540798f90"),
        source = "/path/to/file.txt",
        hash = "01234567890",
        mimetype = "text/plain",
        created = LocalDateTime.parse("2019-10-01T12:00:00"),
        updated = LocalDateTime.parse("2019-10-01T12:00:01")
    ),
      """{
        |"_id": {"$oid": "5d970056b3e8083540798f90"},
        |"source": "/path/to/file.txt",
        |"hash": "01234567890",
        |"mimetype": "text/plain",
        |"created": {"$date": 1569931200000},
        |"updated": {"$date": 1569931201000},
        |"derivative": false,
        |"attrs": null,
        |"doclib": [],
        |"tags": null,
        |"derivatives": null,
        |"origin": null,
        |"metadata": null
        |}""".stripMargin, classOf[DoclibDoc])

  }

}
