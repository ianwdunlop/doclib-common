package io.mdcatapult.doclib.models.ner

import java.util.UUID

import org.mongodb.scala.bson.ObjectId

case class NerDocument(
                     _id: UUID,
                     value: String,
                     hash: String,
                     document: ObjectId,
                     schema: Option[Schema] = None
                 )
