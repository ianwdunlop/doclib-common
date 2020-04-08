package io.mdcatapult.doclib.util

import java.time.LocalDateTime
import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.models.ParentChildMapping
import io.mdcatapult.klein.mongo.Mongo
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.{equal => Mequal}
import org.mongodb.scala.model.Updates.combine
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ParentChildIntegrationTest  extends AnyFlatSpec with Matchers with BeforeAndAfter with ScalaFutures {

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

  val coreCodecs: CodecRegistry = MongoCodecs.get
  implicit val codecs: CodecRegistry = fromRegistries(fromCodecs(new NullWritableLocalDateTime(coreCodecs)), coreCodecs)

  implicit val mongo: Mongo = new Mongo()

  implicit val collection: MongoCollection[ParentChildMapping] =
    mongo.database.getCollection(s"${config.getString("mongo.collection")}_parent_child")

  val created: LocalDateTime = nowUtc.now()

  before {
    Await.result(collection.deleteMany(combine()).toFuture(), Duration.Inf) // empty collection
  }

  "A parent child record" can "be stored" in {
    val parentChild = ParentChildMapping(_id = UUID.randomUUID, parent = new ObjectId, child = new ObjectId)
    val doc = for {
      _ <- collection.insertOne(parentChild).toFuture()
      found <- collection.find(Mequal("_id", parentChild._id)).toFuture()
    } yield found
    whenReady(doc) { d => {
      assert(d.head._id == parentChild._id)
      assert(d.head.child == parentChild.child)
      assert(d.head.parent == parentChild.parent)
    }}
  }

}
