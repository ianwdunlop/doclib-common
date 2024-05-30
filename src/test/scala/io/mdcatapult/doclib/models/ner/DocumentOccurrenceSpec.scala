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

import java.util.UUID.randomUUID

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.models.ner.Fixture._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DocumentOccurrenceSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(Occurrence(
      _id = uuid,
      nerDocument = uuid,
      characterStart = 1,
      characterEnd = 2
    ),
      s"""{
        |"_id": $uuidMongoBinary,
        |"nerDocument": $uuidMongoBinary,
        |"characterStart": 1,
        |"characterEnd": 2,
        |"fragment": null,
        |"correctedValue": null,
        |"correctedValueHash": null,
        |"wordIndex": null,
        |"type": "document"
        |}""".stripMargin)
  }

  it can "give old known hash for same document occurrence" in {
    val doc = Occurrence(
      _id = uuid,
      nerDocument = uuid,
      characterStart = 12,
      characterEnd = 15,
      fragment = Option(fragmentUuid),
      correctedValue = Option("fixed!"),
      correctedValueHash = Option("5e185e300268642a0fcbc964")
    )

    assert(Occurrence.md5(Seq(doc)) == "fafbb1b7c5850a56a61150b55fba488b")
  }

  it can "give old known hash for same document occurrence with optionals are None" in {
    val doc = Occurrence(
      _id = uuid,
      nerDocument = uuid,
      characterStart = 12,
      characterEnd = 15,
      fragment = None,
      correctedValue = None,
      correctedValueHash = None
    )

    assert(Occurrence.md5(Seq(doc)) == "27f8e2217b9e6310984c1c383f3158ce")
  }

  "Occurrences with different _ids for the same value and ner doc" should "have the same md5" in {
    val nerID = randomUUID()
    val occurrences = List[Occurrence](
      Occurrence(
        _id = randomUUID(),
        nerDocument = nerID,
        characterStart = 115416768,
        characterEnd = 115416777
      ),
      Occurrence(
        _id = randomUUID(),
        nerDocument = nerID,
        characterStart = 115424728,
        characterEnd = 115424737
      ),
      Occurrence(
        _id = randomUUID(),
        nerDocument = nerID,
        characterStart = 115379372,
        characterEnd = 115379381
      )
    )
    val occurrencesNew = List[Occurrence](
      Occurrence(
        _id = randomUUID(),
        nerDocument = nerID,
        characterStart = 115416768,
        characterEnd = 115416777
      ),
      Occurrence(
        _id = randomUUID(),
        nerDocument = nerID,
        characterStart = 115424728,
        characterEnd = 115424737
      ),
      Occurrence(
        _id = randomUUID(),
        nerDocument = nerID,
        characterStart = 115379372,
        characterEnd = 115379381
      )
    )
    assert(Occurrence.md5(occurrences) == Occurrence.md5(occurrencesNew))
  }


}
