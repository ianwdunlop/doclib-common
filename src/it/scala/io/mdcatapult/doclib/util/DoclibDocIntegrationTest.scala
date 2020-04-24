package io.mdcatapult.doclib.util

import java.time.LocalDateTime
import java.util.UUID.randomUUID

import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.klein.mongo.Mongo
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.{equal => Mequal}
import org.mongodb.scala.model.Updates.combine
import org.scalatest.BeforeAndAfter
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class DoclibDocIntegrationTest extends IntegrationSpec with BeforeAndAfter with ScalaFutures {

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

  implicit val collection: MongoCollection[DoclibDoc] =
    mongo.database.getCollection(collectionName(suffix = "doclibdoc"))

  val created: LocalDateTime = nowUtc.now()

  before {
    Await.result(collection.deleteMany(combine()).toFuture(), Duration.Inf) // empty collection
  }

  "A DoclibDoc" should "be stored with a UUID" in {
    val newDoc: DoclibDoc = DoclibDoc(
      _id = new ObjectId,
      source = "/path/to/new.txt",
      hash = "0123456789",
      mimetype =  "text/plain",
      created =  created,
      updated =  created,
      uuid = Option(randomUUID())
    )

    val written = collection.insertOne(newDoc).toFutureOption()
    val read = written.flatMap(_ => collection.find(Mequal("_id", newDoc._id)).toFuture())

    whenReady(read) { doc =>
      doc.headOption.value.uuid should be(newDoc.uuid)
    }
  }

  it should "retrieve older DoclibDocs that have no UUID" in {
    val newDoc: DoclibDoc = DoclibDoc(
      _id = new ObjectId,
      source = "/path/to/new.txt",
      hash = "0123456789",
      mimetype =  "text/plain",
      created =  created,
      updated =  created
    )

    val written = collection.insertOne(newDoc).toFutureOption()
    val read = written.flatMap(_ => collection.find(Mequal("_id", newDoc._id)).toFuture())

    whenReady(read) { doc =>
      doc.headOption.value.uuid should be(None)
    }
  }
}
