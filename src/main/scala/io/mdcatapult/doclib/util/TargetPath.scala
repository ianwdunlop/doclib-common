package io.mdcatapult.doclib.util

import java.nio.file.Paths

import com.typesafe.config.Config

trait TargetPath {

  val doclibConfig: Config

  /**
    * determines common root paths for two path string
    * @param paths List[String]
    * @return String common path component
    */
  protected def commonPath(paths: List[String]): String = {
    val SEP = "/"
    val BOUNDARY_REGEX = s"(?=[$SEP])(?<=[^$SEP])|(?=[^$SEP])(?<=[$SEP])"
    def common(a: List[String], b: List[String]): List[String] = (a, b) match {
      case (aa :: as, bb :: bs) if aa equals bb => aa :: common(as, bs)
      case _ => Nil
    }

    paths match {
      case Nil => ""
      case x :: Nil => x
      case _ =>
        val pathEntries = paths.map(_.split(BOUNDARY_REGEX).toList)
        val commonEntries = pathEntries.reduceLeft(common)

        commonEntries.mkString
    }
  }

  /**
    * generate new file path maintaining file path from origin but allowing for intersection of common root paths
    * @param source String
    * @return String full path to new target
    */
  def getTargetPath(source: String, target: String, prefix: Option[String] = None): String = {
    val targetRoot = target.replaceAll("/+$", "")
    val regex = """(.*)/(.*)$""".r

    source match {
      case regex(path, file) =>
        val c = commonPath(List(targetRoot, path))

        val targetPath =
          scrub(
            path
              .replaceAll(s"^$c", "")
              .replaceAll("^/+|/+$", "")
          )

        Paths.get(targetRoot, targetPath, s"${prefix.getOrElse("")}$file").toString
      case _ => source
    }
  }

  /**
    * Scrubs clean the start of a path in preparation to placing the path into a new doclib path directory.
    * Removes all leading sequences of "local", "ingress" from the start of a path.
    * Additionally it de-duplicates "derivatives" from the start.
    *
    * @param path to scrub repeated path entries from
    * @return scrubbed path
    */
  protected def scrub(path: String): String  = {
    val string = doclibConfig.getString _

    val localTempDir = string("doclib.local.temp-dir")
    val localTargetDir = string("doclib.local.target-dir")
    val d = string("doclib.derivative.target-dir")

    val ingressPath = s"^$localTempDir/?(.*)$$".r
    val localPath = s"^$localTargetDir/?(.*)$$".r
    val doubleDerivatives = s"^$d/($d/.*)$$".r

    path match {
      case ingressPath(remainder) => scrub(remainder)
      case localPath(remainder) => scrub(remainder)
      case doubleDerivatives(remainder) => scrub(remainder)
      case _ => path
    }
  }
}
