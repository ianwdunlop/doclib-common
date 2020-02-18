package io.mdcatapult.doclib.models

import java.time.LocalDateTime
import play.api.libs.json.{Format, Json, Reads, Writes}

object DoclibFlag  {
  implicit val prefetchOriginReader: Reads[DoclibFlag] = Json.reads[DoclibFlag]
  implicit val prefetchOriginWriter: Writes[DoclibFlag] = Json.writes[DoclibFlag]
  implicit val prefetchOriginFormatter: Format[DoclibFlag] = Json.format[DoclibFlag]
}

/**
  * Flag Object for
  * @param version version number
  * @param started when consumer started
  * @param ended when consumer ended
  */
case class DoclibFlag(
                       key: String,
                       version: ConsumerVersion,
                       started: LocalDateTime,
                       ended: Option[LocalDateTime] = None,
                       errored: Option[LocalDateTime] = None,
                       reset: Option[LocalDateTime] = None,
                       state: Option[DoclibFlagState] = None
                     )
