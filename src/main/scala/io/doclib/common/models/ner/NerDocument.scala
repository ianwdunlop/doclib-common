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

import java.util.UUID

import org.mongodb.scala.bson.ObjectId

case class NerDocument(
                        _id: UUID,
                        value: String,
                        hash: String,
                        document: ObjectId,
                        entityType: Option[String] = None,
                        entityGroup: Option[String] = None,
                        resolvedEntity: Option[String] = None,
                        resolvedEntityHash: Option[String] = None,
                        schema: Option[Schema] = None
                 )