package io.mdcatapult.doclib.json

import org.bson.BsonDocument
import org.mongodb.scala.bson.{BsonBoolean, BsonInt32, BsonInt64, BsonNumber, BsonString, Document}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

trait BsonDocumentJson {

  implicit val bsonDocumentFormatter: Format[Document] = {

    val bsonDocumentReader: Reads[Document] = (jv: JsValue) => JsSuccess(BsonDocument.parse(jv.toString))

    val bsonDocumentWriter: Writes[Document] = (doc: Document) =>
      Json.obj(doc.toMap.map { case (s, o) =>
        val v: JsValueWrapper = o match {
          case b: BsonBoolean => JsBoolean(b.getValue)
          case s: BsonString => JsString(s.getValue)
          case n: BsonInt32 => JsNumber(n.getValue)
          case n: BsonInt64 =>  JsNumber(n.getValue)
          case n: BsonNumber => JsNumber(n.doubleValue)
        }
        val ret: (String, JsValueWrapper) = s -> v
        ret
      }.toSeq: _*)

    Format(bsonDocumentReader, bsonDocumentWriter)
  }

  /**
   * Convert a Map of JSON key/value pair eg "doi": "10.1101/327015" to a Map of String -> Any tuples
   */
  val mapReads: Reads[Map[String, Any]] = {

    def convert(m: JsValue): Any =
      m match {
        case JsString(s) => s
        case JsNumber(n) => n
        case JsBoolean(b) => b
      }

    def metaValueToJsValue(m: JsValue): JsResult[Any] =
      m match {
        case JsObject(m) =>
          val m1 = m.map(f => (f._1, convert(f._2))).toMap
          JsSuccess(m1)
        case JsString(s) => JsSuccess(s)
        case JsNumber(n) => JsSuccess(n)
        case JsBoolean(b) => JsSuccess(b)
        case JsArray(arr) =>
          val list = arr.map(convert)
          JsSuccess(list)
      }

    val anyReads: Reads[Any] = Reads[Any](m => metaValueToJsValue(m))

    Reads[Map[String, Any]](m => Reads.mapReads[Any](anyReads).reads(m))
  }

  /**
   * Convert a Map of String -> any back to Json "a": "b" key/value pairs
   */
  val mapWrites: Writes[Map[String, Any]] = (map: Map[String, Any]) => Json.obj(map.map { case (s, o) =>
    val v: JsValueWrapper = o match {
      case b: BsonBoolean => JsBoolean(b.getValue)
      case s: String => JsString(s)
      case s: BsonString => JsString(s.getValue)
      case n: BsonInt32 => JsNumber(n.getValue)
      case n: BsonInt64 => JsNumber(n.getValue)
      case n: BsonNumber => JsNumber(n.doubleValue)
    }
    val ret: (String, JsValueWrapper) = s -> v
    ret
  }.toSeq: _*)

  implicit val mapFormat: Format[Map[String, Any]] = Format(mapReads, mapWrites)

}
