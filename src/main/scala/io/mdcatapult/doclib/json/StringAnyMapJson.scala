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

package io.mdcatapult.doclib.json

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait StringAnyMapJson {

  implicit val stringAnyMapFormatter: Format[Map[String, Any]] = {

    val stringAnyMapReader: Reads[Map[String, Any]] = (jv: JsValue) =>
      JsSuccess(jv match {
        case JsObject(fields) => fields.toMap[String, JsValue].map { case (k, v) => k -> (v match {
          case JsBoolean(b) => b
          case JsString(s) => s
          case JsNumber(n) => Try(n.toBigIntExact) match {
            case Success(value) => value.get.toLong
            case Failure(_) => n.toDouble
          }
          case _ => throw new IllegalArgumentException("Unable to convert value")
        }) }
        case _ => jv.as[Map[String, Any]].map { case (k, v) => k -> v }
      })

    val stringAnyMapWriter: Writes[Map[String, Any]] = (map: Map[String, Any]) =>
      Json.obj(map.map { case (s, o) =>
        val v: JsValueWrapper = o match {
          case b: Boolean => JsBoolean(b)
          case aString: String => JsString(aString)
          case n: Int => JsNumber(n)
          case n: Double => JsNumber(n)
          case n: Float => JsNumber(n.toDouble)
          case other => throw new MatchError(other)
        }
        val ret: (String, JsValueWrapper) = s -> v
        ret
      }.toSeq: _*)

    Format(stringAnyMapReader, stringAnyMapWriter)
  }

}
