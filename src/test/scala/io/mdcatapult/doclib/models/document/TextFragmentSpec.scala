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

package io.mdcatapult.doclib.models.document

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.models.ner.Fixture.{docId, docIdMongo, uuid, uuidMongoBinary}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TextFragmentSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(TextFragment(
      _id = uuid,
      document = docId,
      index = 1,
      startAt = 2,
      endAt = 3,
      length = 1
    ),
      s"""{
        |"_id": $uuidMongoBinary,
        |"document": $docIdMongo,
        |"index": 1,
        |"startAt": 2,
        |"endAt": 3,
        |"length": 1,
        |}""".stripMargin)
  }
}
