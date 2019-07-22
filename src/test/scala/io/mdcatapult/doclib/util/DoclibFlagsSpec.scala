package io.mdcatapult.doclib.util

import java.time.{LocalDateTime, ZoneOffset}

import com.mongodb.async.SingleResultCallback
import org.mongodb.scala.{ClientSession, Document, MongoCollection}
import org.scalatest.FlatSpec
import com.mongodb.async.client.{MongoCollection ⇒ JMongoCollection}
import com.typesafe.config.{Config, ConfigFactory}
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.mongodb.scala.bson._
import org.mongodb.scala.result.UpdateResult
import org.scalamock.matchers.ArgCapture.{CaptureAll, CaptureOne}
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory

import scala.collection.JavaConverters._

class DoclibFlagsSpec extends FlatSpec with Matchers with MockFactory {

  implicit val config: Config = ConfigFactory.parseMap(Map[String, Any](
    "version.number" → "0",
    "version.hash" → "test",
    "doclib.flags" → "doclib"
  ).asJava)

  val wrappedCollection: JMongoCollection[Document] = mock[JMongoCollection[Document]]
  implicit val collection: MongoCollection[Document] = MongoCollection[Document](wrappedCollection)

  val codecs: CodecRegistry = MongoCodecs.get
  val started: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

  val newDoc = Document(List(
    "_id" → BsonObjectId(),
    "doclib" → BsonArray()
  ))

  val startedDoc = Document(List(
    "_id" → BsonObjectId(),
    "doclib" → BsonArray(Document(
      "key" → BsonString("test"),
      "version" → BsonInt32(0),
      "hash" → BsonBoolean(false),
      "started" → BsonDateTime(started),
      "ended" → BsonNull(),
      "errored" → BsonNull()
    ))
  ))

  "A 'started' document" should "return true when testing for the flag" in {
    val flags = new DoclibFlags("test")
    assert(flags.hasFlag(startedDoc))
  }

  it should "restart the document" in {
    (wrappedCollection.updateOne(_:Bson, _:Bson, _: SingleResultCallback[UpdateResult])).expects(where(
      (filter:Bson, update:Bson, _: SingleResultCallback[UpdateResult]) ⇒ {

        val f = filter.toBsonDocument(classOf[BsonDocument], codecs)
        assert(f.containsKey("doclib.key"))
        assert(f.getString("doclib.key").getValue == "test")

        val u = update.toBsonDocument(classOf[BsonDocument], codecs)
        assert(u.containsKey("$currentDate"))
        assert(u.getDocument("$currentDate").containsKey("doclib.$.started"))
        assert(u.getDocument("$set").containsKey("doclib.$.version"))
        assert(u.getDocument("$set").get("doclib.$.version").isInt32)
        assert(u.getDocument("$set").getInt32("doclib.$.version").getValue == config.getInt("version.number"))
        assert(u.getDocument("$set").containsKey("doclib.$.hash"))
        assert(u.getDocument("$set").get("doclib.$.hash").isString)
        assert(u.getDocument("$set").getString("doclib.$.hash").getValue == config.getString("version.hash"))
        assert(u.getDocument("$set").containsKey("doclib.$.ended"))
        assert(u.getDocument("$set").get("doclib.$.ended").isNull)
        assert(u.getDocument("$set").containsKey("doclib.$.errored"))
        assert(u.getDocument("$set").get("doclib.$.errored").isNull)
        true
      }
    ))
    val flags = new DoclibFlags("test")
    flags.start(startedDoc)
  }

  it should "end the document cleanly" in {
    (wrappedCollection.updateOne(_:Bson, _:Bson, _: SingleResultCallback[UpdateResult])).expects(where(
      (filter:Bson, update:Bson, _: SingleResultCallback[UpdateResult]) ⇒ {

        val f = filter.toBsonDocument(classOf[BsonDocument], org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY)
        assert(f.containsKey("doclib.key"))
        assert(f.getString("doclib.key").getValue == "test")

        val u = update.toBsonDocument(classOf[BsonDocument], codecs)
        assert(u.containsKey("$currentDate"))
        assert(u.getDocument("$currentDate").containsKey("doclib.$.ended"))
        assert(u.getDocument("$set").get("doclib.$.errored").isNull)
        true
      }
    ))
    val flags = new DoclibFlags("test")
    flags.end(startedDoc)
  }

  it should "error the document cleanly" in {
    (wrappedCollection.updateOne(_:Bson, _:Bson, _: SingleResultCallback[UpdateResult])).expects(where(
      (filter:Bson, update:Bson, _: SingleResultCallback[UpdateResult]) ⇒ {

        val f = filter.toBsonDocument(classOf[BsonDocument], codecs)
        assert(f.containsKey("doclib.key"))
        assert(f.getString("doclib.key").getValue == "test")

        val u = update.toBsonDocument(classOf[BsonDocument], codecs)
        assert(u.containsKey("$currentDate"))
        assert(u.getDocument("$currentDate").containsKey("doclib.$.errored"))
        assert(u.getDocument("$set").get("doclib.$.ended").isNull)
        true
      }
    ))
    val flags = new DoclibFlags("test")
    flags.error(startedDoc)
  }


  "A 'new' document" should "return false when testing for the flag" in {
    val flags = new DoclibFlags("missing")
    assert(!flags.hasFlag(newDoc))
  }

  it should "start the document cleanly" in {
    (wrappedCollection.updateOne(_:Bson, _:Bson, _: SingleResultCallback[UpdateResult])).expects(where(
      (filter:Bson, update:Bson, _: SingleResultCallback[UpdateResult]) ⇒ {

        val f = filter.toBsonDocument(classOf[BsonDocument], codecs)
        assert(f.containsKey("_id"))
        assert(f.get("_id").isObjectId)

        val u = update.toBsonDocument(classOf[BsonDocument], codecs)
        assert(u.containsKey("$addToSet"))
        assert(u.getDocument("$addToSet").containsKey("doclib"))
        assert(u.getDocument("$addToSet").getDocument("doclib").containsKey("key"))
        assert(u.getDocument("$addToSet").getDocument("doclib").getString("key").getValue == "test")
        assert(u.getDocument("$addToSet").getDocument("doclib").getInt32("version").getValue == config.getInt("version.number"))
        assert(u.getDocument("$addToSet").getDocument("doclib").getString("hash").getValue == config.getString("version.hash"))
        true
      }
    ))
    val flags = new DoclibFlags("test")
    flags.start(newDoc)
  }

}
