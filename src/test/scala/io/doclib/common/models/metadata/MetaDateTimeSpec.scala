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

package io.doclib.common.models.metadata

import java.time.LocalDateTime

import io.doclib.common.models.BsonCodecCompatible
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetaDateTimeSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(MetaDateTime(
      key = "key",
      value = LocalDateTime.parse("2019-10-01T12:00:00")
    ),
      """{
        |  "key": "key",
        |  "value": {"$date": 1569931200000}
        |}""".stripMargin)
  }
}
