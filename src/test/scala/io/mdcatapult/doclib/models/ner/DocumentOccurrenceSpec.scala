package io.mdcatapult.doclib.models.ner

import java.util.UUID

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class DocumentOccurrenceSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" can "be encoded and decoded successfully to BSON" in {
    val uuid = UUID.fromString("dc83cac6-4daa-4a0b-8e52-df1543af1e8f")
    val docUUID = UUID.fromString("600029ba-ccea-4e46-9ea5-7f54996954dd")
    roundTrip(Occurrence(
      _id = uuid,
      nerDocument = docUUID,
      characterStart = 1,
      characterEnd = 2
    ),
      """{
        |"_id": {"$binary": "3IPKxk2qSguOUt8VQ68ejw==", "$type": "04"},
        |"nerDocument": {"$binary": "YAApuszqTkaepX9UmWlU3Q==", "$type": "04"},
        |"characterStart": 1,
        |"characterEnd": 2,
        |"fragment": null,
        |"correctedValue": null,
        |"correctedValueHash": null,
        |"wordIndex": null,
        |"type": "document"
        |}""".stripMargin, classOf[Occurrence])
  }

  it can "give old known hash for same document occurrence" in {
    val uuid = UUID.fromString("dc83cac6-4daa-4a0b-8e52-df1543af1e8f")
    val docUUID = UUID.fromString("600029ba-ccea-4e46-9ea5-7f54996954dd")
    val doc = Occurrence(
      _id = uuid,
      nerDocument = docUUID,
      characterStart = 12,
      characterEnd = 15,
      fragment = Option(UUID.fromString("600029ba-ccea-4e46-9ea5-7f54996954dd")),
      correctedValue = Option("fixed!"),
      correctedValueHash = Option("5e185e300268642a0fcbc964")
    )

    assert(Occurrence.md5(Seq(doc)) == "19c6dc6cf38d85c801eb9fd09e03a99c")
  }

  it can "give old known hash for same document occurrence with optionals are None" in {
    val uuid = UUID.fromString("dc83cac6-4daa-4a0b-8e52-df1543af1e8f")
    val docUUID = UUID.fromString("600029ba-ccea-4e46-9ea5-7f54996954dd")
    val doc = Occurrence(
      _id = uuid,
      nerDocument = docUUID,
      characterStart = 12,
      characterEnd = 15,
      fragment = None,
      correctedValue = None,
      correctedValueHash = None
    )

    assert(Occurrence.md5(Seq(doc)) == "e1b591c4ced77328af6321f52fd540da")
  }

  "Occurrences with different _ids for the same value and ner doc" should "have the same md5" in {
    val nerID = UUID.randomUUID()
    val occurrences = List[Occurrence](
      Occurrence(
        _id = UUID.randomUUID,
        nerDocument = nerID,
        characterStart = 115416768,
        characterEnd = 115416777
      ),
      Occurrence(
        _id = UUID.randomUUID,
        nerDocument = nerID,
        characterStart = 115424728,
        characterEnd = 115424737
      ),
      Occurrence(
        _id = UUID.randomUUID,
        nerDocument = nerID,
        characterStart = 115379372,
        characterEnd = 115379381
      )
    )
    val occurrencesNew = List[Occurrence](
      Occurrence(
        _id = UUID.randomUUID,
        nerDocument = nerID,
        characterStart = 115416768,
        characterEnd = 115416777
      ),
      Occurrence(
        _id = UUID.randomUUID,
        nerDocument = nerID,
        characterStart = 115424728,
        characterEnd = 115424737
      ),
      Occurrence(
        _id = UUID.randomUUID,
        nerDocument = nerID,
        characterStart = 115379372,
        characterEnd = 115379381
      )
    )
    assert(Occurrence.md5(occurrences) == Occurrence.md5(occurrencesNew))
  }


}
