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

import org.bson.types.ObjectId
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait ObjectIdJson {

  implicit val objectIdFormatter: Format[ObjectId] = {
    val objectIdReader: Reads[ObjectId] = (jv: JsValue) =>
      Try(new ObjectId(jv.asInstanceOf[JsString].value)) match {
        case Success(u) => JsSuccess(u)
        case Failure(e) => JsError(e.getMessage)
      }

    val objectIdWriter: Writes[ObjectId] = (oid: ObjectId) => JsString(oid.toHexString)

    Format(objectIdReader, objectIdWriter)
  }
}
