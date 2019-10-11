package io.mdcatapult.doclib.models.ner

import org.mongodb.scala.bson.ObjectId

case class DocumentOccurrence(
                               entityType: String,
                               schema: String,
                               characterStart: Int,
                               characterEnd: Int,
                               fragment: Option[ObjectId] = None,
                               correctedValue: Option[String] = None,
                               correctedValueHash: Option[String] = None,
                               resolvedEntity: Option[String] = None,
                               resolvedEntityHash: Option[String] = None,
) extends Occurrence {

  val `type`: String = "document"
}
