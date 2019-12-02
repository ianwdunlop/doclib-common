package io.mdcatapult.doclib.util

import com.typesafe.config.Config
import io.mdcatapult.doclib.messages.PrefetchMsg
import io.mdcatapult.doclib.models.{Derivative, DoclibDoc, Origin}
import io.mdcatapult.doclib.models.metadata.{MetaString, MetaValueUntyped}
import io.mdcatapult.klein.queue.Sendable
import cats.syntax.option._
import org.bson.types.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.addEachToSet
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.Future

trait PrefetchUtils {

  val doclibConfig: Config
  val prefetchQueue: Sendable[PrefetchMsg]
  val derivativeType: String
  val doclibCollection: MongoCollection[DoclibDoc]

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

  def persist(doc: DoclibDoc, files: List[String]): Future[Option[UpdateResult]] = {
    doclibCollection.updateOne(equal("_id", doc._id),
      addEachToSet("derivatives", files.map(path ⇒ Derivative(derivativeType, path)):_*),
    ).toFutureOption()
  }

  def fetch(id: String): Future[Option[DoclibDoc]] =
    doclibCollection.find(equal("_id", new ObjectId(id))).first().toFutureOption()
}
