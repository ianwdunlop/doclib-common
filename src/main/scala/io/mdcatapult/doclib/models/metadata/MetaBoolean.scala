package io.mdcatapult.doclib.models.metadata

case class MetaBoolean(key: String, value: Boolean) extends MetaValue[Boolean] with MetaValueUntyped {
  def getKey: String = key
  def getValue: Boolean = value
}