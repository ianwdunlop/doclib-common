package io.mdcatapult.doclib.models

import io.lemonlabs.uri.Uri
import io.mdcatapult.doclib.json._
import io.mdcatapult.doclib.models.metadata.MetaValueUntyped
import play.api.libs.json.{Format, Json}

object Origin extends StringAnyMapJson with LemonLabsUriJson with MetaValueJson {
  implicit val prefetchOriginFormatter: Format[Origin] = Json.format[Origin]
}

case class Origin(
                   scheme: String,
                   hostname: Option[String] = None,
                   uri: Option[Uri] = None,
                   headers: Option[Map[String, Seq[String]]] = None,
                   metadata: Option[List[MetaValueUntyped]] = None
                 )



