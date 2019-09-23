package io.mdcatapult.doclib.messages

import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import play.api.libs.json._

class PrefetchMsgSpec  extends FlatSpec with Matchers with MockFactory {

  "A prefetch message created from JSON" should "return metadata" in {
    val prefetchMsg: PrefetchMsg = Json.parse("""{ "source" : "/home/ian.dunlop@medcat.local/scratch/doclib/biorxiv/files/327015.full.pdf", "metadata" : { "doi" : "10.1101/327015" }, "tags" : [ "bioarxiv" ] }""").as[PrefetchMsg]
    val metadata: Map[String, Any] = prefetchMsg.metadata.get
    assert(metadata.get("doi").get == "10.1101/327015")
  }

  "A prefetch message" should "convert back to JSON" in {
    val prefetchMsgJson: String = """{ "source" : "/home/ian.dunlop@medcat.local/scratch/doclib/biorxiv/files/327015.full.pdf", "metadata" : { "doi" : "10.1101/327015" }, "tags" : [ "bioarxiv" ] }"""
    val prefetchMsg: PrefetchMsg = Json.parse(prefetchMsgJson).as[PrefetchMsg]
    val json: JsValue = Json.toJson(prefetchMsg)
    val prefetchMsg2: PrefetchMsg = Json.parse(json.toString()).as[PrefetchMsg]
    // The json from the original message should create a prefetch message with the same fields
    assert(prefetchMsg.equals(prefetchMsg2))
  }

}
