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
    if (paths.length < 2) paths.headOption.getOrElse("")
    else paths.map(_.split(BOUNDARY_REGEX).toList).reduceLeft(common).mkString
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
      case regex(path, file) ⇒
        val c = commonPath(List(targetRoot, path))
        val targetPath  = scrub(path.replaceAll(s"^$c", "").replaceAll("^/+|/+$", ""))
        Paths.get(targetRoot, targetPath, s"${prefix.getOrElse("")}$file").toString
      case _ ⇒ source
    }
  }

  /**
   * Replace repeated doclib path entries with single.
   * eg "/derivatives/derivatives" with "/derivatives"
   * @param path
   * @return
   */
  protected def scrub(path: String):String  = {
    val ingressPath = s"^${doclibConfig.getString("doclib.local.temp-dir")}/?(.*)$$".r
    val localPath = s"^${doclibConfig.getString("doclib.local.target-dir")}/?(.*)$$".r
    val d = doclibConfig.getString("doclib.derivative.target-dir")
    val doubleDerivatives = s"^$d/($d/.*)$$".r

    path match {
      case ingressPath(remainder) ⇒ scrub(remainder)
      case localPath(remainder) ⇒ scrub(remainder)
      case doubleDerivatives(remainder) ⇒ scrub(remainder)
      case _ ⇒ path
    }
  }
}
