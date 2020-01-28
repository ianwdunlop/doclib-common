package io.mdcatapult.doclib.models

import java.time.LocalDateTime

import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}

class FileAttrsSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(FileAttrs(
      path = "/path/to/",
      name = "file.txt",
      mtime = LocalDateTime.parse("2019-10-01T12:00:00"),
      ctime = LocalDateTime.parse("2019-10-01T12:00:00"),
      atime = LocalDateTime.parse("2019-10-01T12:00:00"),
      size = 123456.toLong,
    ),
      """{
        |"path": "/path/to/",
        |"name": "file.txt",
        |"mtime": {"$date": 1569931200000},
        |"ctime": {"$date": 1569931200000},
        |"atime": {"$date": 1569931200000},
        |"size": {"$numberLong": "123456"}
        |}""".stripMargin, classOf[FileAttrs])

  }

}
