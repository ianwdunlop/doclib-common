package io.mdcatapult.doclib.models.ner

import org.mongodb.scala.bson.ObjectId

case class NerDocument(
                     _id: ObjectId,
                     value: String,
                     hash: String,
                     total: Int,
                     document: ObjectId,
                     fragment: Option[ObjectId] = None,
                     occurrences: Option[List[Occurrence]] = None,
                     stats: Option[Stats] = None,
                     schemas: Option[List[Schema]] = None,
                     `type`: String = "document"
                 )
