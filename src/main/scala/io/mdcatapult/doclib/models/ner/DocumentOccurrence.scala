package io.mdcatapult.doclib.models.ner

import org.mongodb.scala.bson.ObjectId

case class DocumentOccurrence(
                               entityType: String,
                               entityGroup: Option[String] = None,
                               schema: String,
                               characterStart: Int,
                               characterEnd: Int,
                               fragment: Option[ObjectId] = None,
                               correctedValue: Option[String] = None,
                               correctedValueHash: Option[String] = None,
                               resolvedEntity: Option[String] = None,
                               resolvedEntityHash: Option[String] = None,
                               `type`: String = "document"
) extends Occurrence
