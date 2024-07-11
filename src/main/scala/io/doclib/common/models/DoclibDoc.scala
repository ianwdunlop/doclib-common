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

import java.time.LocalDateTime
import java.util.UUID
import io.doclib.common.models.metadata.MetaValueUntyped
import org.mongodb.scala.bson.ObjectId

case class DoclibDoc(
                      _id: ObjectId,
                      source: String,
                      hash: String,
                      mimetype: String,
                      created: LocalDateTime,
                      updated: LocalDateTime,
                      derivative: Boolean = false,
                      attrs: Option[FileAttrs] = None,
                      doclib: List[DoclibFlag] = List(),
                      tags: Option[List[String]] = None,
                      derivatives: Option[List[Derivative]] = None,
                      origin: Option[List[Origin]] = None,
                      metadata: Option[List[MetaValueUntyped]] = None,
                      uuid: Option[UUID] = None,
                      rogueFile: Option[Boolean] = None,
                    )  {

  def hasFlag(key: String): Boolean = doclib.exists(_.key == key)
  def getFlag(key: String): List[DoclibFlag] = doclib.filter(_.key == key)
}
