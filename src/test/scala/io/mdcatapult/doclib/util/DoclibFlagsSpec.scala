package io.mdcatapult.doclib.util

import java.time.LocalDateTime

import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.{MongoCollection ⇒ JMongoCollection}
import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.models.{ConsumerVersion, DoclibDoc, DoclibFlag}
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson._
import org.mongodb.scala.result.UpdateResult
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.collection.JavaConverters._

class DoclibFlagsSpec extends FlatSpec with Matchers with MockFactory {

  implicit val config: Config = ConfigFactory.parseString(
    """
      |version {
      |  number = "/test"
      |  major = 0
      |  minor = 0
      |  patch = 1
      |  hash = "test"
      |}
    """.stripMargin)

  val wrappedCollection: JMongoCollection[DoclibDoc] = mock[JMongoCollection[DoclibDoc]]
  implicit val collection: MongoCollection[DoclibDoc] = MongoCollection[DoclibDoc](wrappedCollection)

  val codecs: CodecRegistry = MongoCodecs.get
  val now: LocalDateTime = LocalDateTime.now()

  val newDoc: DoclibDoc = DoclibDoc(
    _id = new ObjectId,
    source = "/path/to/file.txt",
    hash = "0123456789",
    mimetype =  "text/plain",
    created =  now,
    updated =  now,
  )

  val startedDoc: DoclibDoc = newDoc.copy(
    doclib = List(DoclibFlag(
      key = "test",
      version = ConsumerVersion(
        number = "0.0.1",
        major = 0,
        minor = 0,
        patch = 1,
        hash = "1234567890"),
      started = now,
    ))
  )


  "A 'started' document" should "return true when testing for the flag" in {
    assert(startedDoc.hasFlag("test"))
  }

  it should "get a valid flag" in {
    val flag = startedDoc.getFlag("test")
    assert(flag.length == 1)
    assert(flag.head.started == now)
  }

  it should "fail to get an invalid flag" in {
    val flag = startedDoc.getFlag("dummy")
    assert(flag.isEmpty)
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
        assert(u.getDocument("$set").containsKey("doclib.$.ended"))
        assert(u.getDocument("$set").get("doclib.$.ended").isNull)
        assert(u.getDocument("$set").containsKey("doclib.$.errored"))
        assert(u.getDocument("$set").get("doclib.$.errored").isNull)

        assert(u.getDocument("$set").containsKey("doclib.$.version"))
        val version = u.getDocument("$set").getDocument("doclib.$.version")
        assert(version.get("number").isString)
        assert(version.getString("number").getValue == config.getString("version.number"))
        assert(version.get("major").isInt32)
        assert(version.getInt32("major").getValue == config.getInt("version.major"))
        assert(version.get("minor").isInt32)
        assert(version.getInt32("minor").getValue == config.getInt("version.minor"))
        assert(version.get("patch").isInt32)
        assert(version.getInt32("patch").getValue == config.getInt("version.patch"))
        assert(version.containsKey("hash"))
        assert(version.get("hash").isString)
        assert(version.getString("hash").getValue == config.getString("version.hash"))
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
    assert(!newDoc.hasFlag("test"))
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
        assert(u.getDocument("$addToSet").getDocument("doclib").getDocument("version").getString("number").getValue == config.getString("version.number"))
        assert(u.getDocument("$addToSet").getDocument("doclib").getDocument("version").getString("hash").getValue == config.getString("version.hash"))
        true
      }
    ))
    val flags = new DoclibFlags("test")
    flags.start(newDoc)
  }

}
