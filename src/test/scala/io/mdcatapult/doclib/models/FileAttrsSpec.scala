/*
 * Copyright 2024 Medicines Discovery Catapult
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
