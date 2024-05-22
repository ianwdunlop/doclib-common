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

package io.mdcatapult.doclib.models.ner

import java.time.LocalDateTime

import org.mongodb.scala.bson.ObjectId

/** Stats holds precomputed statistics about the occurrences in a document from this consumer.  This will reduce the
 * load on Mongo server when making analytic queries.
 *
 * @param _id hash value that acts as a primary key and if not yet known it must be calculated using StatsKey.key
 * @param document id from the documents collection
 * @param schema the leadmine schema (e.g. for leadmine this is the yaml config 'label' value)
 * @param tool identifier for the tool that was run over the document
 * @param config identifier for the config that was used by the tool
 * @param version the version of the tool that was run
 * @param hash md5 hash of the raw entity data returned by the tool - note that this is completely unrelated to _id
 * @param lastRun the date the tool was last run on the document
 * @param updated the date the NER data found by the tool last changed
 * @param stats list of counts found for the document
 */
case class Stats(
                  _id: String,
                  document: ObjectId,
                  schema: String,
                  tool: String,
                  config: String,
                  version: String,
                  hash: String,
                  lastRun: LocalDateTime,
                  updated: LocalDateTime,
                  stats: List[Count],
                ) {

  def key: StatsKey = StatsKey(document, schema, config, tool)
}
