package io.mdcatapult.doclib.models.metadata

import play.api.libs.json.{Format, Json, Reads, Writes}

object MetaInt {
  implicit val msgReader: Reads[MetaInt] = Json.reads[MetaInt]
  implicit val msgWriter: Writes[MetaInt] = Json.writes[MetaInt]
  implicit val msgFormatter: Format[MetaInt] = Json.format[MetaInt]
}

case class MetaInt(key: String, value: Int) extends MetaValue[Int] {
  def getKey: String = key
  def getValue: Int = value
}
