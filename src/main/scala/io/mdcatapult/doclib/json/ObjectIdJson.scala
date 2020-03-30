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
