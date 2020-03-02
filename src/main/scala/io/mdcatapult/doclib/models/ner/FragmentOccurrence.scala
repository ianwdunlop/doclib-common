package io.mdcatapult.doclib.models.ner

import java.util.UUID

case class FragmentOccurrence(
                              _id: UUID,
                              characterStart: Int,
                              characterEnd: Int,
                              wordIndex: Int,
                              fragment: Option[UUID] = None,
                              correctedValue: Option[String] = None,
                              correctedValueHash: Option[String] = None,
                              resolvedEntity: Option[String] = None,
                              resolvedEntityHash: Option[String] = None,
                              `type`: String = "fragment"
) extends Occurrence {
  override def toMap: Map[String, Any] = super.toMap + ("wordIndex" -> wordIndex)
}
