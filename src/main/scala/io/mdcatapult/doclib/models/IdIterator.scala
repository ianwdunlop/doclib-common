package io.mdcatapult.doclib.models

/**
  * Generates IDs.
  * @tparam T type of ID - typically UUID or ObjectID
  */
trait IdIterator[T] {

  /**
    * Generate the next ID.
    * @return ID
    */
  def next(): T
}
