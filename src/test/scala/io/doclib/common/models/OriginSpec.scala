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

package io.doclib.common.models

import io.lemonlabs.uri.Uri
import io.doclib.common.models.metadata.{MetaInt, MetaString}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OriginSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(Origin(
      scheme = "https",
      hostname = None,
      uri = Some(Uri.parse("https://new.bbc.co.uk")),
      metadata = Some(List(MetaString("key", "value"), MetaInt("a-value", 1))),
      headers = None
    ),
      """{
        |"scheme": "https",
        |"hostname": null,
        |"uri": "https://new.bbc.co.uk",
        |"headers": null,
        |"metadata": [{
        |   "key": "key",
        |   "value": "value"
        |},
        |{
        |   "key" : "a-value",
        |   "value": 1
        |}
        |]}""".stripMargin)

  }

}
