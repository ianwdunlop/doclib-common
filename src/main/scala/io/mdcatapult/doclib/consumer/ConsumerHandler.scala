package io.mdcatapult.doclib.consumer

import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.klein.queue.Envelope

import scala.concurrent.Future

trait ConsumerHandler {

  def handle(message: Envelope): Future[Option[DoclibDoc]]

}
