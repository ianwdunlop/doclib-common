package io.mdcatapult.doclib.concurrency
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

/** A stub implementation of LimitedExecution that makes no attempt to actually limit execution. */
class UnlimitedLimitedExecution extends LimitedExecution with LazyLogging {

  override def weighted[C, T](weight: Int)(c: C, label: String)(f: C => Future[T])(implicit ec: ExecutionContext): Future[T] =
    unlimited(c, label)(f)

  override def unlimited[C, T](c: C, label: String)(f: C => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    import System.{currentTimeMillis => time}
    val start = time()

    logger.debug("=> {}", label)

    val x = f(c)
    x.onComplete(_ => logger.debug("completed {} in {}ms", label, time() - start))
    x
  }

}
