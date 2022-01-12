package io.mdcatapult.doclib.models

import java.time.LocalDateTime
import java.util.UUID
import io.mdcatapult.doclib.models.metadata.MetaValueUntyped
import org.mongodb.scala.bson.ObjectId

case class DoclibDoc(
                      _id: ObjectId,
                      source: String,
                      hash: String,
                      mimetype: String,
                      created: LocalDateTime,
                      updated: LocalDateTime,
                      derivative: Boolean = false,
                      attrs: Option[FileAttrs] = None,
                      doclib: List[DoclibFlag] = List(),
                      tags: Option[List[String]] = None,
                      derivatives: Option[List[Derivative]] = None,
                      origin: Option[List[Origin]] = None,
                      metadata: Option[List[MetaValueUntyped]] = None,
                      uuid: Option[UUID] = None,
                      rogueFile: Option[Boolean] = None,
                    )  {

  def hasFlag(key: String): Boolean = doclib.exists(_.key == key)
  def getFlag(key: String): List[DoclibFlag] = doclib.filter(_.key == key)
}
