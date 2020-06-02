package io.mdcatapult.doclib.models.ner

import java.time.LocalDateTime
import java.util.UUID

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
                  document: UUID,
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
