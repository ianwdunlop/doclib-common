package io.mdcatapult.doclib.models.metadata

import play.api.libs.json.{Format, Json}

object MetaInt {
  implicit val msgFormatter: Format[MetaInt] = Json.format[MetaInt]
}

case class MetaInt(key: String, value: Int) extends MetaValue[Int] with MetaValueUntyped{
  def getKey: String = key
  def getValue: Int = value
}
