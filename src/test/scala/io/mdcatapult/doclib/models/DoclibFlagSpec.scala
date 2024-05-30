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

import io.mdcatapult.util.models.Version
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DoclibFlagSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Origin" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(DoclibFlag(
      key = "test",
      version = Version(
        number = "2.0.0",
        major = 2,
        minor = 0,
        patch = 0,
        hash = "01234567890"),
      started = Some(LocalDateTime.parse("2019-10-01T12:00:00")),
      ended = Some(LocalDateTime.parse("2019-10-01T12:00:00")),
      errored = Some(LocalDateTime.parse("2019-10-01T12:00:00")),
      summary = Some("errored"),
      reset = Some(LocalDateTime.parse("2019-10-01T12:00:00")),
      state = Some(DoclibFlagState(value = "12345", updated = LocalDateTime.parse("2019-10-01T12:00:00")))
    ),
      """{
        |"key": "test",
        |"version": {"number": "2.0.0", "major": 2, "minor": 0, "patch": 0, "hash": "01234567890"},
        |"started": {"$date": 1569931200000},
        |"ended": {"$date": 1569931200000},
        |"errored": {"$date": 1569931200000},
        |"reset": {"$date": 1569931200000},
        |"state": {"value": "12345", "updated": {"$date": 1569931200000}},
        |"summary": "errored",
        |"queued": false
        |}""".stripMargin)

  }

}
