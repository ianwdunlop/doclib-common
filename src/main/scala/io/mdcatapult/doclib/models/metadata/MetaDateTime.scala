package io.mdcatapult.doclib.models.metadata

import java.time.LocalDateTime

case class MetaDateTime(key: String, value: LocalDateTime) extends MetaValue[LocalDateTime] with MetaValueUntyped {
  def getKey: String = key
  def getValue: LocalDateTime = value
}
