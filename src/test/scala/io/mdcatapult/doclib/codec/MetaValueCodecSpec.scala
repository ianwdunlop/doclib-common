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

package io.mdcatapult.doclib.codec

import java.time.format.DateTimeFormatter

import io.mdcatapult.doclib.models.metadata._
import io.mdcatapult.util.time.nowUtc

class MetaValueCodecSpec extends CodecSpec {

  val codec = new MetaValueCodec()

  "MetaValueCodec" can "encode & decode MetaString" in {
    val original = MetaString("theKey", "theValue")
    val decodedDocument = roundTrip(original, codec)

    decodedDocument shouldBe a[MetaString]
    decodedDocument should equal(original)
  }
  it can "encode & decode MetaBoolean" in {
    val original = MetaBoolean("theKey", value = true)
    val decodedDocument = roundTrip(original, codec)

    decodedDocument shouldBe a[MetaBoolean]
    decodedDocument should equal(original)
  }
  it can "encode & decode MetaDouble" in {
    val original = MetaDouble("theKey", 2.0)
    val decodedDocument = roundTrip(original, codec)

    decodedDocument shouldBe a[MetaDouble]
    decodedDocument should equal(original)
  }
  it can "encode & decode MetaInt" in {
    val original = MetaInt("theKey", 1)
    val decodedDocument = roundTrip(original, codec)

    decodedDocument shouldBe a[MetaInt]
    decodedDocument should equal(original)
  }
  it can "encode & decode MetaDateTime" in {
    val time = nowUtc.now()
    val original = MetaDateTime("theKey", time)
    val decodedDocument = roundTrip(original, codec)

    decodedDocument shouldBe a[MetaDateTime]
    // compares formatted date time as translation to BSON datetime loses precision
    val retrieved = decodedDocument.asInstanceOf[MetaDateTime].value
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    retrieved.format(formatter) should equal(time.format(formatter))

  }
}
