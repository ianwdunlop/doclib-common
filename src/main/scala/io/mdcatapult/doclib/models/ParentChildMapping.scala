package io.mdcatapult.doclib.models

import java.util.UUID

import org.mongodb.scala.bson.ObjectId

case class ParentChildMapping(_id: UUID, parent: ObjectId, child: ObjectId)
