package io.mdcatapult.doclib.util

import java.time.{LocalDateTime, ZoneId, ZoneOffset}

object FixedTimezoneNow {

  def utc(): FixedTimezoneNow =
    new FixedTimezoneNow(ZoneOffset.UTC)
}

class FixedTimezoneNow private (zone: ZoneId) extends Now {

  override def now(): LocalDateTime = LocalDateTime.now(zone)
}
