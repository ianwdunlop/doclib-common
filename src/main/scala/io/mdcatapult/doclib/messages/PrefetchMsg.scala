package io.mdcatapult.doclib.messages

import io.mdcatapult.doclib.json.{BsonDocumentJson, MetaValueJson}
import io.mdcatapult.doclib.models.Origin
import io.mdcatapult.doclib.models.metadata.MetaValueUntyped
import io.mdcatapult.klein.queue.Envelope
import play.api.libs.json._

object PrefetchMsg extends BsonDocumentJson  with MetaValueJson{
  implicit val msgFormatter: Format[PrefetchMsg] = Json.format[PrefetchMsg]
}

case class PrefetchMsg(
                        source: String,
                        origin: Option[List[Origin]] = None,
                        tags: Option[List[String]] = None,
                        metadata: Option[List[MetaValueUntyped]] = None,
                        derivative: Option[Boolean] = None,
                        verify: Option[Boolean] = None
                      ) extends Envelope


