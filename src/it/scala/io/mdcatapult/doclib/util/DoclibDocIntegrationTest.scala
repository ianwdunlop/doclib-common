package io.mdcatapult.doclib.util

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.models.DoclibDoc
import io.mdcatapult.klein.mongo.Mongo
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Updates.combine
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues._
import org.mongodb.scala.model.Filters.{equal => Mequal}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class DoclibDocIntegrationTest extends AnyFlatSpec with Matchers with BeforeAndAfter with ScalaFutures {

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

  implicit val collection: MongoCollection[DoclibDoc] =
    mongo.database.getCollection(s"${config.getString("mongo.collection")}_doclibdoc")

  val now: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

  before {
    Await.result(collection.deleteMany(combine()).toFuture(), Duration.Inf) // empty collection
  }

  "A DoclibDoc" should "be stored with a UUID" in {
    val newDoc: DoclibDoc = DoclibDoc(
      _id = new ObjectId,
      source = "/path/to/new.txt",
      hash = "0123456789",
      mimetype =  "text/plain",
      created =  now,
      updated =  now,
      uuid = Option(UUID.randomUUID())
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
      created =  now,
      updated =  now
    )

    val written = collection.insertOne(newDoc).toFutureOption()
    val read = written.flatMap(_ => collection.find(Mequal("_id", newDoc._id)).toFuture())

    whenReady(read) { doc =>
      doc.headOption.value.uuid should be(None)
    }
  }
}
