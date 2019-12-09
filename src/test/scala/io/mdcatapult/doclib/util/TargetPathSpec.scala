package io.mdcatapult.doclib.util
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}

class TargetPathSpec extends FlatSpec with Matchers {

  implicit var config: Config = ConfigFactory.parseString(
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
      |}
      |convert {
      |  format: "tsv"
      |  to: {
      |    path: "derivatives"
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
    override val consumerPath: String = "convert.to.path"
  }

  "A derivative" should "be ingested into doclib-root/temp-dir" in {
    val targetPath = new MyTargetPath
    val source = "local/test.csv"
    val target = targetPath.getTargetPath(source, config.getString(targetPath.consumerPath), Some("spreadsheet_conv"))
    assert(target == "ingress/derivatives/spreadsheet_conv-test.csv")
  }
}
