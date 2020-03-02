package io.mdcatapult.doclib.models.ner

import java.util.UUID

case class DocumentOccurrence(
                               _id: UUID,
                               characterStart: Int,
                               characterEnd: Int,
                               fragment: Option[UUID] = None,
                               correctedValue: Option[String] = None,
                               correctedValueHash: Option[String] = None,
                               resolvedEntity: Option[String] = None,
                               resolvedEntityHash: Option[String] = None,
                               `type`: String = "document"
) extends Occurrence