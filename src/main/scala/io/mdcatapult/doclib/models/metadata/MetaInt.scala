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

package io.mdcatapult.doclib.models.metadata

import play.api.libs.json.{Format, Json}

object MetaInt {
  implicit val msgFormatter: Format[MetaInt] = Json.format[MetaInt]
}

case class MetaInt(key: String, value: Int) extends MetaValue[Int] with MetaValueUntyped{
  def getKey: String = key
  def getValue: Int = value
}
