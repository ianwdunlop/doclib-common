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

package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.models.ner.Fixture._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FragmentOccurrenceSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(Occurrence(
      _id = uuid,
      nerDocument = uuid,
      characterStart = 1,
      characterEnd = 2,
      wordIndex = Some(3)
    ),
      s"""{
        |"_id": $uuidMongoBinary,
        |"nerDocument": $uuidMongoBinary,
        |"characterStart": 1,
        |"characterEnd": 2,
        |"wordIndex": 3,
        |"fragment": null,
        |"correctedValue": null,
        |"correctedValueHash": null,
        |"type": "fragment"}""".stripMargin)
  }

  it can "give old known hash for same document occurrence" in {
    val doc = Occurrence(
      _id = uuid,
      nerDocument = uuid,
      characterStart = 12,
      characterEnd = 15,
      wordIndex = Some(10),
      fragment = Option(fragmentUuid),
      correctedValue = Option("fixed!"),
      correctedValueHash = Option("5e185e300268642a0fcbc964")
    )

    assert(Occurrence.md5(Seq(doc)) == "c1848b55947e5cca0197c5a7295b36c5")
  }

  it can "give old known hash for same document occurrence with optionals are None" in {
    val doc = Occurrence(
      _id = uuid,
      nerDocument = uuid,
      characterStart = 12,
      characterEnd = 15,
      wordIndex = Some(10),
      fragment = None,
      correctedValue = None,
      correctedValueHash = None
    )

    assert(Occurrence.md5(Seq(doc)) == "b9e2be3d8dea149526592430ce3d926a")
  }
}
