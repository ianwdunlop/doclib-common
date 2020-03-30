package io.mdcatapult.doclib.models.metadata

import play.api.libs.json.{Format, Json}

object MetaDouble {
  implicit val msgFormatter: Format[MetaDouble] = Json.format[MetaDouble]
}
case class MetaDouble(key: String, value: Double) extends MetaValue[Double] with MetaValueUntyped{
  def getKey: String = key
  def getValue: Double = value
}
