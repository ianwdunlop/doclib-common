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

class NerDocumentSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "NerDocument" can "be encoded and decoded successfully to BSON" in {
    roundTrip(NerDocument(
      _id =  uuid,
      value = "value",
      hash = "01234567890",
      entityType = Some("entity-type"),
      entityGroup = Some("entity-group"),
      resolvedEntity = Some("resolved-entity"),
      resolvedEntityHash = Some("resolved-entity-hash"),
      document = docId,
    ),
      s"""{
        |"_id": $uuidMongoBinary,
        |"value": "value",
        |"hash": "01234567890",
        |"document": $docIdMongo,
        |"entityType": "entity-type",
        |"entityGroup": "entity-group",
        |"resolvedEntity": "resolved-entity",
        |"resolvedEntityHash": "resolved-entity-hash",
        |"schema": null}""".stripMargin)
  }
}
