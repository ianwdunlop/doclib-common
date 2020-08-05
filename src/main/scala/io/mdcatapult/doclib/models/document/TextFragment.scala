package io.mdcatapult.doclib.models.document

import java.util.UUID

import org.mongodb.scala.bson.ObjectId

case class TextFragment(
                         _id: UUID,
                         document: ObjectId,
                         index: Int,
                         startAt: Int,
                         endAt: Int,
                         length: Int
                       )
