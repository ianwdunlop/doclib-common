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

import io.doclib.common.models.DoclibDoc
import io.doclib.util.hash.Md5
import org.mongodb.scala.bson.ObjectId

object StatsKey {

  /** Create an object that represents the primary key information into the consumer stats Mongo collection.
   *
   * @param doc document stats are collected for
   * @param config identifier for the config that was used by the tool
   * @param tool identifier for the tool that was run over the document
   * @param s the schema (e.g. for leadmine this is the yaml config 'label' value)
   * @return stats key
   */
  def apply(doc: DoclibDoc, config: String, tool: String)(implicit s: Schema): StatsKey =
    StatsKey(doc._id, s.key, config, tool)
}

/** StatsKey represents the primary key information into the consumer stats Mongo collection.
 *
 * @param document id from the documents collection
 * @param schema the schema (e.g. for leadmine this is the yaml config 'label' value)
 * @param tool identifier for the tool that was run over the document
 * @param config identifier for the config that was used by the tool
 */
case class StatsKey(
                     document: ObjectId,
                     schema: String,
                     config: String,
                     tool: String,
                   ) {

  /** Calculate the hash value that is required to be used as the _id for the corresponding Stats object. */
  def key: String = Md5.md5(document.toHexString + schema + config + tool)
}
