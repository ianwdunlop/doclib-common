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

package io.doclib.common.flag

import io.doclib.util.models.result.UpdatedResult
import io.doclib.common.models.{DoclibDoc, DoclibFlagState}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Context through which this consumer's flag can be maintained for a given [[io.mdcatapult.doclib.models.DoclibDoc]].
 */
trait FlagContext {

  /**
   * Check if the document has already been processed recently for this flag context
   *
   * @param doc document
   * @return true if it hasn't been run recently
   */
  def isRunRecently(doc: DoclibDoc): Boolean

  /**
   * Declare that processing has started for a document.
   *
   * @param doc document to process
   * @param ec  execution context
   * @return result of update indicating if update succeeded
   */
  def start(doc: DoclibDoc)(implicit ec: ExecutionContext): Future[UpdatedResult]

  /**
   * Declare that processing of a document has finished successfully.
   *
   * @param doc     processed document
   * @param state   hashed result that can be used to quickly determine if anything has changed
   * @param noCheck if true then check if flag exists
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
   * @param noCheck if true then check if flag exists
   * @param ec      execution context
   * @return result of update indicating if update succeeded
   */
  def error(
             doc: DoclibDoc,
             noCheck: Boolean = false
           )(implicit ec: ExecutionContext): Future[UpdatedResult]

  /**
   * If doc is not currently queued then set queued to true otherwise do nothing.
   *
   * @param doc document that might be queued
   * @return
   */
  def queue(doc: DoclibDoc): Future[UpdatedResult]

  /**
   * Set the started and restart timestamp to the current time. Clear the
   * ended and errored timestamps. Set queued to true.
   *
   * @param doc the document to reset
   * @return
   */
  def reset(doc: DoclibDoc): Future[UpdatedResult]
}
