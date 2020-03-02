package io.mdcatapult.doclib.models.ner

import java.nio.ByteBuffer
import java.util.UUID

import io.mdcatapult.doclib.util.HashUtils

abstract class Occurrence {
  val _id: UUID
  val characterStart: Int
  val characterEnd: Int
  val fragment: Option[UUID]
  val correctedValue: Option[String]
  val correctedValueHash: Option[String]
  val resolvedEntity: Option[String]
  val resolvedEntityHash: Option[String]
  val `type`: String

  def toMap: Map[String,Any] =
    Map(
      "_id" → _id,
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

    def decodeUUID(uuidBytes: Array[Byte]): Option[UUID] = {
      uuidBytes match {
        case bytes ⇒ {
          val bb = ByteBuffer.wrap(bytes)
          Some(new UUID(bb.getLong, bb.getLong))
        }
        case _ ⇒ None
      }
    }

    getVal[Array[Byte]]("_id").getOrElse(throw new Exception("_id must be provided"))
    val _id: UUID = decodeUUID(getVal[Array[Byte]]("_id").getOrElse(throw new Exception("_id must be provided"))).get
    val characterStart: Int = getVal[Int]("characterStart").getOrElse(throw new Exception("characterStart must be provided"))
    val characterEnd: Int = getVal[Int]("characterEnd").getOrElse(throw new Exception("characterEnd must be provided"))
    val fragment: Option[UUID]  = decodeUUID(getVal[Array[Byte]]("fragment").get)
    val correctedValue: Option[String]  = getVal[String]("correctedValue")
    val correctedValueHash: Option[String]  = getVal[String]("correctedValueHash")
    val resolvedEntity: Option[String]  = getVal[String]("resolvedEntity")
    val resolvedEntityHash: Option[String]  = getVal[String]("resolvedEntityHash")


    values("type").toString.toLowerCase match {
      case "document" ⇒ DocumentOccurrence(
        _id = _id,
        characterStart = characterStart,
        characterEnd = characterEnd,
        fragment = fragment,
        correctedValue = correctedValue,
        correctedValueHash = correctedValueHash,
        resolvedEntity = resolvedEntity,
        resolvedEntityHash = resolvedEntityHash
      )
      case "fragment" ⇒ FragmentOccurrence(
        _id = _id,
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

    HashUtils.md5(allOccurrencesAsText)
  }
}
