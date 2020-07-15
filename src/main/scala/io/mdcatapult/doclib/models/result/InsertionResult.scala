package io.mdcatapult.doclib.models.result

object InsertionResult {

  /** Placeholder indicates that nothing was inserted, but request was accepted. */
  val nothing: InsertionResult = InsertionResult(acknowledged = true, 0)

  /** Result when a single item is successfully inserted. */
  val ok: InsertionResult = InsertionResult(acknowledged = true, 1)
}

/**
  * Result object from an insert request.
  * @param acknowledged true if database ack'ed the request
  * @param insertedCount number of database items that were inserted
  */
case class InsertionResult(acknowledged: Boolean, insertedCount: Long)
