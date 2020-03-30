package io.mdcatapult.doclib.messages

import io.mdcatapult.klein.queue.Envelope
import play.api.libs.json.{Format, Json}

object ArchiveMsg {
  implicit val msgFormatter: Format[ArchiveMsg] = Json.format[ArchiveMsg]
}
case class ArchiveMsg(id: Option[String] = None, source: Option[String] = None) extends Envelope
