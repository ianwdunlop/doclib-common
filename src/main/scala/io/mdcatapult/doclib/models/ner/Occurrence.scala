package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.util.HashUtils.md5HashString
import org.mongodb.scala.bson.ObjectId

abstract class Occurrence {
  val entityType: String
  val entityGroup: Option[String]
  val schema: String
  val characterStart: Int
  val characterEnd: Int
  val fragment: Option[ObjectId]
  val correctedValue: Option[String]
  val correctedValueHash: Option[String]
  val resolvedEntity: Option[String]
  val resolvedEntityHash: Option[String]
  val `type`: String

  def toMap: Map[String,Any] =
    Map(
      "entityType" -> entityType,
      "entityGroup" -> entityGroup,
      "schema" -> schema,
      "characterStart" -> characterStart,
      "characterEnd" -> characterEnd,
      "fragment" -> fragment,
      "correctedValue" -> correctedValue,
      "resolvedEntity" -> resolvedEntity,
      "type" -> `type`
    ).filter(_._2 != None)
}

object Occurrence {
  def apply(values: Map[String, Any]): Occurrence = {

    def getVal[T](key: String): Option[T] = {
      if (!values.exists(_._1 == key)) None
      else values(key).asInstanceOf[T] match {
        case None ⇒ None
        case v ⇒ Some(v)
      }
    }

    val entityType: String = getVal[String]("entityType").getOrElse(throw new Exception("entityType must be provided"))
    val entityGroup: Option[String] = getVal[String]("entityGroup")
    val schema: String = getVal[String]("schema").getOrElse(throw new Exception("schema must be provided"))
    val characterStart: Int = getVal[Int]("characterStart").getOrElse(throw new Exception("characterStart must be provided"))
    val characterEnd: Int = getVal[Int]("characterEnd").getOrElse(throw new Exception("characterEnd must be provided"))
    val fragment: Option[ObjectId]  = getVal[ObjectId]("fragment")
    val correctedValue: Option[String]  = getVal[String]("correctedValue")
    val correctedValueHash: Option[String]  = getVal[String]("correctedValueHash")
    val resolvedEntity: Option[String]  = getVal[String]("resolvedEntity")
    val resolvedEntityHash: Option[String]  = getVal[String]("resolvedEntityHash")


    values("type").toString.toLowerCase match {
      case "document" ⇒ DocumentOccurrence(
        entityType = entityType,
        entityGroup = entityGroup,
        schema = schema,
        characterStart = characterStart,
        characterEnd = characterEnd,
        fragment = fragment,
        correctedValue = correctedValue,
        correctedValueHash = correctedValueHash,
        resolvedEntity = resolvedEntity,
        resolvedEntityHash = resolvedEntityHash
      )
      case "fragment" ⇒ FragmentOccurrence(
        entityType = entityType,
        entityGroup = entityGroup,
        schema = schema,
        characterStart = characterStart,
        characterEnd = characterEnd,
        wordIndex = getVal[Int]("characterStart").getOrElse(throw new Exception("wordIndex must be provided")),
        fragment = fragment,
        correctedValue = correctedValue,
        correctedValueHash = correctedValueHash,
        resolvedEntity = resolvedEntity,
        resolvedEntityHash = resolvedEntityHash
      )
    }
  }

  def md5(occurrences: Seq[Occurrence]): String = {
    def keyValuesPairsAsText(o: Occurrence): Seq[String] =
      for {
        (key, value) <- o.toMap.toSeq.sortBy(_._1)
      } yield s"$key:$value"

    val allPairedKeyValues =
      for {
        o <- occurrences
        keyValue = keyValuesPairsAsText(o).mkString(",")
      } yield keyValue

    val allOccurrencesAsText = allPairedKeyValues.sorted.mkString(",")

    md5HashString(allOccurrencesAsText)
  }
}
