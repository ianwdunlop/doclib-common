package io.mdcatapult.doclib.loader

import java.io.{BufferedInputStream, InputStream}

/**
  * Loader for Input Streams, just wraps in a BufferedInputStream for handling
  * @param is input stream to process
  */
class InputStreamLoader(is: InputStream) extends SourceLoader {

  /** input stream wrapped in BufferedInputStream **/
  val input = new BufferedInputStream(is)
}
