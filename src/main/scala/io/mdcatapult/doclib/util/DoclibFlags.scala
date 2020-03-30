package io.mdcatapult.doclib.util

import java.time.{LocalDateTime, ZoneOffset}

import com.typesafe.config.Config
import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.models.{ConsumerVersion, DoclibDoc, DoclibFlag, DoclibFlagState}
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.{BsonNull, ObjectId}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.{ExecutionContext, Future}

class DoclibFlags(key: String)(implicit collection: MongoCollection[DoclibDoc], config: Config, ex: ExecutionContext) {

  final class NotStarted(flag: String, doc: DoclibDoc, cause: Throwable = None.orNull)
    extends DoclibDocException(doc, f"Cannot '$flag' as flag '$key' has not been started", cause)

  protected val flags: String = "doclib"
  protected val flagKey = s"$flags.key"
  protected val flagVersion = s"$flags.$$.version"
  protected val flagHash = s"$flags.$$.hash"
  protected val flagStarted = s"$flags.$$.started"
  protected val flagEnded = s"$flags.$$.ended"
  protected val flagErrored = s"$flags.$$.errored"
  protected val flagReset = s"$flags.$$.reset"
  protected val flagState = s"$flags.$$.state"
  protected val flagSummary = s"$flags.$$.summary"


  protected def getVersion(ver: Config): ConsumerVersion = ConsumerVersion(
    number = ver.getString("number"),
    major =  ver.getInt("major"),
    minor = ver.getInt("minor"),
    patch = ver.getInt("patch"),
    hash = ver.getString("hash"))

  def getFlags(id: ObjectId): Future[List[DoclibFlag]] =
    collection.find(equal("_id", id)).toFuture()
      .map(_.toList.flatMap(_.doclib).filter(_.key == key))

  /**
   * function to self heal in the event duplicate flags appear. Assumes the latest flag is the most relevant and
   * retains that while removing flags with older started timestamps
   * @param doc DoclibDoc
   * @return
   */
  def deDuplicate(doc: DoclibDoc): Future[Option[UpdateResult]] = {

    import ImplicitOrdering.localDateOrdering

    getFlags(doc._id).flatMap(
      _.sortBy(_.started).reverse match {
        case _ :: Nil => Future.successful(None)
        case _ :: old =>
          collection.updateOne(
            equal("_id", doc._id),
            pullByFilter(combine(
              equal("doclib",
                combine(
                  equal("key", key),
                  in("started", old.map(_.started):_*)
                )
              )
            ))).toFutureOption()
        case _ => Future.successful(None)
      }
    )
  }


  /**
    * the document to start, assumes that if flag is present in DoclibDoc requires restart
    * ensures only one flag exists be making sure the update conditionally checks flagKey is not already present to
    * eliminate race condition for multiple instances of the same document being in flight
    * @param doc DoclibDoc
    * @return
    */
  def start(doc: DoclibDoc): Future[Option[UpdateResult]] =
    if (doc.hasFlag(key)) {
      restart(doc)
    } else {
      for {
        _ <- deDuplicate(doc)
        result <- collection.updateOne(
          combine(
            equal("_id", doc._id),
            nin(flagKey,List(key))),
          combine(push(flags, DoclibFlag(
            key = key,
            version = getVersion(config.getConfig("version")),
            started = LocalDateTime.now(ZoneOffset.UTC),
            summary = Some("started")
          )))
        ).toFutureOption()
      } yield result
    }


  /**
    * Set the started timestamp to the current time. Clear the
    * ended and errored timestamps.
    * @param doc the document to restart
    * @return
    */
  def restart(doc: DoclibDoc): Future[Option[UpdateResult]] =
    if (doc.hasFlag(key)) {
      for {
        _ <- deDuplicate(doc)
        result <- collection.updateOne(
          and(
            equal("_id", doc._id),
            equal(flagKey, key)),
          combine(
            currentDate(flagStarted),
            set(flagVersion, getVersion(config.getConfig("version"))),
            set(flagEnded, BsonNull()),
            set(flagErrored, BsonNull())
          )
        ).toFutureOption()
      } yield result
    } else Future.failed(new NotStarted("restart", doc))

  /**
   * Update ended flag. Updates the state if provided.
   *
   * @param doc   mongo document to update
   * @param state an optional DoclibFlagState object. If present then the state on the flag
   *              will be updated
   * @param noCheck should this be done without checking if the flag exists
   * @return
   */
  def end(doc: DoclibDoc, state: Option[DoclibFlagState] = None, noCheck: Boolean = false): Future[Option[UpdateResult]] = {
    if (noCheck || doc.hasFlag(key)) {
      for {
        _ <- deDuplicate(doc)
        result <- collection.updateOne(
          and(
            equal("_id", doc._id),
            equal(flagKey, key)),
          combine(
            set(flagReset, BsonNull()),
            set(flagSummary, "ended"),
            getStateUpdate(state)
          )
        ).toFutureOption()
      } yield result

    } else Future.failed(new NotStarted("end", doc))
  }

  /**
    *
    * @param doc mongo document to update
    * @param noCheck should this be done without checking if the flag exists
    * @return
    */
  def error(doc: DoclibDoc, noCheck: Boolean = false): Future[Option[UpdateResult]] =
    if (noCheck || doc.hasFlag(key)) {
      for {
        _ <- deDuplicate(doc)
        result <- collection.updateOne(
        and(
          equal("_id", doc._id),
          equal(flagKey, key)),
        combine(
          set(flagEnded, BsonNull()),
          set(flagReset, BsonNull()),
          set(flagSummary, "errored"),
          currentDate(flagErrored)
        )).toFutureOption()
      } yield result
    } else Future.failed(new NotStarted("error", doc))

  /**
    * Set the started and restart timestamp to the current time. Clear the
    * ended and errored timestamps.
    * @param doc the document to restart
    * @return
    */
  def reset(doc: DoclibDoc): Future[Option[UpdateResult]] =
    if (doc.hasFlag(key)) {
      for {
        _ <- deDuplicate(doc)
        result <- collection.updateOne(
          and(
            equal("_id", doc._id),
            equal(flagKey, key)),
          combine(
            currentDate(flagReset),
            set(flagVersion, getVersion(config.getConfig("version")))
          )
        ).toFutureOption()
      } yield result
    } else Future.failed(new NotStarted("reset", doc))

  /**
   * Create Bson update statement for a DoclibFlagState. If the state is
   * updated then the state updated time is set to the current date.
   *
   * @param state DoclibFlagState
   * @return
   */
  def getStateUpdate(state: Option[DoclibFlagState]): Bson = {
    state match {
      case None =>
        combine(
          currentDate(flagEnded),
          set(flagErrored, BsonNull())
        )
      case Some(state) =>
        combine(
          currentDate(flagEnded),
          set(flagErrored, BsonNull()),
          set(flagState, state)
        )
    }
  }
}
