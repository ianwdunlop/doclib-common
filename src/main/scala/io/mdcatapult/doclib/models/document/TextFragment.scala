package io.mdcatapult.doclib.models.document

import org.mongodb.scala.bson.ObjectId

case class TextFragment(
                       _id: ObjectId,
                       document: ObjectId,
                       index: Int,
                       startAt: Int,
                       endAt: Int,
                       length: Int
                       )
