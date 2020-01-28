package io.mdcatapult.doclib.json

import org.bson.types.ObjectId
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait ObjectIdJson {

  implicit val objectIdReader: Reads[ObjectId] = (jv: JsValue) =>
    Try(new ObjectId(jv.asInstanceOf[JsString].value)) match {
      case Success(u) ⇒ JsSuccess(u)
      case Failure(e) ⇒ JsError(e.getMessage)
    }

  implicit val objectIdWriter: Writes[ObjectId] = (oid: ObjectId) => JsString(oid.toHexString)
  implicit val objectIdFormatter: Format[ObjectId] = Format(objectIdReader, objectIdWriter)
}
