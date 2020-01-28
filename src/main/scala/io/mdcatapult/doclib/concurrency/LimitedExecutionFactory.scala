package io.mdcatapult.doclib.concurrency

trait LimitedExecutionFactory {

  /** Create a LimitedExecution that supports a given the execution of functions against some data for a given level
    * of concurrency.
    *
    * @param concurrency level of concurrency, typically number of concurrent operations although this could be weighted
    * @return executor
    */
  def create(concurrency: Int): LimitedExecution
}
