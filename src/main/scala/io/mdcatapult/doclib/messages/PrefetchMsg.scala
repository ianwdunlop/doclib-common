package io.mdcatapult.doclib.messages

import io.mdcatapult.doclib.json.{BsonDocumentJson, MetaValueJson}
import io.mdcatapult.doclib.models.Origin
import io.mdcatapult.doclib.models.metadata.{MetaValue, MetaValueUntyped}
import io.mdcatapult.klein.queue.Envelope
import play.api.libs.json._

object PrefetchMsg extends BsonDocumentJson  with MetaValueJson{
  implicit val msgReader: Reads[PrefetchMsg] = Json.reads[PrefetchMsg]
  implicit val msgWriter: Writes[PrefetchMsg] = Json.writes[PrefetchMsg]
  implicit val msgFormatter: Format[PrefetchMsg] = Json.format[PrefetchMsg]
}

case class PrefetchMsg(
                        source: String,
                        origin: Option[List[Origin]],
                        tags: Option[List[String]],
                        metadata: Option[List[MetaValueUntyped]],
                        derivative: Option[Boolean]
                      ) extends Envelope


