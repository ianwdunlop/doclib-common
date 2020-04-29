package io.mdcatapult.doclib.models

import java.time.LocalDateTime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FileAttrsSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

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
        |}""".stripMargin)

  }

}
