package io.mdcatapult.doclib.messages.legacy

import io.mdcatapult.doclib.util.StringAnyMapJson
import io.mdcatapult.klein.queue.Envelope
import play.api.libs.json._

object PrefetchMsg extends StringAnyMapJson {
  implicit val msgReader: Reads[PrefetchMsg] = Json.reads[PrefetchMsg]
  implicit val msgWriter: Writes[PrefetchMsg] = Json.writes[PrefetchMsg]
  implicit val msgFormatter: Format[PrefetchMsg] = Json.format[PrefetchMsg]
}

case class PrefetchMsg(
                        source: String,
                        origin: String,
                        tags: List[String]
                      ) extends Envelope


