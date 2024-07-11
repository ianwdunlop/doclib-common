/*
 * Copyright 2024 Medicines Discovery Catapult
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.doclib.common.path

import java.nio.file.Paths
import com.typesafe.config.Config

import java.io.File
import scala.annotation.tailrec

trait TargetPath {

  /**
    * determines common root paths for two path string
    * @param paths List[String]
    * @return String common path component
    */
  protected def commonPath(paths: List[String]): String = {
    val SEP = "/"
    val BOUNDARY_REGEX = s"(?=[$SEP])(?<=[^$SEP])|(?=[^$SEP])(?<=[$SEP])"

    def common(a: List[String], b: List[String]): List[String] =
      (a, b) match {
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
  def getTargetPath(source: String, target: String, prefix: Option[String] = None)(implicit config: Config): String = {
    val targetRoot = target.replaceAll("/+$", "")
    val regex = """(.*)/(.*)$""".r

    val archiveDirName = config.getString("doclib.archive.target-dir")

    deduplicateDerivatives(
      if (targetRoot == archiveDirName)
        Paths.get(targetRoot, source).toString
      else
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
    )
  }

  private def deduplicateDerivatives(path: String)(implicit config: Config): String = {
    val derivative = config.getString("doclib.derivative.target-dir")
    val doubleDerivatives = s"$derivative${File.separator}$derivative"

    @tailrec
    def deduplicate(p: String): String = {
      val deduplicated = p.replace(doubleDerivatives, derivative)

      if (deduplicated == p)
        p
      else
        deduplicate(deduplicated)
    }

    deduplicate(path)
  }

  /**
    * Scrubs clean the start of a path in preparation to placing the path into a new doclib path directory.
    * Removes all leading sequences of "local", "remote", "ingress" from the start of a path.
    * Additionally it de-duplicates "derivatives" from the start.
    *
    * @param path to scrub repeated path entries from
    * @return scrubbed path
    */
  private def scrub(path: String)(implicit config: Config): String  = {
    val localTempDir = config.getString("doclib.local.temp-dir")
    val localTargetDir = config.getString("doclib.local.target-dir")
    val remoteTargetDir = config.getString("doclib.remote.target-dir")

    val ingressPath = s"^$localTempDir/?(.*)$$".r
    val localPath = s"^$localTargetDir/?(.*)$$".r
    val remotePath = s"^$remoteTargetDir/?(.*)$$".r

    @tailrec
    def scrubStart(p: String): String =
      p match {
        case ingressPath(subPath) => scrubStart(subPath)
        case localPath(subPath) => scrubStart(subPath)
        case remotePath(subPath) => scrubStart(subPath)
        case _ => p
      }

    scrubStart(path)
  }
}
