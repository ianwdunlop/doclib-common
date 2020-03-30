package io.mdcatapult.doclib.models

import play.api.libs.json.{Format, Json}

object ConsumerVersion  {
  implicit val prefetchOriginFormatter: Format[ConsumerVersion] = Json.format[ConsumerVersion]
}

case class ConsumerVersion(number: String, major: Int, minor: Int, patch: Int, hash: String)
