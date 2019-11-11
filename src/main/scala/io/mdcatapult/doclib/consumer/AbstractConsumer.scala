package io.mdcatapult.doclib.consumer

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.spingo.op_rabbit.SubscriptionRef
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import com.typesafe.scalalogging.LazyLogging
import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.doclib.util.MongoCodecs
import io.mdcatapult.klein.mongo.Mongo
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoCollection
import scopt.OParser

import scala.concurrent.ExecutionContextExecutor

/**
  * default opt based config
  * @param config Config
  */
sealed case class ConsumerConfig(
                                  action: Option[String] = None,
                                  config: Config = ConfigFactory.load())

abstract class AbstractConsumer(name: String) extends App with LazyLogging{

  val consumerVersion: Config = ConfigFactory.load("version")
  val optConfig = getOptConfig
  implicit val config: Config = optConfig.config.withFallback(consumerVersion)

  logger.debug(config.root().render(ConfigRenderOptions.defaults()))

  def getOptConfig: ConsumerConfig = {
    val optBuilder = OParser.builder[ConsumerConfig]
    val optParser: OParser[Unit, ConsumerConfig] = {
      import optBuilder._
      OParser.sequence(
        programName(name),
        head(name, consumerVersion.getString("version.number")),
        version("version").text("prints the current version"),
        help("help").text("prints this usage text"),

        cmd("start")
          .action((_, c) ⇒ c.copy(action = Some("start")))
          .text("start the consumer")
          .children(
            opt[String]('c', "config")
              .action((x, c) ⇒ c.copy(config = ConfigFactory.parseFile(new File(x)).withFallback(c.config)))
              .text("optional: path to additional config for the consumer")

          )
      )
    }
    val consumerConfig = ConsumerConfig()
    OParser.parse(optParser, args, consumerConfig) match {
      case Some(c: ConsumerConfig) ⇒ c
      case None ⇒ sys.exit(1)
    }
  }

  def start()(implicit as: ActorSystem, materializer: ActorMaterializer, mongo: Mongo): SubscriptionRef

  optConfig.action match {
    case Some("start") ⇒
      /** initialise actor system **/
      implicit val system: ActorSystem = ActorSystem(name)
      implicit val materializer: ActorMaterializer = ActorMaterializer()
      implicit val executor: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
      /** Initialise Mongo **/
      implicit val codecs: CodecRegistry = MongoCodecs.get
      implicit val mongo: Mongo = new Mongo()
      start
    case Some(_) ⇒
      println(s"${optConfig.action} is not a recognised action")
      sys.exit(1)
    case None ⇒
      println("You must specify a command valid command or `start`, see --help for more information")
      sys.exit(1)
  }

}
