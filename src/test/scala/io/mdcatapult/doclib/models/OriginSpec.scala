package io.mdcatapult.doclib.models

import io.lemonlabs.uri.Uri
import io.mdcatapult.doclib.models.metadata.{MetaInt, MetaString}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OriginSpec extends AnyFlatSpec with Matchers with BsonCodecCompatible {

  "Model" should "be able to be encoded and decoded successfully to BSON" in {
    roundTrip(Origin(
      scheme = "https",
      hostname = None,
      uri = Some(Uri.parse("https://new.bbc.co.uk")),
      metadata = Some(List(MetaString("key", "value"), MetaInt("a-value", 1))),
      headers = None
    ),
      """{
        |"scheme": "https",
        |"hostname": null,
        |"uri": "https://new.bbc.co.uk",
        |"headers": null,
        |"metadata": [{
        |   "key": "key",
        |   "value": "value"
        |},
        |{
        |   "key" : "a-value",
        |   "value": 1
        |}
        |]}""".stripMargin)

  }

}
