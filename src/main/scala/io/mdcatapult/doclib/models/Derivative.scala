package io.mdcatapult.doclib.models

import io.mdcatapult.doclib.json._
import io.mdcatapult.doclib.models.metadata.MetaValueUntyped
import play.api.libs.json.{Format, Json, Reads, Writes}

object Derivative extends MetaValueJson {
  implicit val msgReader: Reads[Derivative] = Json.reads[Derivative]
  implicit val msgWriter: Writes[Derivative] = Json.writes[Derivative]
  implicit val msgFormatter: Format[Derivative] = Json.format[Derivative]
}

case class Derivative(`type`: String, path: String, metadata: Option[List[MetaValueUntyped]] = None)
