package io.mdcatapult.doclib.messages

import io.mdcatapult.doclib.models.metadata.{MetaString, MetaValueUntyped}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class PrefetchMsgSpec  extends AnyFlatSpec with Matchers with MockFactory {

  "A message" should "convert to JSON sucessfully" in {
    val msgJson = Json.toJson(PrefetchMsg("doclib/biorxiv/files/327015.full.pdf", metadata = Some(List(MetaString("doi", "10.1101/327015"))), tags = Some(List("bioarxiv"))))
    assert(msgJson.toString == """{"source":"doclib/biorxiv/files/327015.full.pdf","tags":["bioarxiv"],"metadata":[{"key":"doi","value":"10.1101/327015"}]}""")
  }

  "Valid JSON" should "convert to a message" in {
    val msg: PrefetchMsg = Json.parse("""{ "source" : "doclib/biorxiv/files/327015.full.pdf"}""").as[PrefetchMsg]
    assert(msg.source == "doclib/biorxiv/files/327015.full.pdf")
  }

  "Valid JSON with metadata" should "return metadata" in {
    val prefetchMsg: PrefetchMsg = Json.parse("""{ "source" : "doclib/biorxiv/files/327015.full.pdf", "metadata" : [{ "key": "doi", "value": "10.1101/327015" }], "tags" : [ "bioarxiv" ] }""").as[PrefetchMsg]
    val metadata: List[MetaValueUntyped] = prefetchMsg.metadata.get

    assert(metadata.length == 1)
    assert(metadata.head.asInstanceOf[MetaString].key == "doi")
    assert(metadata.head.asInstanceOf[MetaString].value == "10.1101/327015")
  }

  "Valid JSON" should "decode code to the message and back to json sucessfully" in {
    val prefetchMsgJson: String = """{ "source" : "doclib/biorxiv/files/327015.full.pdf", "metadata" : [{ "key": "doi", "value": "10.1101/327015" }], "tags" : [ "bioarxiv" ] }"""
    val prefetchMsg: PrefetchMsg = Json.parse(prefetchMsgJson).as[PrefetchMsg]
    val json: JsValue = Json.toJson(prefetchMsg)
    val prefetchMsg2: PrefetchMsg = Json.parse(json.toString()).as[PrefetchMsg]
    // The json from the original message should create a prefetch message with the same fields
    assert(prefetchMsg.equals(prefetchMsg2))
  }

}
