package io.mdcatapult.doclib.concurrency

import java.util.concurrent.Semaphore

object SemaphoreLimitedExecution extends LimitedExecutionFactory {

  /** Create a SemaphoreLimitedExecution that controls concurrency using an encapsulated Semaphore that is fair.
    * Fair means that this semaphore will guarantee first-in first-out granting of permits under contention.
    *
    * @param concurrency number of semaphore permits
    * @return LimitedExecution configured with a semaphore
    */
  override def create(concurrency: Int): SemaphoreLimitedExecution = create(new Semaphore(concurrency, true))

  /** Create a SemaphoreLimitedExecution that controls concurrency with an exposed Semaphore.  Because the semaphore is
    * passed in it is possible that it is manipulated outside of this LimitedExecution, which could then leak permits.
    * As such it is generally safer to pass semaphore limits rather than an actual semaphore.
    *
    * @param s semaphore to control concurrency
    * @return LimitedExecution configured with a semaphore
    */
  def create(s: Semaphore): SemaphoreLimitedExecution = new SemaphoreLimitedExecution(s)
}

/** Control function execution concurrency using a JDK semaphore.  It is responsible for ensuring that acquired permits
  * are released once function execution has finished.
  *
  * @param s semaphore
  */
class SemaphoreLimitedExecution private (s: Semaphore) extends LimitedExecution {

  /** @inheritdoc */
  override def apply[C, T](c: C)(f: C => T): T = {
    s.acquire()
    try {
      f(c)
    } finally {
      s.release()
    }
  }

  /** @inheritdoc */
  override def weighted[C, T](weight: Int)(c: C)(f: C => T): T = {
    s.acquire(weight)
    try {
      f(c)
    } finally {
      s.release(weight)
    }
  }

  /** @inheritdoc */
  override def unlimited[C, T](c: C)(f: C => T): T = f(c)
}
