package io.mdcatapult.doclib.models.metadata

import play.api.libs.json.{Format, Json}

object MetaString {
  implicit val msgFormatter: Format[MetaString] = Json.format[MetaString]
}
case class MetaString(key: String, value: String) extends MetaValue[String] with MetaValueUntyped {
  def getKey: String = key
  def getValue: String = value
}
