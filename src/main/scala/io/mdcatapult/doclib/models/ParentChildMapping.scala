package io.mdcatapult.doclib.models

import java.util.UUID

import io.mdcatapult.doclib.models.metadata.MetaValueUntyped
import org.mongodb.scala.bson.ObjectId

case class ParentChildMapping(_id: UUID, parent: ObjectId, child: Option[ObjectId] = None, childPath: String, metadata: Option[List[MetaValueUntyped]] = None, consumer: Option[String] = None)
