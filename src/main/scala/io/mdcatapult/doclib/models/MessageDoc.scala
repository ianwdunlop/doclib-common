package io.mdcatapult.doclib.models

import io.mdcatapult.doclib.messages.DoclibMsg
import org.mongodb.scala.bson.ObjectId

case class MessageDoc(_id: ObjectId, doclib: DoclibMsg)
