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

import io.mdcatapult.util.models.Version
import play.api.libs.json.{Format, Json}

object DoclibFlag  {
  implicit val prefetchOriginFormatter: Format[DoclibFlag] = Json.format[DoclibFlag]
}

/**
  * Flag Object for
  * @param version version number
  * @param started when consumer started
  * @param ended when consumer ended
  */
case class DoclibFlag(
                       key: String,
                       version: Version,
                       started: Option[LocalDateTime] = None,
                       ended: Option[LocalDateTime] = None,
                       errored: Option[LocalDateTime] = None,
                       state: Option[DoclibFlagState] = None,
                       summary: Option[String] = None,
                       reset: Option[LocalDateTime] = None,
                       queued: Option[Boolean] = Some(false)
                     ) {

  def isQueued: Boolean = queued.getOrElse(false)

  def isNotQueued: Boolean = !isQueued
}
