package io.mdcatapult.doclib.models.ner

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
