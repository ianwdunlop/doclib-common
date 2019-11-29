package io.mdcatapult.doclib.util

import com.typesafe.config.Config
import io.mdcatapult.doclib.messages.PrefetchMsg
import io.mdcatapult.doclib.models.{DoclibDoc, Origin}
import io.mdcatapult.doclib.models.metadata.{MetaString, MetaValueUntyped}
import io.mdcatapult.klein.queue.Sendable

import cats.syntax.option._

import scala.concurrent.Future

trait PrefetchUtils {

  val doclibConfig: Config
  val prefetchQueue: Sendable[PrefetchMsg]
  val derivativeType: String

  def enqueue(source: List[String], doc: DoclibDoc): List[String] = {
    // Let prefetch know that it is an unarchived derivative
    val derivativeMetadata = List[MetaValueUntyped](MetaString("derivative.type", derivativeType))
    source.foreach(path ⇒ {
      prefetchQueue.send(PrefetchMsg(
        source = path,
        origin = Some(List(Origin(
          scheme = "mongodb",
          metadata = List(
            MetaString("db", doclibConfig.getString("mongo.database")),
            MetaString("collection", doclibConfig.getString("mongo.collection")),
            MetaString("_id", doc._id.toHexString)).some
        ))),
        tags = doc.tags,
        metadata = (doc.metadata.getOrElse(Nil) ::: derivativeMetadata).some,
        derivative =  true.some
      ))
    })
    source
  }
}
