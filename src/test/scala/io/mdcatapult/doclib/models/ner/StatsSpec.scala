package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.models.metadata.MetaValue
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.scalatest.{FlatSpec, Matchers}


class StatsSpec extends FlatSpec with Matchers with BsonCodecCompatible {

  val registry: CodecRegistry = MongoCodecs.get

  "Model" can "be encoded and decoded successfully to BSON" in {
    roundTrip(Stats(
      bySchema = List(),
      byDictionary =  List(),
      byResolvedEntity = List(),
      byEntityType = List(),
    ),
      """{
        |"bySchema": [],
        |"byDictionary": [],
        |"byResolvedEntity": [],
        |"byEntityType": []}""".stripMargin, classOf[Stats])
  }
}
