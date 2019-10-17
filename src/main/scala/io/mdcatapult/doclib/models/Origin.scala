package io.mdcatapult.doclib.models

import io.lemonlabs.uri.Uri
import io.mdcatapult.doclib.json._
import io.mdcatapult.doclib.models.metadata.{MetaValue, MetaValueUntyped}
import io.mdcatapult.doclib.util._
import play.api.libs.json.{Format, Json, Reads, Writes}

object Origin extends StringAnyMapJson with LemonLabsUriJson with MetaValueJson {
  implicit val prefetchOriginReader: Reads[Origin] = Json.reads[Origin]
  implicit val prefetchOriginWriter: Writes[Origin] = Json.writes[Origin]
  implicit val prefetchOriginFormatter: Format[Origin] = Json.format[Origin]
}

case class Origin(
                           scheme: String,
                           uri: Option[Uri] = None,
                           headers: Option[Map[String, Seq[String]]] = None,
                           metadata: Option[List[MetaValueUntyped]] = None
                         )



