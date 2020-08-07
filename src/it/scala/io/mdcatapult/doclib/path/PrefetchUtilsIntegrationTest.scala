package io.mdcatapult.doclib.path

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKit
import com.spingo.op_rabbit.properties.MessageProperty
import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.messages.PrefetchMsg
import io.mdcatapult.doclib.models.metadata.{MetaString, MetaValueUntyped}
import io.mdcatapult.doclib.models.{DoclibDoc, FileAttrs}
import io.mdcatapult.doclib.util.PrefetchUtils
import io.mdcatapult.klein.queue.Sendable
import io.mdcatapult.util.time.nowUtc
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class PrefetchUtilsIntegrationTest extends TestKit(ActorSystem("PrefetchUtilsIntegrationTest", ConfigFactory.parseString(
  """
  akka.loggers = ["akka.testkit.TestEventListener"]
  """))) with AnyFlatSpecLike with Matchers with MockFactory with Eventually {

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

  // Fake the queues, we are not interacting with them
  class FakePrefetchQ extends Sendable[PrefetchMsg] {
    val name = "prefetch-message-queue"
    val rabbit: ActorRef = testActor
    val sent: AtomicInteger = new AtomicInteger(0)
    val messages: mutable.ListBuffer[MetaValueUntyped] = mutable.ListBuffer[MetaValueUntyped]()
    def send(envelope: PrefetchMsg,  properties: Seq[MessageProperty] = Seq.empty): Unit = {
      sent.set(sent.get() + 1)
      messages ++= envelope.metadata.get
    }
  }

  private val prefetchQ = new FakePrefetchQ

  class MyPrefetchUtils extends PrefetchUtils {
    override val doclibConfig: Config = config
    override val prefetchQueue: Sendable[PrefetchMsg] = prefetchQ
    override val derivativeType: String = "a.derivative.type"
    override val doclibCollection: MongoCollection[DoclibDoc] = None.orNull
  }

  private val source = "local/derivatives/derivatives/remote/http/a/path/test_doc.doc"
  private val metadata = List[MetaValueUntyped](MetaString("derivative.type","unarchived"), MetaString("derivative.type", "rawtext"), MetaString("key", "value"))

  private val createdTime = nowUtc.now()

  private val fileAttrs = {
    val path = new File(source).toPath
    FileAttrs(
      path = path.getParent.toAbsolutePath.toString,
      name = path.getFileName.toString,
      mtime = createdTime,
      ctime = createdTime,
      atime = createdTime,
      size = 5
    )
  }

  private val doc = DoclibDoc(
    _id = new ObjectId(),
    source = source,
    hash = "12345",
    created = createdTime,
    updated = createdTime,
    mimetype = "",
    attrs = Some(fileAttrs),
    metadata = Some(metadata)
  )

  private val prefetchUtils = new MyPrefetchUtils

  "Existing derivative.type metadata" should "be removed when message is sent" in {
    val source = prefetchUtils.enqueue(List("first/path", "second/path"), doc)
    assert(source.length == 2)
    // In theory this might not have happened in time so....eventually
    eventually {
      prefetchQ.messages.length == 4 &&
      prefetchQ.messages.distinct.length == 2
    }
  }

}
