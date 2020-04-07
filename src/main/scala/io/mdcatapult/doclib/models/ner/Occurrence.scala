package io.mdcatapult.doclib.models.ner

import java.util.UUID

import io.mdcatapult.doclib.util.HashUtils

import scala.collection.compat._

case class Occurrence(
                       _id: UUID,
                       nerDocument: UUID,
                       characterStart: Int,
                       characterEnd: Int,
                       fragment: Option[UUID],
                       correctedValue: Option[String],
                       correctedValueHash: Option[String],
                       wordIndex: Option[Int],
                       `type`: String) {

  def toMap: Map[String, Any] =
    Map(
      "_id" -> _id,
      "nerDocument" -> nerDocument,
      "characterStart" -> characterStart,
      "characterEnd" -> characterEnd,
      "fragment" -> fragment,
      "correctedValue" -> correctedValue,
      "wordIndex" -> wordIndex,
      "type" -> `type`
    ).filter(_._2 != None)

}

object Occurrence {

  def apply(
            _id: UUID,
            nerDocument: UUID,
            characterStart: Int,
            characterEnd: Int,
            fragment: Option[UUID] = None,
            correctedValue: Option[String] = None,
            correctedValueHash: Option[String] = None,
            wordIndex: Option[Int] = None
          ): Occurrence = {
    val docType = wordIndex.map(_ => "fragment").getOrElse("document")
    this(_id, nerDocument, characterStart, characterEnd,fragment, correctedValue, correctedValueHash, wordIndex, docType)
  }

  def md5(occurrences: Seq[Occurrence]): String = {
    def keyValuesPairsAsText(o: Occurrence): Seq[String] =
      for {
        (key, value) <- o.toMap.view.filterKeys(k => k != "_id" && k != "nerDocument").toSeq.sortBy(_._1)
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
