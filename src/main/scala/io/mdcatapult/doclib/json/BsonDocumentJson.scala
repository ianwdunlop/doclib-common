package io.mdcatapult.doclib.json

import org.bson.BsonDocument
import org.mongodb.scala.bson.{BsonBoolean, BsonInt32, BsonInt64, BsonNumber, BsonString, Document}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

trait BsonDocumentJson {

  implicit val bsonDocumentReader: Reads[Document] = (jv: JsValue) => JsSuccess(BsonDocument.parse(jv.toString()))
  implicit val bsonDocumentWriter: Writes[Document] = (doc: Document) =>
    Json.obj(doc.toMap.map { case (s, o) =>
      val v: JsValueWrapper = o match {
        case b: BsonBoolean ⇒ JsBoolean(b.getValue)
        case s: BsonString ⇒ JsString(s.getValue)
        case n: BsonInt32 ⇒ JsNumber(n.getValue)
        case n: BsonInt64 ⇒  JsNumber(n.getValue)
        case n: BsonNumber ⇒ JsNumber(n.doubleValue())
      }
      val ret: (String, JsValueWrapper) = s -> v
      ret
    }.toSeq: _*)
  implicit val bsonDocumentFormatter: Format[Document] = Format(bsonDocumentReader, bsonDocumentWriter)

}
