package io.mdcatapult.doclib.flag

import io.mdcatapult.util.models.result.UpdatedResult
import io.mdcatapult.doclib.models.DoclibFlagState

import scala.concurrent.{ExecutionContext, Future}

/**
  * Context through which this consumer's flag can be maintained for a given [[io.mdcatapult.doclib.models.DoclibDoc]].
  */
trait FlagContext {

  /**
    * Check if the document has already been processed recently for this flag context.
    * @return true if it hasn't been run recently
    */
  def isRunRecently(): Boolean

  /**
    * Declare that processing has started for a document.
    * @param ec execution context
    * @return result of update indicating if update succeeded
    */
  def start()(implicit ec: ExecutionContext): Future[UpdatedResult]

  /**
    * Declare that processing of a document has finished successfully.
    *
    * @param state hashed result that can be used to quickly determine if anything has changed
    * @param noCheck if true then check if flag exists
    * @param ec execution context
    * @return result of update indicating if update succeeded
    */
  def end(
           state: Option[DoclibFlagState] = None,
           noCheck: Boolean = false
         )(implicit ec: ExecutionContext): Future[UpdatedResult]

  /**
    * Declare that processing of a document has finished with an error.
    *
    * @param noCheck if true then check if flag exists
    * @param ec execution context
    * @return result of update indicating if update succeeded
    */
  def error(
             noCheck: Boolean = false
           )(implicit ec: ExecutionContext): Future[UpdatedResult]

  /**
   * If doc is not currently queued then set queued to true otherwise do nothing.
   * @return
   */
  def queue(): Future[UpdatedResult]

  /**
   * Set the started and restart timestamp to the current time. Clear the
   * ended and errored timestamps. Set queued to true.
   * @return
   */
  def reset(): Future[UpdatedResult]
}
