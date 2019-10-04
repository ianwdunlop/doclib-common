package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.metadata.MetaValue
import org.mongodb.scala.bson.ObjectId

case class NerDoc(
                   _id: ObjectId,
                   value: String,
                   hash: String,
                   `type`: String,
                   total: Int,
                   document: ObjectId,
                   occurrences: List[NerOccurence] = List(),
                   metadata: Option[List[MetaValue[_]]] = None,
                   schemas: NerStats
                 )
