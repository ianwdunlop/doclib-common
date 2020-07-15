package io.mdcatapult.doclib.flag

import io.mdcatapult.doclib.models.result.UpdatedResult
import io.mdcatapult.doclib.models.{DoclibDoc, DoclibFlagState}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Context through which this consumer's flag can be maintained for a given [[DoclibDoc]].
  */
trait FlagContext {

  /**
    * Check if the document has already been processed recently for this flag context.
    * @param doc document
    * @return true if it hasn't been run recently
    */
  def isNotRunRecently(doc: DoclibDoc): Boolean

  /**
    * Declare that processing has started for a document.
    * @param doc document to process
    * @param ec execution context
    * @return result of update indicating if update succeeded
    */
  def start(doc: DoclibDoc)(implicit ec: ExecutionContext): Future[UpdatedResult]

  /**
    * Declare that processing of a document has finished successfully.
    *
    * @param doc processed document
    * @param state hashed result that can be used to quickly determine if anything has changed
    * @param noCheck if true then check if flag exists
    * @param ec execution context
    * @return result of update indicating if update succeeded
    */
  def end(
           doc: DoclibDoc,
           state: Option[DoclibFlagState] = None,
           noCheck: Boolean = false
         )(implicit ec: ExecutionContext): Future[UpdatedResult]

  /**
    * Declare that processing of a document has finished with an error.
    *
    * @param doc processed document
    * @param noCheck if true then check if flag exists
    * @param ec execution context
    * @return result of update indicating if update succeeded
    */
  def error(
             doc: DoclibDoc,
             noCheck: Boolean = false
           )(implicit ec: ExecutionContext): Future[UpdatedResult]

  /**
   * If doc is not currently queued then set queued to true otherwise do nothing.
   * @param doc document that might be queued
   * @return
   */
  def queue(doc: DoclibDoc): Future[UpdatedResult]

  /**
   * Set the started and restart timestamp to the current time. Clear the
   * ended and errored timestamps. Set queued to true.
   * @param doc the document to restart
   * @return
   */
  def reset(doc: DoclibDoc): Future[UpdatedResult]
}
