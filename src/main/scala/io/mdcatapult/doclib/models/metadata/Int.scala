package io.mdcatapult.doclib.models.metadata

import play.api.libs.json.{Format, Json, Reads, Writes}

object Int {
  implicit val msgReader: Reads[Int] = Json.reads[Int]
  implicit val msgWriter: Writes[Int] = Json.writes[Int]
  implicit val msgFormatter: Format[Int] = Json.format[Int]
}

case class Int(key: String, value: Int) extends MetaValue
