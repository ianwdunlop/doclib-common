package io.mdcatapult.doclib.models.result

object DeletionResult {

  /** Result when a single item is successfully deleted. */
  val ok: DeletionResult = DeletionResult(acknowledged = true, deletedCount = 1)
}

/**
  * Result object from a delete request.
  * @param acknowledged true if database ack'ed the request
  * @param deletedCount number of database items that were deleted
  */
case class DeletionResult(acknowledged: Boolean, deletedCount: Long)
