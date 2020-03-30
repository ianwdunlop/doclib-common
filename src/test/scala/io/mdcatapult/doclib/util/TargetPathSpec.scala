package io.mdcatapult.doclib.util
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}

class TargetPathSpec extends FlatSpec with Matchers {

  implicit val config: Config = ConfigFactory.parseString(
    """
      |doclib {
      |  root: "test-assets"
      |  overwriteDerivatives: false
      |  local {
      |    target-dir: "local"
      |    temp-dir: "ingress"
      |  }
      |  remote {
      |    target-dir: "remote"
      |    temp-dir: "remote-ingress"
      |  }
      |  archive {
      |    target-dir: "archive"
      |  }
      |  derivative {
      |    target-dir: "derivatives"
      |  }
      |}
      |mongo {
      |  database: "prefetch-test"
      |  collection: "documents"
      |  connection {
      |    username: "doclib"
      |    password: "doclib"
      |    database: "admin"
      |    hosts: ["localhost"]
      |  }
      |}
    """.stripMargin)

  class MyTargetPath(implicit config: Config) extends TargetPath {
    override val doclibConfig: Config = config
  }

  val targetPath = new MyTargetPath

  "A ingress path " can "be converted to a local path" in {
    val source = "ingress/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.local.target-dir"))
    assert(target == "local/test.csv")
  }

  "A deeply nested ingress path " can "be converted to a equally nested local path" in {
    val source = "ingress/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.local.target-dir"))
    assert(target == "local/path/to/a/file/somewhere/test.csv")
  }

  "A deeply nested derivatives ingress path " can "be converted to a equally nested derivatives local path" in {
    val source = "ingress/derivatives/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.local.target-dir"))
    assert(target == "local/derivatives/path/to/a/file/somewhere/test.csv")
  }

  "A deeply nested double derivatives ingress path " can "be converted to a equally nested single derivative local path" in {
    val source = "ingress/derivatives/derivatives/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.local.target-dir"))
    assert(target == "local/derivatives/path/to/a/file/somewhere/test.csv")
  }


  "A local path " can "be converted to a ingress path" in {
    val source = "local/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.local.temp-dir"))
    assert(target == "ingress/test.csv")
  }

  "A deeply nested local path " can "be converted to a equally nested ingress path" in {
    val source = "local/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.local.temp-dir"))
    assert(target == "ingress/path/to/a/file/somewhere/test.csv")
  }

  "A deeply nested derivatives local path " can "be converted to a equally nested derivatives ingress path" in {
    val source = "local/derivatives/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.local.temp-dir"))
    assert(target == "ingress/derivatives/path/to/a/file/somewhere/test.csv")
  }

  "A deeply nested double derivatives local path " can "be converted to a equally nested single derivative ingress path" in {
    val source = "local/derivatives/derivatives/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.local.temp-dir"))
    assert(target == "ingress/derivatives/path/to/a/file/somewhere/test.csv")
  }

  "A deeply nested double derivatives local path with prefix " can "be converted to a equally nested single derivative ingress path with prefix" in {
    val source = "local/derivatives/derivatives/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.local.temp-dir"), Some("spreadsheet_conv-"))
    assert(target == "ingress/derivatives/path/to/a/file/somewhere/spreadsheet_conv-test.csv")
  }


  "A local path " can "be converted to a archive path" in {
    val source = "local/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.archive.target-dir"))
    assert(target == "archive/test.csv")
  }

  "A deeply nested local path " can "be converted to a equally nested archive path" in {
    val source = "local/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.archive.target-dir"))
    assert(target == "archive/path/to/a/file/somewhere/test.csv")
  }

  "A deeply nested derivatives local path " can "be converted to a equally nested derivatives archive path" in {
    val source = "local/derivatives/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.archive.target-dir"))
    assert(target == "archive/derivatives/path/to/a/file/somewhere/test.csv")
  }

  "A deeply nested double derivatives local path " can "be converted to a equally nested single derivative archive path" in {
    val source = "local/derivatives/derivatives/path/to/a/file/somewhere/test.csv"
    val target = targetPath.getTargetPath(source, config.getString("doclib.archive.target-dir"))
    assert(target == "archive/derivatives/path/to/a/file/somewhere/test.csv")
  }

}
