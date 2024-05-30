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

import io.mdcatapult.doclib.models.metadata.{MetaInt, MetaString}
import io.mdcatapult.doclib.models.ner.Fixture._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParentChildSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Derivative" can "be encoded and decoded successfully to BSON" in {
    val id = uuid
    val parentId = docId
    val childId = childDocId
    val metadataMap = List(MetaString("doi", "10.1101/327015"), MetaInt("a-value", 10))

    roundTrip(ParentChildMapping(
      _id = id,
      parent = parentId,
      child = Some(childId),
      childPath = "/a/path/to/child",
      metadata = Some(metadataMap),
      consumer = Some("consumer")
    ),
      s"""{
        |"_id": $uuidMongoBinary,
        |"parent": $docIdMongo,
        |"child": $childDocIdMongo,
        |"childPath": "/a/path/to/child",
        |"metadata": [{"key": "doi", "value": "10.1101/327015"}, {"key": "a-value", "value": 10}],
        |"consumer": "consumer"
        |}""".stripMargin)
  }

}