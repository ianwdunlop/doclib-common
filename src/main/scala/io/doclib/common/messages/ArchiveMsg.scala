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

package io.doclib.common.messages

import io.doclib.common.messages.ArchiveMsg.msgFormatter
import io.doclib.queue.Envelope
import play.api.libs.json.{Format, Json}

object ArchiveMsg {
  implicit val msgFormatter: Format[ArchiveMsg] = Json.format[ArchiveMsg]
}
case class ArchiveMsg(id: Option[String] = None, source: Option[String] = None) extends Envelope {
  override def toJsonString(): String = Json.toJson(this).toString()
}
