package io.mdcatapult.doclib.models

import java.util.UUID
import java.util.UUID.randomUUID

/**
  * Generates a random UUID.
  */
object UuidIdIterator extends IdIterator[UUID] {

  override def next(): UUID = randomUUID()
}
