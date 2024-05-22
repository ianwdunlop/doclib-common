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

import java.time.LocalDateTime

import play.api.libs.json.{Format, Json}

object DoclibFlagState  {
  implicit val doclibFlagStateFormatter: Format[DoclibFlagState] = Json.format[DoclibFlagState]
}

/**
 * Allows tracking of  when the output of any consumer changes for a given document,
 * so that we can decide whether any downstream processing should be repeated
 *
 * @param value arbitrary value representing the output of the consumer
 * @param updated timestamp for when the 'state.value' last changed
 */
case class DoclibFlagState(value: String, updated: LocalDateTime)
