package io.mdcatapult.doclib.models.ner

import java.util.UUID

import io.mdcatapult.util.hash.Md5

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

  def toFields: (UUID, UUID, Map[String, Option[_]]) =
    (
      _id,
      nerDocument,
      Map(
        "characterStart" -> Some(characterStart),
        "characterEnd" -> Some(characterEnd),
        "fragment" -> Some(fragment),
        "correctedValue" -> Some(correctedValue),
        "wordIndex" -> Some(wordIndex),
        "type" -> Some(`type`)
        // Note that fragment, correctedValue & wordIndex are already inside
        // a Some(_) but wrapping it for the md5 (key, option_value.get)
      ).filter(_._2.get != None)
    )

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
    val docType =
      if (fragment.isDefined || wordIndex.isDefined)
        "fragment"
      else
        "document"

    this(_id, nerDocument, characterStart, characterEnd,fragment, correctedValue, correctedValueHash, wordIndex, docType)
  }

  def md5(occurrences: Seq[Occurrence]): String = {
    def keyValuesPairsAsText(o: Occurrence): Seq[String] = {
      val (_, _, rest) = o.toFields
      for {
        (key, option_value) <- rest.view.toSeq.sortBy(_._1)
        (k, value) = (key, option_value.get)
      } yield s"$k:$value"
    }

    val allPairedKeyValues =
      for {
        o <- occurrences
        keyValue = keyValuesPairsAsText(o).mkString(",")
      } yield keyValue

    val allOccurrencesAsText = allPairedKeyValues.sorted.mkString(",")

    Md5.md5(allOccurrencesAsText)
  }
}
