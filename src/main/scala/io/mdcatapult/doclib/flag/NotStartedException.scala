package io.mdcatapult.doclib.flag

import io.mdcatapult.doclib.exception.DoclibDocException
import io.mdcatapult.doclib.models.DoclibDoc

object NotStartedException {

  def apply(key: String)(flag: String, doc: DoclibDoc): NotStartedException =
    NotStartedException(doc, flag, key)
}

case class NotStartedException(doc: DoclibDoc, flag: String, key: String)
  extends DoclibDocException(doc, s"Cannot '$flag' as flag '$key' has not been started")
