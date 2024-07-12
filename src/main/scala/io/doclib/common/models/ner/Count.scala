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

package io.doclib.common.models.ner

/** Count is the labelled number of times a value was found in a document.
  *
  * @param value name of the quantity being counted, such as entity type or entity group
  * @param count number of times quantity was found in doc
  * @param `type` type of the value: "entityType" or "entityGroup"
  */
case class Count(
                  value: String,
                  count: Int,
                  `type`: String,
               )