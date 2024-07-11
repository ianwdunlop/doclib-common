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

import io.doclib.common.models.metadata.MetaString
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DerivativeSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Derivative" can "be encoded and decoded successfully to BSON" in {
    roundTrip(Derivative(
      `type` = "test",
      path = "/path/to/file/txt",
      metadata = Some(List(MetaString("key", "value")))
    ),
      """{
        |"type": "test",
        |"path": "/path/to/file/txt",
        |"metadata": [{
        |   "key": "key",
        |   "value": "value"
        |}]}""".stripMargin)
  }

}
