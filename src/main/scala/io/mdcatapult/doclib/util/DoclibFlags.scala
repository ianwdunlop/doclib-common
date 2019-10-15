package io.mdcatapult.doclib.util

import java.time.{LocalDateTime, ZoneOffset}

import com.typesafe.config.Config
import io.mdcatapult.doclib.models.{DoclibDoc, DoclibFlag}
import org.bson.BsonDocument
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.result.UpdateResult

import scala.collection.JavaConverters._
import scala.concurrent.Future

class DoclibFlags(key: String)(implicit collection: MongoCollection[DoclibDoc], config: Config) {

  protected val flags: String = "doclib"
  protected val flagKey = s"$flags.key"
  protected val flagVersion = s"$flags.$$.version"
  protected val flagHash = s"$flags.$$.hash"
  protected val flagStarted = s"$flags.$$.started"
  protected val flagEnded = s"$flags.$$.ended"
  protected val flagErrored = s"$flags.$$.errored"

  /**
    * the document to start
    * @param doc
    * @return
    */
  def start(doc: DoclibDoc): Future[Option[UpdateResult]] = {
    if (doc.hasFlag(key)) {
      restart(doc)
    } else {
      collection.updateOne(
        equal("_id", doc._id),
        addToSet(flags, DoclibFlag(
          key = key,
          version = config.getDouble("version.number"),
          hash = config.getString("version.hash"),
          started = LocalDateTime.now()
        ))
      ).toFutureOption()
    }
  }

  /**
    *
    * @param doc the document to restart
    * @return
    */
  def restart(doc: DoclibDoc): Future[Option[UpdateResult]] =
    if (doc.hasFlag(key)) {
      collection.updateOne(
        and(
          equal("_id", doc._id),
          equal(flagKey, key)),
        combine(
          currentDate(flagStarted),
          set(flagVersion, config.getDouble("version.number")),
          set(flagHash, config.getString("version.hash")),
          set(flagEnded, BsonNull()),
          set(flagErrored, BsonNull())
        )).toFutureOption()
    } else Future.failed(new Exception(s"Cannot 'restart' as flag '$key' has not been started"))


  /**
    *
    * @param doc mongo document to update
    * @param noCheck should this be done without checking if the flag exists
    * @return
    */
  def end(doc: DoclibDoc, noCheck: Boolean = false): Future[Option[UpdateResult]] =
    if (noCheck || doc.hasFlag(key)) {
      collection.updateOne(
        and(
          equal("_id", doc._id),
          equal(flagKey, key)),
        combine(
          currentDate(flagEnded),
          set(flagErrored, BsonNull())
        )).toFutureOption()
    } else Future.failed(new Exception(s"Cannot 'end' as flag '$key' has not been started"))

  /**
    *
    * @param doc mongo document to update
    * @param noCheck should this be done without checking if the flag exists
    * @return
    */
  def error(doc: DoclibDoc, noCheck: Boolean = false): Future[Option[UpdateResult]] =
    if (noCheck || doc.hasFlag(key)) {
      collection.updateOne(
        and(
          equal("_id", doc._id),
          equal(flagKey, key)),
        combine(
          set(flagEnded, BsonNull()),
          currentDate(flagErrored)
        )).toFutureOption()
    } else Future.failed(new Exception(s"Cannot 'error' as flag '$key' has not been started"))
}
