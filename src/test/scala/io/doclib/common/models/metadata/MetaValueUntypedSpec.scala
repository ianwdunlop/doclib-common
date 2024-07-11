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

import io.doclib.common.json.MetaValueJson
import io.doclib.common.models.BsonCodecCompatible
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class MetaValueUntypedSpec extends AnyFlatSpec with Matchers with MetaValueJson with BsonCodecCompatible {

  val dummyList: List[MetaValueUntyped] = List(
    MetaString("stringKey", "stringValue"),
    MetaInt("intKey", 1),
    MetaDouble("doubleKey", 1.1),
    MetaBoolean("boolKey", value=true),
    MetaDateTime("dateTimeKey", LocalDateTime.parse("2019-10-04T13:46:37.612508"))
  )

  "The list" should "be able to be encoded as JSON" in {
    val listJson = Json.toJson(dummyList)
    assert(listJson.toString == """[{"key":"stringKey","value":"stringValue"},{"key":"intKey","value":1},{"key":"doubleKey","value":1.1},{"key":"boolKey","value":true},{"key":"dateTimeKey","value":"2019-10-04T13:46:37.612508"}]""")
  }

  it should "be convertible from JSON into a list" in {
    val listJson = Json.parse("""[{"key":"stringKey","value":"stringValue"},{"key":"intKey","value":1},{"key":"doubleKey","value":1.1},{"key":"boolKey","value":true},{"key":"dateTimeKey","value":"2019-10-04T13:46:37.612508"}]""").as[List[MetaValueUntyped]]
    assert(listJson.length == 5)
  }
}
