package io.mdcatapult.doclib.consumer
import org.mongodb.scala.bson.ObjectId

sealed trait HandleLogStatus

case object Received extends HandleLogStatus
case object Completed extends HandleLogStatus
case object Failed extends HandleLogStatus

object HandleLogStatus {

  def loggerMessage(status: HandleLogStatus, loggerMessage: String, messageId: String): String = {
    s"$status - $loggerMessage, id: $messageId"
  }

  def loggerMessage(status: HandleLogStatus, messageId: String, doclibDocId: ObjectId): String = {
    s"$status - id: $messageId, doclib_doc_id: $doclibDocId"
  }

  def loggerMessage(status: HandleLogStatus, messageId: String): String = {
    s"$status - id: $messageId"
  }
}
