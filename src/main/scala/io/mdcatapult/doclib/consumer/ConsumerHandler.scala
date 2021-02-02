package io.mdcatapult.doclib.consumer

import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.klein.queue.Envelope

import scala.concurrent.Future

trait ConsumerHandler[T <: Envelope] {

  /**
   * Process a message and return a DoclibDoc. The handle method is passed into a
   * Queue when subscribing to it and acts as a callback for any messages that the
   * queue receives. The key (ie exchange) is extracted from the message within
   * the Queue subscribe method.
   * @param message One of the various Envelopes eg PrefetchMsg, SupervisorMsg
   * @param key AMQP Exchange
   * @return DoclibDoc
   */
  def handle(message: T, key: String): Future[Option[DoclibDoc]]

}
