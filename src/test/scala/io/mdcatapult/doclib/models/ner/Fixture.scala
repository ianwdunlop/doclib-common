package io.mdcatapult.doclib.models.ner

import java.util.UUID
import java.util.UUID.fromString

import org.mongodb.scala.bson.ObjectId

object Fixture {

  val uuid: UUID = fromString("dc83cac6-4daa-4a0b-8e52-df1543af1e8f")
  val docId: ObjectId = new ObjectId("5d9f0662679b3e75b2781c94")
  val fragmentUuid: UUID = fromString("f19e299d-d07e-434c-9cb7-9370832a7808")
  val childDocId: ObjectId = new ObjectId("5f2a6054bb1c5d3b1c64f9c6")

  val uuidMongoBinary = """{"$binary": "3IPKxk2qSguOUt8VQ68ejw==", "$type": "04"}"""
  val docIdMongo = """{"$oid": "5d9f0662679b3e75b2781c94"}"""
  val childDocIdMongo = """{"$oid": "5f2a6054bb1c5d3b1c64f9c6"}"""
}
