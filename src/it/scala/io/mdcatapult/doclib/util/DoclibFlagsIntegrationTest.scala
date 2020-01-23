package io.mdcatapult.doclib.util

import java.time.LocalDateTime

import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.models.{ConsumerVersion, DoclibDoc, DoclibFlag}
import io.mdcatapult.klein.mongo.Mongo
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.{equal => Mequal}
import org.mongodb.scala.model.Updates._
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class DoclibFlagsIntegrationTest extends FlatSpec with Matchers with BeforeAndAfter {

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
    mongo.database.getCollection(s"${config.getString("mongo.collection")}_doclibflags")

  val current: LocalDateTime = LocalDateTime.now()
  val earlier: LocalDateTime = current.minusHours(1)
  val later: LocalDateTime = current.plusHours(1)

  val flags = new DoclibFlags("test")

  val newDoc: DoclibDoc = DoclibDoc(
    _id = new ObjectId,
    source = "/path/to/new.txt",
    hash = "0123456789",
    mimetype =  "text/plain",
    created =  current,
    updated =  current,
  )

  val startedDoc: DoclibDoc = newDoc.copy(
    _id = new ObjectId,
    source = "/path/to/started.txt",
    doclib = List(DoclibFlag(
      key = "test",
      version = ConsumerVersion(
        number = "0.0.1",
        major = 0,
        minor = 0,
        patch = 1,
        hash = "1234567890"),
      started = current,
    ))
  )

  val dupeDoc: DoclibDoc = newDoc.copy(
    _id = new ObjectId,
    source = "/path/to/dupe.txt",
    doclib = List(
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = current,
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = null,
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.1",
          major = 0,
          minor = 0,
          patch = 1,
          hash = "1234567891"),
        started = later,
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = earlier,
      ),
      DoclibFlag(
        key = "keep",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = current,
      ),
    )
  )

  before  {
    Await.result(collection.deleteMany(combine()).toFuture(), Duration.Inf) // empty collection
    Await.result(collection.insertMany(List(newDoc, startedDoc, dupeDoc)).toFuture(), Duration.Inf)
  }

  "A 'started' document" should "be restarted successfully" in {
    val f = flags.start(startedDoc)
    f map { result =>
      assert(result.isDefined)
      assert(result.get.getModifiedCount == 1)
    }
    collection.find(Mequal("_id", dupeDoc._id)).subscribe((doc: DoclibDoc) => {
      assert(doc.doclib.size == 1)
      assert(doc.doclib.head.started.isAfter(current))
    })
  }

  it should "end cleanly" in {
    val f = flags.end(startedDoc)
    f map { result =>
      assert(result.isDefined)
      assert(result.get.getModifiedCount == 1)
    }
    collection.find(Mequal("_id", dupeDoc._id)).subscribe((doc: DoclibDoc) => {
      assert(doc.doclib.size == 1)
      assert(doc.doclib.head.ended.isDefined)
      assert(doc.doclib.head.ended.get.isAfter(doc.doclib.head.started))
    })
  }

  it should "error cleanly" in {
    val f = flags.error(startedDoc)
    f map { result =>
      assert(result.isDefined)
      assert(result.get.getModifiedCount == 1)
    }
    collection.find(Mequal("_id", dupeDoc._id)).subscribe((doc: DoclibDoc) => {
      assert(doc.doclib.size == 1)
      assert(doc.doclib.head.errored.isDefined)
      assert(doc.doclib.head.errored.get.isAfter(doc.doclib.head.started))
    })
  }

  "A 'new' document" should "start successfully" in {
    val f = flags.start(newDoc)
    f map { result => {
      assert(result.isDefined)
      assert(result.get.getModifiedCount == 1)
    }}
  }

  it should "fail on error" in {
    import flags.NotStarted
    flags.error(newDoc).onComplete({
      case Success(_) => fail()
      case Failure(e) =>
        assert(e.isInstanceOf[NotStarted])
        assert(e.getMessage == "Cannot 'error' as flag 'test' has not been started")
    })
  }

  it should "fail on end" in {
    import flags.NotStarted
    flags.end(newDoc).onComplete({
      case Success(_) => fail()
      case Failure(e) =>
        assert(e.isInstanceOf[NotStarted])
        assert(e.getMessage == "Cannot 'error' as flag 'test' has not been started")
    })
  }

  it should "not be restartable" in {
    import flags.NotStarted
    flags.restart(newDoc).onComplete({
      case Success(_) => fail()
      case Failure(e) =>
        assert(e.isInstanceOf[NotStarted])
        assert(e.getMessage == "Cannot 'restart' as flag 'test' has not been started")
    })
  }

  "A doc with duplicate flags" should "deduplicate when starting" in {
    val f = flags.start(dupeDoc)
    f map { result => {
      assert(result.isDefined)
      assert(result.get.getModifiedCount == 1)
    }}
    collection.find(Mequal("_id", dupeDoc._id)).subscribe((doc: DoclibDoc) => {
      assert(doc.doclib.size == 2)
      assert(doc.doclib.filter(_.key == "test").head.started == later)
      assert(doc.doclib.exists(_.key == "keep"))
      assert(doc.doclib.exists(_.key == "test"))
    })
  }

  it should "deduplicate when ending" in {
    val f = flags.end(dupeDoc)
    f map { result => {
      assert(result.isDefined)
      assert(result.get.getModifiedCount == 1)
    }}
    collection.find(Mequal("_id", dupeDoc._id)).subscribe((doc: DoclibDoc) => {
      assert(doc.doclib.size == 2)
      assert(doc.doclib.filter(_.key == "test").head.started == later)
      assert(doc.doclib.exists(_.key == "keep"))
      assert(doc.doclib.exists(_.key == "test"))
    })
  }

  it should "deduplicate when erroring" in {
    val f = flags.error(dupeDoc)
    f map { result => {
      assert(result.isDefined)
      assert(result.get.getModifiedCount == 1)
    }}
    collection.find(Mequal("_id", dupeDoc._id)).subscribe((doc: DoclibDoc) => {
      assert(doc.doclib.size == 2)
      assert(doc.doclib.filter(_.key == "test").head.started == later)
      assert(doc.doclib.exists(_.key == "keep"))
      assert(doc.doclib.exists(_.key == "test"))
    })
  }


}
