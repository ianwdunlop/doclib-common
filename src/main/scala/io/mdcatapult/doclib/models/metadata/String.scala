package io.mdcatapult.doclib.models.metadata

import play.api.libs.json.{Format, Json, Reads, Writes}

object String {
  implicit val msgReader: Reads[String] = Json.reads[String]
  implicit val msgWriter: Writes[String] = Json.writes[String]
  implicit val msgFormatter: Format[String] = Json.format[String]
}
case class String(key: String, value: String) extends MetaValue
x