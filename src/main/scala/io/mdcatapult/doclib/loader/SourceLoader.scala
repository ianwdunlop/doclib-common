package io.mdcatapult.doclib.loader

import java.io._

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.compress.archivers._
import org.apache.tika.Tika
import org.apache.tika.metadata._
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler

import scala.util.{Failure, Success, Try}

/**
  * Loader object to allow easy instantiation and detection of appropriate loader
  */
object SourceLoader {

  /**
    * Direct loader shortcut for InputStreams
    * @param is input stream of file
    * @return list of strings with file contents if identified as Archive stream list entries correlate to one per file
    */
  def load(is: InputStream): List[String]= new InputStreamLoader(is).load

  /**
    * Dynamic loader of file based on file path supplied
    * Uses apache tika to determin mimetype and load specific types as required
    * @param file path to file to be loaded
    * @return
    */
  def load(file: String): List[String] = new FileLoader(file).load
}

/**
  * Abstract source loader
  */
abstract class SourceLoader extends LazyLogging{

  /** abstract input value to be defined in super classes **/
  val input: BufferedInputStream

  /** list of valid extensions to be handled in archive format **/
  val validExtensions = List(
    "bz2", "csv", "docx", "htm", "html", "nam", "nam.gz", "nxml",
    "odp", "ods", "odt", "pdf", "pptx", "sgm", "sgml", "shtm", "shtml",
    "txt", "txt.gz", "tar", "tar.001", "tar.bz2", "tar.gz", "tbz2", "tgz",
    "xlsx", "xml", "xml.gz", "zip")


  /**
    * default load method to extract source as text
    * @return
    */
  def load: List[String] = archiveStream match {
    case _: BufferedInputStream => List(tikaExtract)
    case ais: ArchiveInputStream =>
      Iterator.continually(ais.getNextEntry)
        .takeWhile(_ != null)
        .filterNot(_.isDirectory)
        .filter(entry => validExtensions.exists(ext => entry.getName.matches(s".*\\.$ext$$")))
        .map(_ => {
          SourceLoader.load(ais).head
        }).toList
  }


  def tikaExtract: String =  {
    val tika = new Tika()
    val handler = new BodyContentHandler(-1)
    tika.getParser.parse(
      input,
      handler,
      new Metadata,
      new ParseContext)
    val content =  handler.toString
    logger.whenDebugEnabled({
      println(f"RAW Content length = ${scala.io.Source.fromInputStream(input).getLines().mkString("").length} characters")
      println()
      println("Content Loaded as string with Apache Tika")
      println(content)
    })
    content

  }


  /**
    * test if input is an archive and return appropriate stream of type
    * @return
    */
  protected def archiveStream: InputStream =
    Try(new ArchiveStreamFactory().createArchiveInputStream(input)) match {
      case Success(ais) => ais
      case Failure(_) => input
    }
}
