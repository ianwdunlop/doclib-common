package io.mdcatapult.doclib.models.metadata

import play.api.libs.json.{Format, Json, Reads, Writes}
object Double {
  implicit val msgReader: Reads[Double] = Json.reads[Double]
  implicit val msgWriter: Writes[Double] = Json.writes[Double]
  implicit val msgFormatter: Format[Double] = Json.format[Double]
}
case class Double(key: String, value: Double) extends MetaValue