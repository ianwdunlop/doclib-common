package io.mdcatapult.doclib.util

import java.time.temporal.ChronoUnit.MILLIS
import java.time.{LocalDateTime, ZoneId, ZoneOffset}
import java.util.Date
import java.util.UUID.randomUUID

import com.typesafe.config.{Config, ConfigFactory}
import io.mdcatapult.doclib.flag.{MongoFlagStore, NotStartedException}
import io.mdcatapult.doclib.models.result.UpdatedResult
import io.mdcatapult.doclib.models._
import io.mdcatapult.doclib.util.ImplicitOrdering.localDateOrdering
import io.mdcatapult.klein.mongo.Mongo
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.{equal => Mequal}
import org.mongodb.scala.model.Updates._
import org.scalatest.BeforeAndAfter
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class DoclibFlagsIntegrationTest extends IntegrationSpec with BeforeAndAfter with ScalaFutures {

  implicit val config: Config = ConfigFactory.parseString(
    """doclib {
      |  flag = test
      |}
      |version {
      |  number = "2.0.6-SNAPSHOT",
      |  major = 2,
      |  minor =  0,
      |  patch = 6,
      |  hash =  "20837d29"
      |}
    """.stripMargin).withFallback(ConfigFactory.load())

  implicit private val mongo: Mongo = new Mongo()

  implicit private val collection: MongoCollection[DoclibDoc] =
    mongo.database.getCollection(collectionName(suffix = "doclibflags"))

  private val current: LocalDateTime = nowUtc.now()
  private val time: Now = AdvancingNow.fromCurrentTime()

  private val earlier: LocalDateTime = current.minusHours(1)
  private val later: LocalDateTime = current.plusHours(1)

  private val flags =
    new MongoFlagStore(
      ConsumerVersion.fromConfig(config),
      DoclibDocExtractor(),
      collection,
      time
    ).findFlagContext(None)

  private val newDoc: DoclibDoc = DoclibDoc(
    _id = randomUUID(),
    source = "/path/to/new.txt",
    hash = "0123456789",
    mimetype =  "text/plain",
    created =  current,
    updated =  current
  )

  private val startedDoc: DoclibDoc = newDoc.copy(
    _id = randomUUID(),
    source = "/path/to/started.txt",
    doclib = List(DoclibFlag(
      key = "test",
      version = ConsumerVersion(
        number = "0.0.1",
        major = 0,
        minor = 0,
        patch = 1,
        hash = "1234567890"),
      started = Some(current),
      summary = Some("started")
    ))
  )

  private val dupeDoc: DoclibDoc = newDoc.copy(
    _id = randomUUID(),
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
        started = Some(current)
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = null
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.1",
          major = 0,
          minor = 0,
          patch = 1,
          hash = "1234567891"),
        started = Some(later),
        state = Some(DoclibFlagState(value = "12345", updated = current))
      ),
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = Some(earlier)
      ),
      DoclibFlag(
        key = "keep",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = Some(current)
      )
    )
  )

  private val resetDoc: DoclibDoc = newDoc.copy(
    _id = randomUUID(),
    source = "/path/to/reset.txt",
    doclib = List(
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = Some(current),
        ended = Some(current),
        errored = Some(current),
        state = Some(DoclibFlagState(value = "12345", updated = current))
      )
    )
  )

  private val endOrErrorDoc: DoclibDoc = newDoc.copy(
    _id = randomUUID(),
    source = "/path/to/ending.txt",
    doclib = List(
      DoclibFlag(
        key = "test",
        version = ConsumerVersion(
          number = "0.0.2",
          major = 0,
          minor = 0,
          patch = 2,
          hash = "1234567890"),
        started = Some(current),
        reset = Some(current)
      )
    )
  )

  before  {
    Await.result(collection.deleteMany(combine()).toFuture(), Duration.Inf) // empty collection
    Await.result(collection.insertMany(
      List(newDoc, startedDoc, dupeDoc, resetDoc, endOrErrorDoc)
    ).toFuture(), Duration.Inf)
  }

  "A 'started' document" should "be restarted successfully" in {
    val result = Await.result(flags.start(startedDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", startedDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    assert(doc.doclib.head.started.get.isAfter(current))
    assert(doc.doclib.head.summary.contains("started"))
    assert(doc.doclib.head.isQueued)

  }

  "A queued document" should "have queued true" in {
    val result = Await.result(flags.queue(newDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", newDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    doc.doclib.head.isQueued should be (true)

    doc.doclib.head.started should be (None)
  }

  "A previously queued document" should "not be requeued" in {
    val result = Await.result(flags.queue(newDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", newDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    doc.doclib.head.isQueued should be (true)
    doc.doclib.head.started should be (None)

    // Doc is now queued so should not be done again ie. the request does not result in a mongo update
    val requeue = Await.result(flags.queue(doc), 5.seconds)
    requeue should be (UpdatedResult.nothing)

    val requeueDoc = Await.result(collection.find(Mequal("_id", newDoc._id)).toFuture(), 5.seconds).head
    requeueDoc.doclib should have length 1
    requeueDoc.doclib.head.isQueued should be (true)
    requeueDoc.doclib.head.started should be (None)
  }

  "A new document" can "be started " in {
    val result = Await.result(flags.start(newDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", newDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    doc.doclib.head.summary should contain ("started")
  }

  "A started document" should "be queued " in {
    val result = Await.result(flags.start(newDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", newDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    doc.doclib.head.isQueued should be (true)
  }

  it should "end cleanly" in {

    val result = Await.result(flags.end(startedDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", startedDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    doc.doclib.head.ended.isDefined should be (true)
    assert(doc.doclib.head.ended.get.isAfter(doc.doclib.head.started.get))
    doc.doclib.head.isNotQueued should be (true)
  }

  it should "start and end cleanly" in {
    val doc =
      for {
        _ <- flags.start(newDoc)
        _ <- flags.end(newDoc, noCheck = true)
        d <- collection.find(Mequal("_id", newDoc._id)).toFuture()
      } yield d

    whenReady(doc, longTimeout) { d => {
      val flags = d.head.doclib

      flags should have length 1

      val flag = flags.head
      flag.ended.value should be >= flag.started.get
      assert (flag.isNotQueued)
    }}
  }

  it should "start and end on updated doc be clean" in {
    val doc =
      for {
        _ <- flags.start(newDoc)
        xs <- collection.find(Mequal("_id", newDoc._id)).toFuture()
        createdDoc = xs.head
        _ <- flags.start(createdDoc)
        _ <- flags.end(createdDoc, noCheck = true)
        d <- collection.find(Mequal("_id", createdDoc._id)).toFuture()
      } yield d

    whenReady(doc, longTimeout) { d => {
      val flags = d.head.doclib

      assert(flags.size == 1)

      val flag = flags.head
      assert(flag.ended.isDefined)
      assert(flag.ended.get.isAfter(flag.started.get))
    }}
  }

  it should "start and end existing doc cleanly" in {
    val doc =
      for {
        _ <- flags.start(startedDoc)
        _ <- flags.end(startedDoc, noCheck = true)
        d <- collection.find(Mequal("_id", startedDoc._id)).toFuture()
      } yield d

    whenReady(doc, longTimeout) { d => {
      val flags = d.head.doclib

      assert(flags.size == 1)

      val flag = flags.head
      assert(flag.ended.isDefined)
      assert(flag.ended.get.isAfter(flag.started.get))
    }}
  }

  it should "double start from new and end existing doc cleanly" in {
    val doc =
      for {
        _ <- flags.start(newDoc)
        _ <- flags.start(newDoc)
        _ <- flags.end(newDoc, noCheck = true)
        d <- collection.find(Mequal("_id", newDoc._id)).toFuture()
      } yield d

    whenReady(doc, longTimeout) { d => {
      val flags = d.head.doclib

      flags should have length 1

      val flag = flags.head
      flag.ended.value should be >= flag.started.get
      assert(flag.isNotQueued)
    }}
  }

  it should "error cleanly" in {
    val result = Await.result(flags.error(startedDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", startedDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    doc.doclib.head.errored.isDefined should be (true)
    doc.doclib.head.summary should contain ("errored")
    assert(doc.doclib.head.errored.get.isAfter(doc.doclib.head.started.get))
    assert(doc.doclib.head.isNotQueued)
  }

  "A 'new' document" should "start successfully" in {
    val f = flags.start(newDoc)
    f map { result => {
      result.modifiedCount should be (1)
    }}
  }

  it should "fail on error" in {
    flags.error(newDoc).onComplete({
      case Success(_) => fail()
      case Failure(e) =>
        assert(e.isInstanceOf[NotStartedException])
        assert(e.getMessage == "Cannot 'error' as flag 'test' has not been started")
    })
  }

  it should "fail on end" in {
    flags.end(newDoc).onComplete({
      case Success(_) => fail()
      case Failure(e) =>
        assert(e.isInstanceOf[NotStartedException])
        assert(e.getMessage == "Cannot 'end' as flag 'test' has not been started")
    })
  }

  "A doc with duplicate flags" should "deduplicate when starting" in {
    val time = nowUtc.now().truncatedTo(MILLIS)

    val result = Await.result(flags.start(dupeDoc), 5.seconds)
      result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", dupeDoc._id)).toFuture(), 5.seconds).head
      doc.doclib should have length 2
      doc.doclib.filter(_.key == "test").head.started.get.truncatedTo(MILLIS) should be >= time
      assert(doc.doclib.exists(_.key == "keep"))
  }

  it should "deduplicate when ending" in {
    val time = nowUtc.now().truncatedTo(MILLIS)

    val result = Await.result(flags.start(dupeDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", dupeDoc._id)).toFuture(), 5.seconds).head
      doc.doclib should have length 2
      doc.doclib.filter(_.key == "test").head.started.get.truncatedTo(MILLIS) should be >= time
      assert(doc.doclib.exists(_.key == "keep"))
  }

  it should "deduplicate when erroring" in {
    val time = nowUtc.now().truncatedTo(MILLIS)
    val result = Await.result(flags.error(dupeDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", dupeDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 2

    val testFlag = doc.doclib.filter(_.key == "test").head
    testFlag.started.get.truncatedTo(MILLIS) should be (later.truncatedTo(MILLIS))
    testFlag.errored.value.truncatedTo(MILLIS) should be >= time

    assert(doc.doclib.exists(_.key == "keep"))
  }

  it should "save the doclib flag state if it exists in the flag" in {
    val result = Await.result(flags.start(dupeDoc), 5.seconds)
    result.modifiedCount should be (1)

    // Note: the assertions always seem to pass inside a subscribe so using await instead.
    val doc = Await.result(collection.find(Mequal("_id", dupeDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 2

    assert(doc.doclib.exists(_.key == "test"))
    assert(doc.doclib.exists(_.key == "keep"))

    doc.doclib.filter(_.key == "test").head.state should not be None

    assert(doc.doclib.filter(_.key == "test").head.state.get.value == "12345")
    // Note: LocalDateTime seems to get 'truncated' on write to db eg 2020-01-27T11:28:10.947614 to 2020-01-27T11:28:10.947 so comparison does not work. Convert both to date first.
    assert(Date.from(doc.doclib.filter(_.key == "test").head.state.get.updated.atZone(ZoneId.systemDefault).toInstant) == Date.from(current.atZone(ZoneId.systemDefault).toInstant))
  }

  it should "update the flag state if provided" in {
    val updateTime = nowUtc.now()

    val state = Some(DoclibFlagState(value = "23456", updated = updateTime))
    val flagUpdateResult = Await.result(flags.end(dupeDoc, state = state), 5.seconds)
    flagUpdateResult.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", dupeDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 2
    assert(doc.doclib.exists(_.key == "test"))
    assert(doc.doclib.exists(_.key == "keep"))

    doc.doclib.filter(_.key == "test").head.state should not be None
    assert(doc.doclib.filter(_.key == "test").head.state.get.value == "23456")

    doc.doclib.filter(_.key == "test").head.state.get.updated.truncatedTo(MILLIS) should be >= updateTime.truncatedTo(MILLIS)
  }

  it should "not update the flag state if None" in {
    val result = Await.result(flags.end(dupeDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", dupeDoc._id)).toFuture(), 5.seconds).head
    assert(doc.doclib.size == 2)
    assert(doc.doclib.exists(_.key == "test"))
    assert(doc.doclib.exists(_.key == "keep"))

    doc.doclib.filter(_.key == "test").head.state should not be None
    assert(doc.doclib.filter(_.key == "test").head.state.get.value == "12345")

    doc.doclib.filter(_.key == "test").head.state.get.updated.truncatedTo(MILLIS) == current.truncatedTo(MILLIS)
  }

  "A doc" can "be reset and existing flags remain as before" in {
    val result = Await.result(flags.reset(resetDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", resetDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    assert(doc.doclib.exists(_.key == "test"))

    val flag = doc.doclib.filter(_.key == "test").head
    val t = current.toEpochSecond(ZoneOffset.UTC)

    assert(flag.reset.get.toEpochSecond(ZoneOffset.UTC) >= t)
    assert(flag.started != null)
    assert(flag.started.get.toEpochSecond(ZoneOffset.UTC) == t)
    assert(flag.ended != null)
    assert(flag.ended.get.toEpochSecond(ZoneOffset.UTC) == t)
    assert(flag.errored != null)
    assert(flag.errored.get.toEpochSecond(ZoneOffset.UTC) == t)
    assert(flag.isQueued)
    flag.state should be (None)
  }

  "Ending a flag" should "clear the reset timestamp" in {
    val result = Await.result(flags.end(endOrErrorDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", endOrErrorDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    assert(doc.doclib.exists(_.key == "test"))

    doc.doclib.filter(_.key == "test").head.reset should be (None)
    doc.doclib.filter(_.key == "test").head.ended should not be None

    assert(doc.doclib.filter(_.key == "test").head.ended.get.toEpochSecond(ZoneOffset.UTC) >= current.toEpochSecond(ZoneOffset.UTC))

    doc.doclib.filter(_.key == "test").head.errored should be (None)
    doc.doclib.filter(_.key == "test").head.started should not be None
    doc.doclib.filter(_.key == "test").head.summary should contain ("ended")

    assert(doc.doclib.filter(_.key == "test").head.started.get.toEpochSecond(ZoneOffset.UTC) == current.toEpochSecond(ZoneOffset.UTC))
  }

  "Erroring a flag" should "clear the reset timestamp" in {
    val result = Await.result(flags.error(endOrErrorDoc), 5.seconds)
    result.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", endOrErrorDoc._id)).toFuture(), 5.seconds).head
    doc.doclib should have length 1
    assert(doc.doclib.exists(_.key == "test"))

    doc.doclib.filter(_.key == "test").head.reset should be (None)
    doc.doclib.filter(_.key == "test").head.errored should not be None

    assert(doc.doclib.filter(_.key == "test").head.errored.get.toEpochSecond(ZoneOffset.UTC) >= current.toEpochSecond(ZoneOffset.UTC))

    doc.doclib.filter(_.key == "test").head.ended should be (None)
    doc.doclib.filter(_.key == "test").head.started should not be None
    doc.doclib.filter(_.key == "test").head.summary should contain ("errored")

    assert(doc.doclib.filter(_.key == "test").head.started.get.toEpochSecond(ZoneOffset.UTC) == current.toEpochSecond(ZoneOffset.UTC))
  }

  "The reset flag" should "be reset when ending and state is provided" in {
    val updateTime = nowUtc.now()
    val state = Some(DoclibFlagState(value = "23456", updated = updateTime))

    val flagUpdateResult = Await.result(flags.end(endOrErrorDoc, state = state), 5.seconds)
    flagUpdateResult.modifiedCount should be (1)

    val doc = Await.result(collection.find(Mequal("_id", endOrErrorDoc._id)).toFuture(), 5.seconds).head
    assert(doc.doclib.size == 1)
    assert(doc.doclib.exists(_.key == "test"))

    doc.doclib.filter(_.key == "test").head.state should not be None
    assert(doc.doclib.filter(_.key == "test").head.state.get.value == "23456")
    assert(doc.doclib.filter(_.key == "test").head.state.get.updated.toEpochSecond(ZoneOffset.UTC) >= updateTime.toEpochSecond(ZoneOffset.UTC))

    doc.doclib.filter(_.key == "test").head.ended should not be None
    assert(doc.doclib.filter(_.key == "test").head.ended.get.toEpochSecond(ZoneOffset.UTC) >= current.toEpochSecond(ZoneOffset.UTC))

    doc.doclib.filter(_.key == "test").head.reset should be (None)
  }

}
