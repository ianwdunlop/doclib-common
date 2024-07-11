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

package io.doclib.common.models.ner

import java.time.LocalDateTime

import io.doclib.common.models.BsonCodecCompatible
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SchemaSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(Schema(
      key = "keyValue",
      tool = "toolValue",
      config = "configValue",
      version = "versionValue",
      lastRun = LocalDateTime.parse("2019-10-01T12:00:00")
    ),
      """{
        |"key": "keyValue",
        |"tool": "toolValue",
        |"config": "configValue",
        |"version": "versionValue",
        |"lastRun": {"$date": 1569931200000}}""".stripMargin)
  }
}
