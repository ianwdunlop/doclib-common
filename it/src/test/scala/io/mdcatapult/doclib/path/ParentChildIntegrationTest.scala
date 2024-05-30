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

package io.mdcatapult.doclib.path

import java.time.LocalDateTime
import java.util.UUID.randomUUID

import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.models.ParentChildMapping
import io.mdcatapult.doclib.models.metadata.{MetaInt, MetaString}
import io.mdcatapult.klein.mongo.Mongo
import io.mdcatapult.util.time.nowUtc
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.{equal => Mequal}
import org.mongodb.scala.model.Updates.combine
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ParentChildIntegrationTest  extends IntegrationSpec with BeforeAndAfter with ScalaFutures {

  implicit val config: Config = ConfigFactory.parseString(
    """
      |version {
      |  number = "2.0.6-SNAPSHOT",
      |  major = 2,
      |  minor =  0,
      |  patch = 6,
      |  hash =  "20837d29"
      |}
    """.stripMargin).withFallback(ConfigFactory.load())

  implicit val mongo: Mongo = new Mongo()

  implicit val collection: MongoCollection[ParentChildMapping] =
    mongo.getDatabase(config.getString("mongo.doclib-database")).getCollection(collectionName(suffix = "parent_child"))

  val created: LocalDateTime = nowUtc.now()

  before {
    Await.result(collection.deleteMany(combine()).toFuture(), Duration.Inf) // empty collection
  }

  "A parent child record" can "be stored" in {
    val metadataMap = List(MetaString("doi", "10.1101/327015"), MetaInt("a-value", 10))
    val parentChild = ParentChildMapping(_id = randomUUID(), parent = new ObjectId(), child = Some(new ObjectId()), childPath = "/a/path/to/child", metadata = Some(metadataMap), consumer = Some("consumer"))

    val doc = for {
      _ <- collection.insertOne(parentChild).toFuture()
      found <- collection.find(Mequal("_id", parentChild._id)).toFuture()
    } yield found

    whenReady(doc, longTimeout) { d => {
      assert(d.head._id == parentChild._id)
      assert(d.head.child == parentChild.child)
      assert(d.head.parent == parentChild.parent)
      assert(d.head.childPath == parentChild.childPath)
      assert(d.head.metadata == parentChild.metadata)
      assert(d.head.consumer == parentChild.consumer)
    }}
  }

}
