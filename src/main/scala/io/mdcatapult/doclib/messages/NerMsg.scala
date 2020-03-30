package io.mdcatapult.doclib.messages

import io.mdcatapult.klein.queue.Envelope
import play.api.libs.json.{Format, Json}

object NerMsg {
  implicit val msgFormatter: Format[NerMsg] = Json.format[NerMsg]
}

case class NerMsg(id: String, requires: Option[List[String]]) extends Envelope


