package io.mdcatapult.doclib.consumer

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import io.mdcatapult.klein.queue.Envelope
import io.mdcatapult.doclib.metrics.Metrics.handlerCount
import io.mdcatapult.doclib.models.{ConsumerNameAndQueue, DoclibDocExtractor}
import io.mdcatapult.util.models.Version

import scala.concurrent.Future

abstract class ConsumerHandler[T <: Envelope](implicit config: Config) extends LazyLogging {

  val docExtractor: DoclibDocExtractor = DoclibDocExtractor()
  val version: Version = Version.fromConfig(config)

  /**
   * Process a message and return a DoclibDoc. The handle method is passed into a
   * Queue when subscribing to it and acts as a callback for any messages that the
   * queue receives. The key (ie exchange) is extracted from the message within
   * the Queue subscribe method.
   *
   * @param message One of the various Envelopes eg PrefetchMsg, SupervisorMsg
   * @param key     AMQP Exchange
   * @return DoclibDoc
   */
  def handle(message: T, key: String): Future[Option[HandlerReturn]]

  def incrementHandlerCount(labels: String*)(implicit consumerNameAndQueue: ConsumerNameAndQueue): Unit = {
    val labelsWithConsumerInfo = Seq(consumerNameAndQueue.name, consumerNameAndQueue.queue) ++ labels
    handlerCount.labels(labelsWithConsumerInfo: _*).inc()
  }

}

// a handler's return type can extend this trait to avoid .asInstanceOf casting in tests
trait HandlerReturn

