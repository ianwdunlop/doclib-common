package io.mdcatapult.doclib.models.ner

import org.mongodb.scala.bson.ObjectId


case class FragmentOccurrence(
                              entityType: String,
                              entityGroup: Option[String] = None,
                              schema: String,
                              characterStart: Int,
                              characterEnd: Int,
                              wordIndex: Int,
                              fragment: Option[ObjectId] = None,
                              correctedValue: Option[String] = None,
                              correctedValueHash: Option[String] = None,
                              resolvedEntity: Option[String] = None,
                              resolvedEntityHash: Option[String] = None,
                              `type`: String = "fragment"
) extends Occurrence {
  override def toMap: Map[String, Any] = super.toMap + ("wordIndex" -> wordIndex)
}
