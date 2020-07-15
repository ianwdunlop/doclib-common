package io.mdcatapult.doclib.models

import java.util.concurrent.atomic.AtomicInteger

object SeqIdIterator {

  /**
    * Generate a SeqIdIterator.
    * @param i iterator to get a sequence of IDs from
    * @param count number of IDs to generate
    * @tparam T ID type
    * @return initialised SeqIdIterator
    */
  def from[T](i: IdIterator[T], count: Int): SeqIdIterator[T] =
    new SeqIdIterator(1.to(count).map(_ => i.next()))
}

/**
  * IdIterator that "generate" IDs by stepping through a pre-generated sequence of IDs.
  * @param values sequence of IDs
  * @tparam T type of ID - typically UUID or ObjectID
  */
class SeqIdIterator[T](values: IndexedSeq[T]) extends IdIterator[T] {

  /** Index of the next ID. */
  private val index = new AtomicInteger()

  override def next(): T = values(index.getAndIncrement())

  /**
    * Get a new SeqIdIterator with the same values as this, but with the next value being back at the start.
    * @return refreshed copy
    */
  def refreshCopy(): IdIterator[T] = new SeqIdIterator(values)
}
