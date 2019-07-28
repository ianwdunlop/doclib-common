package io.mdcatapult.doclib.util

import java.time.{LocalDateTime, ZoneOffset}

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

object DoclibFlags {
  /**
    * test if flag exists for key
    * @todo shoudl really use casting to DoclibFlag but for time purposes doesnt
    * @param doc Document
    * @return
    */
  def hasFlag(key: String, doc:Document, flags: String): Boolean =
    doc(flags).asArray().toArray.exists(_.asInstanceOf[BsonDocument].getString("key").getValue == key)

  def getFlag(key: String, doc:Document, flags: String): List[DoclibFlag] =
    doc(flags).asArray().toArray.toList
      .filter(_.asInstanceOf[BsonDocument].getString("key").getValue == key)
      .map(bd ⇒ {
        val d = bd.asInstanceOf[BsonDocument]
        DoclibFlag(
          key=d.getString("key").getValue,
          version=d.getDouble("version").getValue.toDouble,
          hash=d.getString("hash").getValue,
          started=LocalDateTime.ofEpochSecond(d.getDateTime("started").getValue, 0, ZoneOffset.UTC),
          ended=if (d.containsKey("ended") && d.get("ended").isDateTime)
            Some(LocalDateTime.ofEpochSecond(d.getDateTime("ended").getValue, 0, ZoneOffset.UTC))
          else
            None,
          errored=if (d.containsKey("errored") && d.get("errored").isDateTime)
            Some(LocalDateTime.ofEpochSecond(d.getDateTime("errored").getValue, 0, ZoneOffset.UTC))
          else
            None
        )
      })
}

class DoclibFlags(key: String)(implicit collection: MongoCollection[Document], config: Config) {

  protected val flags: String = config.getString("doclib.flags")
  protected val flagKey = s"$flags.key"
  protected val flagVersion = s"$flags.$$.version"
  protected val flagHash = s"$flags.$$.hash"
  protected val flagStarted = s"$flags.$$.started"
  protected val flagEnded = s"$flags.$$.ended"
  protected val flagErrored = s"$flags.$$.errored"


  def start(doc: Document): Future[Option[UpdateResult]] = {
    if (DoclibFlags.hasFlag(key, doc, flags)) {
      restart(doc)
    } else {
      collection.updateOne(
        equal("_id", doc.getObjectId("_id")),
        addToSet(flags, DoclibFlag(
          key = key,
          version = config.getDouble("version.number"),
          hash = config.getString("version.hash"),
          started = LocalDateTime.now()
        ))
      ).toFutureOption()
    }
  }

  def restart(doc: Document): Future[Option[UpdateResult]] =
    if (DoclibFlags.hasFlag(key, doc, flags)) {
      collection.updateOne(
        and(
          equal("_id", doc.getObjectId("_id")),
          equal(flagKey, key)),
        combine(
          currentDate(flagStarted),
          set(flagVersion, config.getDouble("version.number")),
          set(flagHash, config.getString("version.hash")),
          set(flagEnded, BsonNull()),
          set(flagErrored, BsonNull())
        )).toFutureOption()
    } else Future.failed(new Exception(s"Cannot 'restart' as flag '$key' has not been started"))


  def end(doc: Document)(implicit collection: MongoCollection[Document], config: Config): Future[Option[UpdateResult]] =
    if (DoclibFlags.hasFlag(key, doc, flags)) {
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
    if (DoclibFlags.hasFlag(key, doc, flags)) {
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
