package io.mdcatapult.doclib.models

import java.time.LocalDateTime

import io.mdcatapult.doclib.json.MetaValueJson
import io.mdcatapult.doclib.models.metadata.MetaValue
import org.mongodb.scala.bson.ObjectId
import play.api.libs.json.{Format, Json, Reads, Writes}

//object DoclibDoc extends MetaValueJson {
//  implicit val msgReader: Reads[DoclibDoc] = Json.reads[DoclibDoc]
//  implicit val msgWriter: Writes[DoclibDoc] = Json.writes[DoclibDoc]
//  implicit val msgFormatter: Format[DoclibDoc] = Json.format[DoclibDoc]
//}

case class DoclibDoc(
                      _id: ObjectId,
                      source: String,
                      hash: String,
                      derivative: Boolean = false,
                      created: LocalDateTime,
                      updated: LocalDateTime,
                      mimetype: String,
                      attrs: FileAttrs,
                      doclib: List[DoclibFlag] = List(),
                      tags: Option[List[String]] = None,
                      derivatives: Option[List[Derivative]] = None,
                      origin: Option[List[Origin]] = None,
                      metadata: Option[List[MetaValue[_]]] = None
                    ) {

  def hasFlag(key: String): Boolean = doclib.exists(_.key == key)
  def getFlag(key: String): List[DoclibFlag] = doclib.filter(_.key == key)
  def getDerivatives(`type`: Option[String] = None): List[Derivative] =
    derivatives.getOrElse(List()).filter(d ⇒ (`type`.isDefined && d.`type` == `type`.get) || `type`.isEmpty)
}
