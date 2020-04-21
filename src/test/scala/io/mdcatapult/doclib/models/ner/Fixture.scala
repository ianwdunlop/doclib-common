package io.mdcatapult.doclib.models.ner

import java.util.UUID
import java.util.UUID.fromString

object Fixture {

  val uuid: UUID = fromString("dc83cac6-4daa-4a0b-8e52-df1543af1e8f")
  val docUuid: UUID = fromString("600029ba-ccea-4e46-9ea5-7f54996954dd")
  val fragmentUuid: UUID = fromString("f19e299d-d07e-434c-9cb7-9370832a7808")

  val uuidMongoBinary = """{"$binary": "3IPKxk2qSguOUt8VQ68ejw==", "$type": "04"}"""
  val docUuidMongoBinary = """{"$binary": "YAApuszqTkaepX9UmWlU3Q==", "$type": "04"}"""
}
