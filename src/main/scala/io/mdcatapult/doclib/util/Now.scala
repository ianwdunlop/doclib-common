package io.mdcatapult.doclib.util

import java.time.LocalDateTime

trait Now {

  /** Get a timestamp of the current instant in time.
    */
  def now(): LocalDateTime
}
