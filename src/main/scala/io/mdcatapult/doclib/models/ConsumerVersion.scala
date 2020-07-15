package io.mdcatapult.doclib.models

import com.typesafe.config.Config
import play.api.libs.json.{Format, Json}

object ConsumerVersion  {
  implicit val prefetchOriginFormatter: Format[ConsumerVersion] = Json.format[ConsumerVersion]

  /**
    * Generates ConsumerVersion from configuration.
    * @param c config
    * @return version
    */
  def fromConfig(c: Config): ConsumerVersion =
    ConsumerVersion(
      number = c.getString("version.number"),
      major = c.getInt("version.major"),
      minor = c.getInt("version.minor"),
      patch = c.getInt("version.patch"),
      hash = c.getString("version.hash")
    )
}

case class ConsumerVersion(number: String, major: Int, minor: Int, patch: Int, hash: String)
