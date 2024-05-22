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

package io.mdcatapult.doclib.messages

import io.mdcatapult.doclib.messages.NerMsg.msgFormatter
import io.mdcatapult.klein.queue.Envelope
import play.api.libs.json.{Format, Json}

object NerMsg {
  implicit val msgFormatter: Format[NerMsg] = Json.format[NerMsg]
}

case class NerMsg(id: String, requires: Option[List[String]]) extends Envelope {
  override def toJsonString(): String = Json.toJson(this).toString()
}


