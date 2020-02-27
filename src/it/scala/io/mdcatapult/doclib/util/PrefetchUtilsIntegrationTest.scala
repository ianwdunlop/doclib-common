package io.mdcatapult.doclib.util

import java.io.File
import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKit
import com.mongodb.async.client.{MongoCollection => JMongoCollection}
import com.spingo.op_rabbit.properties.MessageProperty
import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.messages.PrefetchMsg
import io.mdcatapult.doclib.models.metadata.{MetaString, MetaValueUntyped}
import io.mdcatapult.doclib.models.{DoclibDoc, FileAttrs}
import io.mdcatapult.klein.queue.Sendable
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.{FlatSpecLike, Matchers}

import scala.collection.mutable

class PrefetchUtilsIntegrationTest extends TestKit(ActorSystem("PrefetchUtilsIntegrationTest", ConfigFactory.parseString(
  """
  akka.loggers = ["akka.testkit.TestEventListener"]
  """))) with FlatSpecLike with Matchers with MockFactory with Eventually {

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
  implicit val mongoCodecs: CodecRegistry = MongoCodecs.get
  val wrappedCollection: JMongoCollection[DoclibDoc] = stub[JMongoCollection[DoclibDoc]]
  implicit val collection: MongoCollection[DoclibDoc] = MongoCollection[DoclibDoc](wrappedCollection)

  // Fake the queues, we are not interacting with them
  class FakePrefetchQ extends Sendable[PrefetchMsg] {
    val name = "prefetch-message-queue"
    val rabbit: ActorRef = testActor
    val sent: AtomicInteger = new AtomicInteger(0)
    val messages: mutable.MutableList[MetaValueUntyped] = mutable.MutableList[MetaValueUntyped]()
    def send(envelope: PrefetchMsg,  properties: Seq[MessageProperty] = Seq.empty): Unit = {
      sent.set(sent.get() + 1)
      messages ++= envelope.metadata.get
    }
  }

  val prefetchQ = new FakePrefetchQ

  class MyPrefetchUtils extends PrefetchUtils {
    override val doclibConfig: Config = config
    override val prefetchQueue: Sendable[PrefetchMsg] = prefetchQ
    override val derivativeType: String = "a.derivative.type"
    override val doclibCollection: MongoCollection[DoclibDoc] = collection
  }

  val source = "local/derivatives/derivatives/remote/http/a/path/test_doc.doc"
  val metadata = List[MetaValueUntyped](MetaString("derivative.type","unarchived"), MetaString("derivative.type", "rawtext"), MetaString("key", "value"))
  val createdTime = LocalDateTime.now().toInstant(ZoneOffset.UTC)
  val path = new File(source).toPath
  val fileAttrs = FileAttrs(
    path = path.getParent.toAbsolutePath.toString,
    name = path.getFileName.toString,
    mtime = LocalDateTime.ofInstant(createdTime, ZoneOffset.UTC),
    ctime = LocalDateTime.ofInstant(createdTime, ZoneOffset.UTC),
    atime = LocalDateTime.ofInstant(createdTime, ZoneOffset.UTC),
    size = 5
  )
  val doc = DoclibDoc(
    _id = new ObjectId(),
    source = source,
    hash = "12345",
    created = LocalDateTime.ofInstant(createdTime, ZoneOffset.UTC),
    updated = LocalDateTime.ofInstant(createdTime, ZoneOffset.UTC),
    mimetype = "",
    attrs = Some(fileAttrs),
    metadata = Some(metadata)
  )
  val prefetchUtils = new MyPrefetchUtils

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
