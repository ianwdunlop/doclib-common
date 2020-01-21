package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.DoclibDoc
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
                   )
