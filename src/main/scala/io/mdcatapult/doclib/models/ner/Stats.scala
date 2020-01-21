package io.mdcatapult.doclib.models.ner

import java.time.LocalDateTime

import org.mongodb.scala.bson.ObjectId

/** Stats holds precomputed statistics about the occurrences in a document from this consumer.  This will reduce the
 * load on Mongo server when making analytic queries.
 *
 * @param _id MongoDb Object ID
 * @param document id from the documents collection
 * @param schema the leadmine schema (e.g. for leadmine this is the yaml config 'label' value)
 * @param tool identifier for the tool that was run over the document
 * @param config identifier for the config that was used by the tool
 * @param version the version of the tool that was run
 * @param hash md5 hash of the raw entity data returned by the tool
 * @param lastRun the date the tool was last run on the document
 * @param updated the date the NER data found by the tool last changed
 * @param stats list of counts found for the document
 */
case class Stats(
                  _id: ObjectId,
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
