package io.mdcatapult.doclib.util

import java.time.LocalDateTime

import com.typesafe.config.Config
import io.mdcatapult.doclib.models.DoclibFlag
import org.bson.BsonDocument
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.result.UpdateResult

import scala.collection.JavaConverters._
import scala.concurrent.Future

class DoclibFlags(key: String)(implicit collection: MongoCollection[Document], config: Config) {

  protected val flags: String = config.getString("doclib.flags")
  protected val flagKey = s"$flags.key"
  protected val flagVersion = s"$flags.$$.version"
  protected val flagHash = s"$flags.$$.hash"
  protected val flagStarted = s"$flags.$$.started"
  protected val flagEnded = s"$flags.$$.ended"
  protected val flagErrored = s"$flags.$$.errored"


  /**
    * test if flag exists for key
    * @todo shoudl really use casting to DoclibFlag but for time purposes doesnt
    * @param doc Document
    * @return
    */
  def hasFlag(doc:Document): Boolean =
    doc(flags).asArray().toArray.exists(_.asInstanceOf[BsonDocument].getString("key").getValue == key)

  def start(doc: Document): Future[Option[UpdateResult]] = {
    if (hasFlag(doc)) {
      restart(doc)
    } else {
      collection.updateOne(
        equal("_id", doc.getObjectId("_id")),
        addToSet(flags, DoclibFlag(
          key = key,
          version = config.getInt("version.number"),
          hash = config.getString("version.hash"),
          started = LocalDateTime.now()
        ))
      ).toFutureOption()
    }
  }

  def restart(doc: Document): Future[Option[UpdateResult]] =
    if (hasFlag(doc)) {
      collection.updateOne(
        and(
          equal("_id", doc.getObjectId("_id")),
          equal(flagKey, key)),
        combine(
          currentDate(flagStarted),
          set(flagVersion, config.getInt("version.number")),
          set(flagHash, config.getString("version.hash")),
          set(flagEnded, BsonNull()),
          set(flagErrored, BsonNull())
        )).toFutureOption()
    } else Future.failed(new Exception(s"Cannot 'restart' as flag '$key' has not been started"))


  def end(doc: Document)(implicit collection: MongoCollection[Document], config: Config): Future[Option[UpdateResult]] =
    if (hasFlag(doc)) {
      collection.updateOne(
        and(
          equal("_id", doc.getObjectId("_id")),
          equal(flagKey, key)),
        combine(
          currentDate(flagEnded),
          set(flagErrored, BsonNull())
        )).toFutureOption()
    } else Future.failed(new Exception(s"Cannot 'end' as flag '$key' has not been started"))

  def error(doc: Document)(implicit collection: MongoCollection[Document], config: Config): Future[Option[UpdateResult]] =
    if (hasFlag(doc)) {
      collection.updateOne(
        and(
          equal("_id", doc.getObjectId("_id")),
          equal(flagKey, key)),
        combine(
          set(flagEnded, BsonNull()),
          currentDate(flagErrored)
        )).toFutureOption()
    } else Future.failed(new Exception(s"Cannot 'error' as flag '$key' has not been started"))
}
