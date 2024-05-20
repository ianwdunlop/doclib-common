package io.mdcatapult.doclib.json

import java.time.LocalDateTime

import io.mdcatapult.doclib.models.metadata._
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait MetaValueJson {

  implicit val metaValueFormatter: Format[MetaValueUntyped] = {

    val isDate = """([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2}).([0-9])*""".r

    val metaValueReader: Reads[MetaValueUntyped] = (jv: JsValue) =>
      JsSuccess(jv match {
        case JsObject(fields: collection.Map[String, JsValue]) => fields("value") match {
          case JsBoolean(b) => MetaBoolean(fields("key").asInstanceOf[JsString].value, b)
          case JsString(s) => s match {
            case isDate(year, month, day, hour, min, sec, nano) =>
              MetaDateTime(
                fields("key").asInstanceOf[JsString].value,
                LocalDateTime.of(year.toInt, month.toInt, day.toInt, hour.toInt, min.toInt, sec.toInt, nano.toInt))
            case _ => MetaString(fields("key").asInstanceOf[JsString].value, s)
          }

          case JsNumber(n) => Try(n.toBigIntExact) match {
            case Success(value) => value match {
              case Some(int) => MetaInt(fields("key").asInstanceOf[JsString].value, int.toInt)
              case None => MetaDouble(fields("key").asInstanceOf[JsString].value, n.toDouble)
            }
            case Failure(_) =>
              MetaDouble(fields("key").asInstanceOf[JsString].value, n.toDouble)
          }
          case _ => throw new IllegalArgumentException("Unable to convert value")
        }
        case _ => throw new IllegalArgumentException("Unable parse json")
      })

    val metaValueWriter: Writes[MetaValueUntyped] = (value: MetaValueUntyped) => {
      val typed: MetaValue[_] = value match {
        case v: MetaBoolean => v.asInstanceOf[MetaBoolean]
        case v: MetaInt => v.asInstanceOf[MetaInt]
        case v: MetaDouble => v.asInstanceOf[MetaDouble]
        case v: MetaDateTime => v.asInstanceOf[MetaDateTime]
        case v: MetaString => v.asInstanceOf[MetaString]
        case other => throw new MatchError(other)
      }

      Json.obj(
        "key" -> JsString(typed.getKey),
        "value" -> {
          typed.getValue match {
            case v: LocalDateTime => JsString(v.toString)
            case v: Boolean => JsBoolean(v)
            case v: String => JsString(v)
            case v: Int => JsNumber(v)
            case v: Double => JsNumber(v)
            case other => throw new MatchError(other)
          }
        })
    }
    Format(metaValueReader, metaValueWriter)
  }
}
