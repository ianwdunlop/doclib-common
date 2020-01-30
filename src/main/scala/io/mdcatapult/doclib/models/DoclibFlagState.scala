package io.mdcatapult.doclib.models

import java.time.LocalDateTime

import play.api.libs.json.{Format, Json, Reads, Writes}

object DoclibFlagState  {
  implicit val doclibFlagStateReader: Reads[DoclibFlagState] = Json.reads[DoclibFlagState]
  implicit val doclibFlagStateWriter: Writes[DoclibFlagState] = Json.writes[DoclibFlagState]
  implicit val doclibFlagStateFormatter: Format[DoclibFlagState] = Json.format[DoclibFlagState]
}

/**
 * Allows tracking of  when the output of any consumer changes for a given document,
 * so that we can decide whether any downstream processing should be repeated
 *
 * @param value arbitrary value representing the output of the consumer
 * @param updated timestamp for when the 'state.value' last changed
 */
case class DoclibFlagState(value: String, updated: LocalDateTime)
