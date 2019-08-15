package io.mdcatapult.doclib.models.metadata

import play.api.libs.json.{Format, Json, Reads, Writes}

object MetaString {
  implicit val msgReader: Reads[MetaString] = Json.reads[MetaString]
  implicit val msgWriter: Writes[MetaString] = Json.writes[MetaString]
  implicit val msgFormatter: Format[MetaString] = Json.format[MetaString]
}
case class MetaString(key: String, value: String) extends MetaValue