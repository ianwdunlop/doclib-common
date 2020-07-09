package io.mdcatapult.doclib.models.ner

import java.util.UUID
import java.util.UUID.fromString

object Fixture {

  val uuid: UUID = fromString("dc83cac6-4daa-4a0b-8e52-df1543af1e8f")
  val docUuid: UUID = fromString("600029ba-ccea-4e46-9ea5-7f54996954dd")
  val fragmentUuid: UUID = fromString("f19e299d-d07e-434c-9cb7-9370832a7808")
  val childDocUuid: UUID = fromString("d0ed1b2f-2660-46ae-810d-bb8a0f83a2e1")

  val uuidMongoBinary = """{"$binary": "3IPKxk2qSguOUt8VQ68ejw==", "$type": "04"}"""
  val docUuidMongoBinary = """{"$binary": "YAApuszqTkaepX9UmWlU3Q==", "$type": "04"}"""
  val childDocUuidMongoBinary = """{"$binary": "0O0bLyZgRq6BDbuKD4Oi4Q==", "$type": "04"}"""
}
