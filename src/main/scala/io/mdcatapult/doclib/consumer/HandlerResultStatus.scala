/*
 * Copyright 2024 Medicines Discovery Catapult
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    * @param status        HandlerLogStatus: Received, Completed, or Failed
    * @param loggerMessages The message to log, this should normally be one of the string constants defined above
    * @param documentId    The id of the document
    * @return
    */
  def loggerMessage(status: HandlerLogStatus, documentId: String, loggerMessages: String*): String = {
    s"$status - ${loggerMessages.mkString(",")}, documentId: $documentId"
  }

  /**
    * @param status     HandlerLogStatus: Received, Completed, or Failed
    * @param documentId The id of document
    * @return
    */
  def loggerMessage(status: HandlerLogStatus, documentId: String): String = {
    s"$status - documentId: $documentId"
  }
}
