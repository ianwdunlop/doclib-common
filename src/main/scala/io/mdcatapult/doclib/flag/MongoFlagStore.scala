package io.mdcatapult.doclib.flag

import io.mdcatapult.klein.mongo.ResultConverters.toUpdatedResult
import io.mdcatapult.doclib.models.{DoclibDoc, DoclibDocExtractor, DoclibFlag, DoclibFlagState}
import io.mdcatapult.util.models.Version
import io.mdcatapult.util.models.result.UpdatedResult
import io.mdcatapult.util.time.{ImplicitOrdering, Now}
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.{BsonNull, ObjectId}
import org.mongodb.scala.model.Filters.{and, equal, in, nin}
import org.mongodb.scala.model.Updates.{combine, currentDate, pullByFilter, push, set}

import scala.concurrent.{ExecutionContext, Future}

/**
  * [[FlagStore]] implemented against documents that are stored in Mongo.
  *
  * @param collection Mongo collections
  * @param time gives current time
  * @param ec execution context
  *
  * @note flag deduplication occurs to avoid the rare circumstance when multiple flags are added with the same key.
  *       This assumes however that all flags have a different started timestamp.
  *       If not true then it is possible for all flags to be removed.
  */
class MongoFlagStore(
                      version: Version,
                      docExtractor: DoclibDocExtractor,
                      collection: MongoCollection[DoclibDoc],
                      time: Now,
                    )(implicit ec: ExecutionContext) extends FlagStore {

  override def findFlagContext(key: Option[String]): FlagContext = {
    val flagName = key.getOrElse(docExtractor.defaultFlagKey)

    new FlagContext {

      private def notStarted: (String, DoclibDoc) => Future[Nothing] =
        (flag: String, doc: DoclibDoc) =>
          Future.failed(
            NotStartedException(flagName)(flag, doc)
          )

      private val flagField = "doclib"
      private val flagKey = s"$flagField.key"
      private val flagVersion = s"$flagField.$$.version"
      private val flagStarted = s"$flagField.$$.started"
      private val flagEnded = s"$flagField.$$.ended"
      private val flagErrored = s"$flagField.$$.errored"
      private val flagReset = s"$flagField.$$.reset"
      private val flagState = s"$flagField.$$.state"
      private val flagSummary = s"$flagField.$$.summary"
      private val flagQueued = s"$flagField.$$.queued"

      override def isNotRunRecently(doc: DoclibDoc): Boolean =
        !docExtractor.isRunRecently(doc, Option(flagName))

      override def start(doc: DoclibDoc)(implicit ec: ExecutionContext): Future[UpdatedResult] =
        if (doc.hasFlag(flagName))
          restart(doc)
        else
          for {
            _ <- deDuplicate(doc)
            result <- collection.updateOne(
              combine(
                equal("_id", doc._id),
                nin(flagKey, List(flagName))),
              combine(push(flagField, DoclibFlag(
                key = flagName,
                version = version,
                started = Some(time.now()),
                summary = Some("started"),
                queued = Some(true)
              )))
            ).toFuture().map(toUpdatedResult)
          } yield result

      /**
       * Update the doclib ended flag with a new state if the NER occurrences have changed
       */
      override def end(
                        doc: DoclibDoc,
                        state: Option[DoclibFlagState] = None,
                        noCheck: Boolean = false
                      )(implicit ec: ExecutionContext): Future[UpdatedResult] =
        if (noCheck || doc.hasFlag(flagName)) {

          val stateUpdates = state.map(s => set(flagState, s)).view.toSeq

          val updates =
            Seq(
              currentDate(flagEnded),
              set(flagReset, BsonNull()),
              set(flagSummary, "ended"),
              set(flagErrored, BsonNull()),
              set(flagQueued, false)
            ) ++ stateUpdates

          for {
            _ <- deDuplicate(doc)
            result <- collection.updateOne(
              and(
                equal("_id", doc._id),
                equal(flagKey, flagName)),
              combine(
                updates: _*
              )
            ).toFuture().map(toUpdatedResult)
          } yield result

        } else notStarted("end", doc)

      override def error(
                          doc: DoclibDoc,
                          noCheck: Boolean = false
                        )(implicit ec: ExecutionContext): Future[UpdatedResult] =
        if (noCheck || doc.hasFlag(flagName)) {
          for {
            _ <- deDuplicate(doc)
            result <- collection.updateOne(
              and(
                equal("_id", doc._id),
                equal(flagKey, flagName)),
              combine(
                set(flagEnded, BsonNull()),
                set(flagReset, BsonNull()),
                set(flagSummary, "errored"),
                currentDate(flagErrored),
                set(flagQueued, false)
              )).toFuture().map(toUpdatedResult)
          } yield result
        } else notStarted("error", doc)

      def queue(doc: DoclibDoc): Future[UpdatedResult] =
        if (doc.hasFlag(flagName))
          for {
            _ <- deDuplicate(doc)
            result <- if (doc.getFlag(flagName).head.isNotQueued) {
              collection.updateOne(
                and(
                  equal("_id", doc._id),
                  equal(flagKey, flagName)),
                combine(
                  set(flagQueued, true)
                )
              ).toFuture().map(toUpdatedResult)
            } else Future.successful(UpdatedResult.nothing)
          } yield result
        else
          for {
            _ <- deDuplicate(doc)
            result <- collection.updateOne(
              combine(
                equal("_id", doc._id),
                nin(flagKey, List(flagName))),
              combine(push(flagField, DoclibFlag(
                key = flagName,
                // version not optional but doesn't really matter since it gets set on start, end etc.
                version = version,
                queued = Some(true)
              )))
            ).toFuture().map(toUpdatedResult)
          } yield result

      def reset(doc: DoclibDoc): Future[UpdatedResult] =
        if (doc.hasFlag(flagName)) {
          for {
            _ <- deDuplicate(doc)
            result <- collection.updateOne(
              and(
                equal("_id", doc._id),
                equal(flagKey, flagName)),
              combine(
                currentDate(flagReset),
                set(flagState, None.orNull),
                set(flagVersion, version),
                set(flagQueued, true)
              )
            ).toFuture().map(toUpdatedResult)
          } yield result
        } else notStarted("reset", doc)

      private def getFlags(id: ObjectId): Future[List[DoclibFlag]] =
        collection.find(equal("_id", id)).toFuture()
          .map(_.toList.flatMap(_.doclib).filter(_.key == flagName))

      /**
        * function to self heal in the event duplicate flags appear. Assumes the latest flag is the most relevant and
        * retains that while removing flags with older started timestamps.
        *
        * @param doc DoclibDoc
        * @return
        */
      private def deDuplicate(doc: DoclibDoc): Future[UpdatedResult] = {

        import ImplicitOrdering.localDateOrdering

        val timeOrderedFlags: Future[List[DoclibFlag]] =
          getFlags(doc._id).map(_.sortBy(_.started).reverse)

        timeOrderedFlags.flatMap {
          case _ :: Nil =>
            Future.successful(UpdatedResult.nothing)
          case _ :: old =>
            collection.updateOne(
              equal("_id", doc._id),
              pullByFilter(combine(
                equal("doclib",
                  combine(
                    equal("key", flagName),
                    in("started", old.map(_.started.orNull): _*)
                  )
                )
              ))).toFuture().map(toUpdatedResult)
          case _ =>
            Future.successful(UpdatedResult.nothing)
        }
      }

      /**
       * Set the started timestamp to the current time. Clear the
       * ended and errored timestamps.
       *
       * @param doc the document to restart
       * @return
       */
      private def restart(doc: DoclibDoc): Future[UpdatedResult] =
        if (doc.hasFlag(flagName)) {
          for {
            _ <- deDuplicate(doc)
            result <- collection.updateOne(
              and(
                equal("_id", doc._id),
                equal(flagKey, flagName)),
              combine(
                currentDate(flagStarted),
                set(flagVersion, version),
                set(flagEnded, BsonNull()),
                set(flagErrored, BsonNull()),
                set(flagQueued, true)
              )
            ).toFuture().map(toUpdatedResult)
          } yield result
        } else notStarted("restart", doc)
    }
  }
}
