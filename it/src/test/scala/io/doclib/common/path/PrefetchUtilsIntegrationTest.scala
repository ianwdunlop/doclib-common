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

import org.apache.pekko.Done

import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import org.apache.pekko.actor.{ActorRef, ActorSystem}
import org.apache.pekko.testkit.TestKit
import com.rabbitmq.client.AMQP
import com.typesafe.config.{Config, ConfigFactory}
import io.doclib.common.messages.PrefetchMsg
import io.doclib.common.models.metadata.{MetaString, MetaValueUntyped}
import io.doclib.common.models.{DoclibDoc, FileAttrs}
import io.doclib.common.util.PrefetchUtils
import io.doclib.queue.Sendable
import io.doclib.util.time.nowUtc
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable
import scala.concurrent.Future

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
    override val persistent: Boolean = true

    override def send(envelope: PrefetchMsg, properties: Option[AMQP.BasicProperties]): Future[Done] = {
      sent.set(sent.get() + 1)
      messages ++= envelope.metadata.get
      Future.successful(Done)
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
