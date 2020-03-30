package io.mdcatapult.doclib.models.metadata

import java.time.LocalDateTime

import io.mdcatapult.doclib.json.MetaValueJson
import io.mdcatapult.doclib.models.BsonCodecCompatible
import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

class MetaValueUntypedSpec extends FlatSpec with Matchers with MetaValueJson with BsonCodecCompatible{

  val registry: CodecRegistry = MongoCodecs.get

  val dummyList: List[MetaValueUntyped] = List(
    MetaString("stringKey", "stringValue"),
    MetaInt("intKey", 1),
    MetaDouble("doubleKey", 1.1),
    MetaBoolean("boolKey", value=true),
    MetaDateTime("dateTimeKey", LocalDateTime.parse("2019-10-04T13:46:37.612508"))
  )

  "The list" should "be able to be encoded as JSON" in {
    val listJson = Json.toJson(dummyList)
    assert(listJson.toString() == """[{"key":"stringKey","value":"stringValue"},{"key":"intKey","value":1},{"key":"doubleKey","value":1.1},{"key":"boolKey","value":true},{"key":"dateTimeKey","value":"2019-10-04T13:46:37.612508"}]""")
  }
  it should "be convertable from JSON into a list" in {
    val listJson = Json.parse("""[{"key":"stringKey","value":"stringValue"},{"key":"intKey","value":1},{"key":"doubleKey","value":1.1},{"key":"boolKey","value":true},{"key":"dateTimeKey","value":"2019-10-04T13:46:37.612508"}]""").as[List[MetaValueUntyped]]
    assert(listJson.length == 5)
  }
}
