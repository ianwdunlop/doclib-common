package io.mdcatapult.doclib.concurrency

/** Run a function against some data when there is a need to limit the number of concurrent executions.
  * Typically the calls take some data of type C which is passed into a function that returns a value of type T.
  * All calls are curried to encourage a style of coding where the function is placed inside {} only
  * and not (), which should look more natural.
  */
trait LimitedExecution {

  /** Run function against data with the default level of concurrency.
    *
    * @param c input data
    * @param f function that parses the data
    * @return result of applying function to the input data
    */
  def apply[C, T](c: C)(f: C => T): T

  /** Run function against data with the a weighted level of concurrency.
    * A weight of 1 is equivalent to unweighted.
    * A weight of 0 is equivalent to unlimited.
    *
    * @param c input data
    * @param f function that parses the data
    * @return result of applying function to the input data
    */
  def weighted[C, T](weight: Int)(c: C)(f: C => T): T

  /** Run function against data immediately.  There is no change to the amount of concurrency available.
    *
    * @param c input data
    * @param f function that parses the data
    * @return result of applying function to the input data
    */
  def unlimited[C, T](c: C)(f: C => T): T
}
