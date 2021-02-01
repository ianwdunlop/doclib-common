package io.mdcatapult.doclib.consumer

import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.klein.queue.Envelope

import scala.concurrent.Future

trait ConsumerHandler[T <: Envelope] {

  /**
   * Process a message and return a DoclibDoc.
   * @param message One of the various Envelopes eg PrefetchMsg, SupervisorMsg
   * @param key
   * @return DoclibDoc
   */
  def handle(message: T, key: String): Future[Option[DoclibDoc]]

}
