package io.mdcatapult.doclib.util

import better.files.{File => ScalaFile}

/**
 * Convenience method to delete a list of directories
 */
trait DirectoryDelete  {

  /**
   * Delete a list of directories from the file system
   * @param directories List of directories to be deleted
   */
  def deleteDirectories(directories: List[ScalaFile]): Unit = {
    directories.map(_.delete(true))
  }

}
