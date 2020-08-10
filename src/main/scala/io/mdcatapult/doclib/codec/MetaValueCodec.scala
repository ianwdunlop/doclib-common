package io.mdcatapult.doclib.codec

import java.time.{Instant, LocalDateTime, ZoneOffset}

import io.mdcatapult.doclib.models.metadata._
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.{BsonReader, BsonType, BsonWriter}

class MetaValueCodec extends Codec[MetaValueUntyped] {

  /**
    * decode Bson MetaValue Document to relevant MetaValue based on supported value types
    * read the document and assigned to key/value variables as we cannot guarantee ordering of properties
    */
  override def decode(bsonReader: BsonReader, decoderContext: DecoderContext): MetaValueUntyped = {
    var key: Option[String] = None
    var value: Option[_] = None

    bsonReader.readStartDocument()

    while ({
      bsonReader.readBsonType ne BsonType.END_OF_DOCUMENT
    }) {
      bsonReader.readName match {
        case "key" =>  key = Some(bsonReader.readString())
        case "value" => value = Some(bsonReader.getCurrentBsonType match {
          case BsonType.STRING => bsonReader.readString()
          case BsonType.INT32 => bsonReader.readInt32()
          case BsonType.INT64 => bsonReader.readInt64().toInt
          case BsonType.DOUBLE => bsonReader.readDouble()
          case BsonType.BOOLEAN => bsonReader.readBoolean()
          case BsonType.DATE_TIME =>
            val tmpv = bsonReader.readDateTime()
            LocalDateTime.ofInstant(Instant.ofEpochMilli(tmpv), ZoneOffset.UTC)
          case v => throw new Exception(s"Unsupported BSON '${v.getClass}' type for MetaValue")
        })
        case v => throw new Exception(s"Invalid property name detected for MetaValue -> $v")
      }
    }
    bsonReader.readEndDocument()

    if (key.isEmpty) {
      throw new Exception("MetaValue requires a key")
    }

    value match {
      case Some(scalarVal: Boolean) => MetaBoolean(key.get, scalarVal)
      case Some(scalarVal: Int) => MetaInt(key.get, scalarVal)
      case Some(scalarVal: Double) => MetaDouble(key.get, scalarVal)
      case Some(scalarVal: LocalDateTime) => MetaDateTime(key.get, scalarVal)
      case Some(scalarVal: String) => MetaString(key.get, scalarVal)
      case _ => throw new Exception("Unable to decode value type for MetaValue")
    }
  }

  override def encode(bsonWriter: BsonWriter, t: MetaValueUntyped, encoderContext: EncoderContext): Unit = {
    val typed: MetaValue[_] = t.asInstanceOf[MetaValue[_]]
    bsonWriter.writeStartDocument()
    bsonWriter.writeString("key", typed.getKey)
    typed.getValue match {
      case scalarVal: Boolean => bsonWriter.writeBoolean("value", scalarVal)
      case scalarVal: Int => bsonWriter.writeInt32("value", scalarVal)
      case scalarVal: Double => bsonWriter.writeDouble("value", scalarVal)
      case scalarVal: LocalDateTime =>
        bsonWriter.writeDateTime("value", scalarVal.toInstant(ZoneOffset.UTC).toEpochMilli)
      case scalarVal: String => bsonWriter.writeString("value", scalarVal)
      case _ => throw new Exception("Unsupported Value type for encoding")
    }
    bsonWriter.writeEndDocument()
  }

  override def getEncoderClass: Class[MetaValueUntyped] = classOf[MetaValueUntyped]
}
