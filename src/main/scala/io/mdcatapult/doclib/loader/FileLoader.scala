package io.mdcatapult.doclib.loader

import java.io.{BufferedInputStream, FileInputStream}

/**
  * Loader for files based on supplied string path
  * @param file path for file to load
  */
class FileLoader(file: String) extends SourceLoader{

  /** FileInputStream for path wrapped in BufferedInputStream **/
  val input = new BufferedInputStream(new FileInputStream(file))
}