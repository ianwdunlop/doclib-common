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
