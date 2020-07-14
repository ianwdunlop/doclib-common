package io.mdcatapult.doclib.util
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

object AdvancingNow {

  /**
    * AdvancingNow starting from the current time in UTC.
    * @return
    */
  def fromCurrentTime(): AdvancingNow =
    new AdvancingNow(nowUtc.now())
}

/**
  * Test implementation of Now that ensures that each generated timestamp differs from the the one before
  * by a millisecond.
  *
  * @param start the first timestamp to be generated
  */
class AdvancingNow(val start: LocalDateTime) extends Now {

  private val offset = new AtomicInteger()

  override def now(): LocalDateTime =
    start.plusNanos(offset.getAndIncrement() * 1000 * 1000)
}
