package io.mdcatapult.doclib.consumer


sealed trait HandlerLogStatus

case object Received extends HandlerLogStatus

case object Completed extends HandlerLogStatus

case object Failed extends HandlerLogStatus

object HandlerLogStatus {

  val NoDocumentError = "error_no_document"
  val UnknownError = "unknown_error"
  val DoclibDocumentException = "doclib_doc_exception"
  val ErrorFlagWriteError = "error_flag_write_error"

  /**
    *
    * @param status        HandlerLogStatus, Received, Completed, or Failed
    * @param loggerMessage The message to log, this should normally be one of the string constants defined above
    * @param messageId     The id of the message
    * @return
    */
  def loggerMessage(status: HandlerLogStatus, loggerMessage: String, messageId: String): String = {
    s"$status - $loggerMessage, id: $messageId"
  }

  /**
    *
    * @param status    HandlerLogStatus, Received, Completed, or Failed
    * @param messageId The id of the message
    * @return
    */
  def loggerMessage(status: HandlerLogStatus, messageId: String): String = {
    s"$status - id: $messageId"
  }
}
