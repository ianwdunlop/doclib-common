package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class FragmentOccurrenceSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(FragmentOccurrence(
      entityType = "entityType",
      schema = "schema",
      characterStart = 1,
      characterEnd = 2,
      wordIndex = 3,
    ),
      """{
        |"entityType": "entityType",
        |"entityGroup": null,
        |"schema": "schema",
        |"characterStart": 1,
        |"characterEnd": 2,
        |"wordIndex": 3,
        |"fragment": null,
        |"correctedValue": null,
        |"correctedValueHash": null,
        |"resolvedEntity": null,
        |"resolvedEntityHash": null,
        |"type": "fragment"}""".stripMargin, classOf[FragmentOccurrence])
  }

  it can "give old known hash for same document occurrence" in {
    val doc = FragmentOccurrence(
      entityType = "test-entity-type",
      entityGroup = Option("test-entity-group"),
      schema = "example-schema",
      characterStart = 12,
      characterEnd = 15,
      wordIndex = 10,
      fragment = Option(new ObjectId("5e18616e8ebbb71a02f2faea")),
      correctedValue = Option("fixed!"),
      correctedValueHash = Option("5e185e300268642a0fcbc964"),
      resolvedEntity = Option("resolved entity"),
      resolvedEntityHash = Option("5e1860510268642a0fcbc965")
    )

    assert(Occurrence.md5(Seq(doc)) == "c65debc68f3b1fae18185e3e4c712725")
  }

  it can "give old known hash for same document occurrence with optionals are None" in {
    val doc = FragmentOccurrence(
      entityType = "test-entity-type",
      entityGroup = None,
      schema = "example-schema",
      characterStart = 12,
      characterEnd = 15,
      wordIndex = 10,
      fragment = None,
      correctedValue = None,
      correctedValueHash = None,
      resolvedEntity = None,
      resolvedEntityHash = None
    )

    assert(Occurrence.md5(Seq(doc)) == "b2f751116bdf37d370f533539987f1f6")
  }
}
