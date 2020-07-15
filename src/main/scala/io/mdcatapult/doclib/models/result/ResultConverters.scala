package io.mdcatapult.doclib.models.result

import org.mongodb.scala.result.{DeleteResult, InsertManyResult, InsertOneResult, UpdateResult}

/**
  * Convert Mongo return objects into simpler doclib result objects.
  * Doclib result objects should always be preferred whenever possible.
  */
object ResultConverters {

  def toDeletionResult(r: DeleteResult): DeletionResult =
    DeletionResult(r.wasAcknowledged, r.getDeletedCount)

  def toInsertionResult(r: InsertManyResult): InsertionResult =
    InsertionResult(r.wasAcknowledged, r.getInsertedIds.size)

  def toInsertionResult(r: InsertOneResult): InsertionResult =
    InsertionResult(r.wasAcknowledged, 1)

  def toUpdatedResult[T](r: UpdateResult): UpdatedResult =
    UpdatedResult(r.wasAcknowledged, r.getModifiedCount, r.getMatchedCount)
}
