package io.mdcatapult.doclib.json

import io.mdcatapult.doclib.models.metadata._
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait MetaValueJson {
  implicit val metaValueReader: Reads[MetaValueUntyped] = (jv: JsValue) =>
    JsSuccess(jv match {
      case JsObject(fields: collection.Map[String, JsValue]) ⇒ fields("value") match {
        case JsBoolean(b) ⇒ MetaBoolean(fields("key").toString(), b)
        case JsString(s) ⇒ MetaString(fields("key").toString(), s)
        case JsNumber(n) ⇒ Try(n.toBigIntExact()) match {
          case Success(value) ⇒ MetaDouble(fields("key").toString(), value.get.toDouble)
          case Failure(_) ⇒ MetaDouble(fields("key").toString(), n.toDouble)
        }
        case _ ⇒ throw new IllegalArgumentException("Unable to convert value")
      }
      case _ ⇒ throw new IllegalArgumentException("Unable parse json")
    })

  implicit val metaValueWriter: Writes[MetaValueUntyped] = (value: MetaValueUntyped) => {
    val typed: MetaValue[_] = value match {
      case v: MetaString ⇒ v.asInstanceOf[MetaString]
      case v: MetaDateTime ⇒ v.asInstanceOf[MetaDateTime]
      case v: MetaDouble ⇒ v.asInstanceOf[MetaDouble]
      case v: MetaInt ⇒ v.asInstanceOf[MetaInt]
      case v: MetaString ⇒ v.asInstanceOf[MetaString]
    }

    Json.obj(
      "key" -> JsString(typed.getKey),
      "value" → {
        typed.getValue match {
          case b: Boolean ⇒ JsBoolean(b)
          case s: String ⇒ JsString(s)
          case n: Int ⇒ JsNumber(n)
          case n: Double ⇒ JsNumber(n)
        }
      })
  }

  implicit val metaValueFormatter: Format[MetaValueUntyped] = Format(metaValueReader, metaValueWriter)
}
