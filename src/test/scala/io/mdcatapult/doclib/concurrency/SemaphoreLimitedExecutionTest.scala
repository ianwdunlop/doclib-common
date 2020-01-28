package io.mdcatapult.doclib.concurrency

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SemaphoreLimitedExecutionTest extends FlatSpec with Matchers {

  private val concurrentTestTimeout = Timeout(Span(1, Seconds))

  "A SemaphoreLimitedExecution" should "execute function when concurrency is available" in {
    val executor = SemaphoreLimitedExecution.create(1)

    executor("run") { "has " + _ } should be("has run")
  }

  it should "allow additional call once an earlier one has finished" in {
    val executor = SemaphoreLimitedExecution.create(1)

    executor("run") { "has " + _ }
    executor("run later") { "has " + _ } should be("has run later")
  }

  it should "execute with a weight a function when sufficient concurrency is available" in {
    val executor = SemaphoreLimitedExecution.create(5)

    executor.weighted(5)("run") { "has " + _ } should be("has run")
  }

  it should "allow additional weighted call once an earlier one has finished" in {
    val executor = SemaphoreLimitedExecution.create(5)

    executor.weighted(5)("run") { "has " + _ }
    executor.weighted(5)("run later") { "has " + _ } should be("has run later")
  }

  it should "unlimited call does not affect concurrency" in {
    val executor = SemaphoreLimitedExecution.create(1)

    executor.unlimited("unlimited") { x => {
      executor("run after") { y => s"has $y $x" }
    }} should be("has run after unlimited")
  }

  it should "allow unlimited call run even if concurrency is exhausted" in {
    val executor = SemaphoreLimitedExecution.create(1)

    executor("run") { x => {
      executor.unlimited("no concurrency left") { y => s"has $x with $y" }
    }} should be("has run with no concurrency left")
  }

  it should "let at most 1 function run concurrently when concurrency limit is 1" in {
    val executor = SemaphoreLimitedExecution.create(1)

    whenReady(runFunctionsConcurrently(executor.apply)) { maxConcurrency =>
      maxConcurrency should be(1)
    }
  }

  it should "let at most 2 functions run concurrently when concurrency limit is 2" in {
    val executor = SemaphoreLimitedExecution.create(2)

    whenReady(runFunctionsConcurrently(executor.apply)) { maxConcurrency =>
      maxConcurrency should be(2)
    }
  }

  it should "let at most 1 function of weight 3 run concurrently when concurrency limit is 3" in {
    val executor = SemaphoreLimitedExecution.create(3)

    whenReady(runFunctionsConcurrently(executor.weighted(3))) { maxConcurrency =>
      maxConcurrency should be(1)
    }
  }

  it should "let at most 2 functions of weight 3 run concurrently when concurrency limit is 6" in {
    val executor = SemaphoreLimitedExecution.create(6)

    whenReady(runFunctionsConcurrently(executor.weighted(3)), concurrentTestTimeout) { maxConcurrency =>
      maxConcurrency should be(2)
    }
  }

  /** Run functions concurrently get return the maximum number of concurrently running functions.
   * It lines up a set of Future by holding them all on a countdown latch until all are ready.  Once there they are all
   * released to be then throttled by the LimitedExecution.  The caller supplies a method of SemaphoreLimitedExecution
   * to run, such as SemaphoreLimitedExecution.weighted(3).  A function is run against this that increments a count of
   * all functions currently running which is returned before decrementing the currently running count.
   *
   * @param f a function of SemaphoreLimitedExecution that is to run a function, such as apply() or weighted()
   * @return a future holding the maximum number of functions that were concurrently running
   */
  def runFunctionsConcurrently(f: Int => (Int => Int) => Int): Future[Int] = {
    val latch = new CountDownLatch(25)

    val running = new AtomicInteger(0)

    val concurrentRunningCounts: Seq[Future[Int]] =
      0.to(latch.getCount.toInt).map( _ =>
        Future {
            latch.countDown()
            f(0)( (_: Int) => {
              try {
                running.incrementAndGet()
              } finally {
                Thread.sleep(1, 0)
                running.decrementAndGet()
              }
            })
          }
      )

    Future.sequence(concurrentRunningCounts).map(_.max)
  }
}
