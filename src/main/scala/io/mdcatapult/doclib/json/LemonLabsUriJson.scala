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

import io.lemonlabs.uri.Uri
import play.api.libs.json._

import scala.util.{Failure, Success}

trait LemonLabsUriJson {

  implicit val lemonLabsUriFormatter: Format[Uri] = {

    val lemonLabsUriReader: Reads[Uri] = (jv: JsValue) =>
      Uri.parseTry(jv.asInstanceOf[JsString].value) match {
        case Success(u) => JsSuccess(u)
        case Failure(e) => JsError(e.getMessage)
      }

    val lemonLabsUriWriter: Writes[Uri] = (uri: Uri) => {
      val u = uri.toString()
      JsString(u)
    }

    Format(lemonLabsUriReader, lemonLabsUriWriter)
  }
}
