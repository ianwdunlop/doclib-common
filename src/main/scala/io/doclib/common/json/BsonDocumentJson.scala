/*
 * Copyright 2024 Medicines Discovery Catapult
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.doclib.common.json

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
          case bs: BsonString => JsString(bs.getValue)
          case n: BsonInt32 => JsNumber(n.getValue)
          case n: BsonInt64 =>  JsNumber(n.getValue)
          case n: BsonNumber => JsNumber(n.doubleValue)
          case other => throw new MatchError(other)
          // While we don't particularly care about the other types it is possible so maybe not the most robust bit of code ever
          // Could throw run time exceptions that we try to avoid if possible
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
        case other => throw new MatchError(other)
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
        case other => throw new MatchError(other)
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
      case aString: String => JsString(aString)
      case bsonString: BsonString => JsString(bsonString.getValue)
      case n: BsonInt32 => JsNumber(n.getValue)
      case n: BsonInt64 => JsNumber(n.getValue)
      case n: BsonNumber => JsNumber(n.doubleValue)
      case other => throw new MatchError(other)
    }
    val ret: (String, JsValueWrapper) = s -> v
    ret
  }.toSeq: _*)

  implicit val mapFormat: Format[Map[String, Any]] = Format(mapReads, mapWrites)

}
