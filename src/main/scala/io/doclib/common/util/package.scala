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

package io.doclib.common

package object util {
  /**
    * Sanatises a name to make it usable for the akka actor system.
    *
    * @param name
    * @return a sanitized string with no special characters and not beginning with a hyphen or underscore.
    */
  def sanitiseName(name: String): String =
    name.replaceAll("""[^a-zA-Z0-9-_]""","-").replaceAll("""^[-_]+""","")
}