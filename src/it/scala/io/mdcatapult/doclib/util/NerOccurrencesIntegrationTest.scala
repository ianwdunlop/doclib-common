package io.mdcatapult.doclib.util

import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.models.ner.{NerDocument, Occurrence}
import io.mdcatapult.klein.mongo.Mongo
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.{equal ⇒ Mequal}
import org.mongodb.scala.model.Updates.combine
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class NerOccurrencesIntegrationTest  extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  implicit val config: Config = ConfigFactory.load()
  val coreCodecs: CodecRegistry = MongoCodecs.get
  implicit val codecs: CodecRegistry = fromRegistries(fromCodecs(new NullWritableLocalDateTime(coreCodecs)), coreCodecs)

  implicit val mongo: Mongo = new Mongo()

  val nerCollection: MongoCollection[NerDocument] =
    mongo.database.getCollection(s"${config.getString("mongo.ner-collection")}_ner")
  val occurrenceCollection: MongoCollection[Occurrence] =
    mongo.database.getCollection(s"${config.getString("mongo.ner-collection")}_occurrences")

  override def beforeAll = {
    Await.result(nerCollection.deleteMany(combine()).toFuture(), Duration.Inf) // empty collection
    Await.result(occurrenceCollection.deleteMany(combine()).toFuture(), Duration.Inf) // empty collection
  }

  "An NER doc" can "be written to disk" in {
    val nerDoc = NerDocument(
      _id =  UUID.randomUUID(),
      value = "value",
      hash = "01234567890",
      entityType = Some("entity-type"),
      entityGroup = Some("entity-group"),
      resolvedEntity = Some("resolved-entity"),
      resolvedEntityHash = Some("resolved-entity-hash"),
      document = new ObjectId("5d9f0662679b3e75b2781c94")
    )
    val written = nerCollection.insertOne(nerDoc).toFutureOption()
    val read = written.flatMap(_ => nerCollection.find(Mequal("_id", nerDoc._id)).toFuture())

    whenReady(read) { doc =>
      doc.headOption.get._id should be(nerDoc._id)
      doc.headOption.get.entityType.get should be(nerDoc.entityType.get)
      doc.headOption.get.entityGroup.get should be(nerDoc.entityGroup.get)
    }
  }

  "An occurrence document" can "be written to disk" in {

    val occurrence = Occurrence(
      _id = UUID.randomUUID(),
      nerDocument = UUID.randomUUID(),
      characterStart = 12,
      characterEnd = 15,
      fragment = Option(UUID.randomUUID()),
      correctedValue = Option("fixed!"),
      correctedValueHash = Option("5e185e300268642a0fcbc964")
    )
    val written = occurrenceCollection.insertOne(occurrence).toFutureOption()
    val read = written.flatMap(_ => occurrenceCollection.find(Mequal("_id", occurrence._id)).toFuture())

    whenReady(read) { doc =>
      doc.headOption.get._id should be(occurrence._id)
      doc.headOption.get.nerDocument should be(occurrence.nerDocument)
      doc.headOption.get.`type` should be("document")
    }
  }

  "An occurrence fragment" can "be written to disk" in {
    val occurrence = Occurrence(
      _id = UUID.randomUUID(),
      nerDocument = UUID.randomUUID(),
      characterStart = 12,
      characterEnd = 15,
      fragment = Option(UUID.randomUUID()),
      correctedValue = Option("fixed!"),
      correctedValueHash = Option("5e185e300268642a0fcbc964"),
      wordIndex = Some(10)
    )
    val written = occurrenceCollection.insertOne(occurrence).toFutureOption()
    val read = written.flatMap(_ => occurrenceCollection.find(Mequal("_id", occurrence._id)).toFuture())

    whenReady(read) { doc =>
      doc.headOption.get._id should be(occurrence._id)
      doc.headOption.get.wordIndex.get should be(10)
      doc.headOption.get.fragment.get should be(occurrence.fragment.get)
      doc.headOption.get.`type` should be("fragment")
    }
  }

}
