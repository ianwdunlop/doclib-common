package io.mdcatapult.doclib.exception
import io.mdcatapult.doclib.models.DoclibDoc

class DoclibDocException(doc: DoclibDoc,
                              message: String = "",
                              cause: Throwable = None.orNull) extends RuntimeException(message, cause) {
  def getDoc = doc
}
