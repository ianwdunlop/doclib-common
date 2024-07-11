/*
 * Copyright 2024 Medicines Discovery Catapult
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.doclib.common.codec.traits

import java.time.{LocalDateTime, ZoneOffset}

import org.bson.codecs.DecoderContext
import org.bson.{BsonReader, BsonType}

import scala.collection.mutable

trait Decodable {

  def getValue(r: BsonReader, c: DecoderContext): Any = {
    r.getCurrentBsonType match {
      case BsonType.STRING => r.readString()
      case BsonType.INT32 => r.readInt32()
      case BsonType.INT64 => r.readInt64().toInt
      case BsonType.DOUBLE => r.readDouble()
      case BsonType.BOOLEAN => r.readBoolean()
      case BsonType.DATE_TIME => LocalDateTime.ofEpochSecond(r.readDateTime(), 0, ZoneOffset.UTC)
      case BsonType.OBJECT_ID => r.readObjectId()
      case BsonType.ARRAY => getList(r, c)
      case BsonType.DOCUMENT => getMap(r, c)
      case BsonType.NULL =>
        r.readNull()
        None
      case _ => throw new Exception(s"Unsupported BSON type detected")
    }
  }

  def getList(r: BsonReader, c: DecoderContext): List[Any] = {
    val buffer = mutable.ListBuffer[Any]()
    r.readStartArray()
    while ({
      r.readBsonType ne BsonType.END_OF_DOCUMENT
    }) buffer.addOne(getValue(r,c))
    r.readEndArray()
    buffer.toList
  }

  def getMap(r: BsonReader, c: DecoderContext): Map[String, Any] = {
    val values: mutable.Map[String, Any] = mutable.Map[String, Any]()

    r.readStartDocument()
    while ({
      r.readBsonType ne BsonType.END_OF_DOCUMENT
    }) {
      val name = r.readName
      val value = getValue(r, c)
      values(name) = value
    }
    r.readEndDocument()
    values.toMap
  }


}
