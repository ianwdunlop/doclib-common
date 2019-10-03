package io.mdcatapult.doclib.messages

import io.mdcatapult.klein.queue.Envelope
import play.api.libs.json.{Format, Json, Reads, Writes}

object ArchiveMsg {
  implicit val msgReader: Reads[ArchiveMsg] = Json.reads[ArchiveMsg]
  implicit val msgWriter: Writes[ArchiveMsg] = Json.writes[ArchiveMsg]
  implicit val msgFormatter: Format[ArchiveMsg] = Json.format[ArchiveMsg]
}
case class ArchiveMsg(id: Option[String] = None, source: Option[String] = None) extends Envelope
