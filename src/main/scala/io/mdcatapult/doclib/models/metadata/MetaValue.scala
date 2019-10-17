package io.mdcatapult.doclib.models.metadata

trait MetaValue[T] {
  def getKey: String
  def getValue: T
}

trait MetaValueUntyped {
  def getKey: String
  def getValue: Any
}
