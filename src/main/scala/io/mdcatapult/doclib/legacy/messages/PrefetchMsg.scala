package io.mdcatapult.doclib.legacy.messages

import io.mdcatapult.doclib.json.BsonDocumentJson
import io.mdcatapult.klein.queue.Envelope
import org.mongodb.scala.Document
import play.api.libs.json._

object PrefetchMsg extends BsonDocumentJson {
  implicit val msgFormatter: Format[PrefetchMsg] = Json.format[PrefetchMsg]
}

case class PrefetchMsg(
                        source: String,
                        origin: String,
                        tags: Option[List[String]],
                        metadata: Option[Document]
                      ) extends Envelope


