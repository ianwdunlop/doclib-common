package io.mdcatapult.doclib.concurrency

import monix.execution.AsyncSemaphore

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

object SemaphoreLimitedExecution extends LimitedExecutionFactory {

  /** Create a SemaphoreLimitedExecution that controls concurrency using an encapsulated Semaphore that is fair.
    * Fair means that this semaphore will guarantee first-in first-out granting of permits under contention.
    *
    * @param concurrency number of semaphore permits
    * @return LimitedExecution configured with a semaphore
    */
  override def create(concurrency: Int): SemaphoreLimitedExecution = create(AsyncSemaphore(concurrency))

  /** Create a SemaphoreLimitedExecution that controls concurrency with an exposed Semaphore.  Because the semaphore is
    * passed in it is possible that it is manipulated outside of this LimitedExecution, which could then leak permits.
    * As such it is generally safer to pass semaphore limits rather than an actual semaphore.
    *
    * @param s semaphore to control concurrency
    * @return LimitedExecution configured with a semaphore
    */
  def create(s: AsyncSemaphore): SemaphoreLimitedExecution = new SemaphoreLimitedExecution(s)
}

/** Control function execution concurrency using a JDK semaphore.  It is responsible for ensuring that acquired permits
  * are released once function execution has finished.
  *
  * @param s semaphore
  */
class SemaphoreLimitedExecution private (s: AsyncSemaphore) extends LimitedExecution with LazyLogging  {

  /** @inheritdoc */
  override def weighted[C, T](weight: Int)(c: C, label: String)(f: C => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    logger.debug("Acquire lock of weight {} for {}", weight, label)
    s.acquireN(weight).flatMap { _ =>
      try {
        logger.debug("Lock acquired for {}", label)
        val result = f(c)
        result.onComplete(_ => {
          logger.debug("Release lock of weight {} for {}", weight, label)
          s.releaseN(weight)
        })
        result
      } catch {
        case e: Exception =>
          logger.debug("Release lock of weight {} for {} on error: {}", weight, label, e)
          s.releaseN(weight)
          throw e
      }
    }
  }

  /** @inheritdoc */
  override def unlimited[C, T](c: C, label: String)(f: C => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    logger.debug("Unlimited execution to run for {}", label)
    f(c)
  }
}
