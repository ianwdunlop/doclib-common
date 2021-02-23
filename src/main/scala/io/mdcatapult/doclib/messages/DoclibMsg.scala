package io.mdcatapult.doclib.messages

import io.mdcatapult.klein.queue.Envelope
import play.api.libs.json.{Format, Json}

object DoclibMsg {
  implicit val msgFormatter: Format[DoclibMsg] = Json.format[DoclibMsg]
}

case class DoclibMsg(id: String) extends Envelope
