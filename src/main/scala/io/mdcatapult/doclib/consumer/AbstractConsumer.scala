package io.mdcatapult.doclib.consumer

import java.io.File
import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.spingo.op_rabbit.SubscriptionRef
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import com.typesafe.scalalogging.LazyLogging
import io.mdcatapult.doclib.models.ConsumerVersion
import io.mdcatapult.doclib.util.MongoCodecs
import io.mdcatapult.klein.mongo.Mongo
import io.mdcatapult.klein.queue.{Envelope, Queue}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import play.api.libs.json.Format
import scopt.OParser

abstract class AbstractConsumer(name: String, codecProviders: Seq[CodecProvider] = Nil) extends App with LazyLogging {

  private case class ConsumerConfig(
                                     action: Option[String] = None,
                                     config: Config = ConfigFactory.load())

  private def parseArgsWithConfiguration(): ConsumerConfig = {
    val optBuilder = OParser.builder[ConsumerConfig]
    val optParser: OParser[Unit, ConsumerConfig] = {
      import optBuilder._
      OParser.sequence(
        programName(name),
        head(name, consumerVersion.number),
        version("version").text("prints the current version"),
        help("help").text("prints this usage text"),

        cmd("start")
          .action((_, c) => c.copy(action = Some("start")))
          .text("start the consumer")
          .children(
            opt[String]('c', "config")
              .action((x, c) => c.copy(config = ConfigFactory.parseFile(new File(x)).withFallback(c.config)))
              .text("optional: path to additional config for the consumer")

          )
      )
    }
    val consumerConfig = ConsumerConfig()
    OParser.parse(optParser, args, consumerConfig) match {
      case Some(c: ConsumerConfig) => c
      case None => sys.exit(1)
    }
  }

  val consumerVersion: ConsumerVersion = ConsumerVersion.fromConfig(ConfigFactory.load("version"))

  private val optConfig: ConsumerConfig = parseArgsWithConfiguration()

  implicit val config: Config = optConfig.config

  private val initialised = new CountDownLatch(1)

  logger.debug(config.root().render(ConfigRenderOptions.concise()))

  def queue[T <: Envelope](property: String)(implicit f: Format[T], s: ActorSystem): Queue[T] = {
    val consumerName = config.getString("op-rabbit.consumer-name")

    new Queue[T](config.getString(property), consumerName = Option(consumerName))
  }

  def waitForInitialisation(timeout: Long, unit: TimeUnit): Unit = {
    initialised.await(timeout, unit)
  }

  def start()(implicit as: ActorSystem, m: Materializer, mongo: Mongo): SubscriptionRef

  optConfig.action match {
    case Some("start") =>
      // initialise actor system
      val system: ActorSystem = ActorSystem(name)
      val m: Materializer = Materializer(system)
      import system.dispatcher

      // Initialise Mongo
      implicit val codecs: CodecRegistry = MongoCodecs.include(codecProviders)
      val mongo: Mongo = new Mongo()
      val ref = start()(system, m, mongo)

      ref.initialized.foreach(_ => initialised.countDown())

    case Some(_) =>
      println(s"${optConfig.action} is not a recognised action")
      sys.exit(1)

    case None =>
      println("You must specify a command valid command or `start`, see --help for more information")
      sys.exit(1)
  }

}
