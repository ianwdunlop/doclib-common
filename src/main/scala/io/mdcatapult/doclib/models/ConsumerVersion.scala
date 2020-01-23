package io.mdcatapult.doclib.models

import play.api.libs.json.{Format, Json, Reads, Writes}

object ConsumerVersion  {
  implicit val prefetchOriginReader: Reads[ConsumerVersion] = Json.reads[ConsumerVersion]
  implicit val prefetchOriginWriter: Writes[ConsumerVersion] = Json.writes[ConsumerVersion]
  implicit val prefetchOriginFormatter: Format[ConsumerVersion] = Json.format[ConsumerVersion]
}

case class ConsumerVersion(number: String, major: Int, minor: Int, patch: Int, hash: String)
