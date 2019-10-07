package io.mdcatapult.doclib.bson

import java.time.{LocalDateTime, ZoneOffset}

import io.mdcatapult.doclib.models.metadata._
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.{BsonReader, BsonType, BsonWriter, Document}

class MetaValueCodec extends Codec[MetaValueUntyped] {

  /**
    * decode Bson MetaValue Document to relevant MetaValue based on supported value types
    * read the document and assigned to key/value variables as we cannot guarantee ordering of properties
    */
  override def decode(bsonReader: BsonReader, decoderContext: DecoderContext): MetaValueUntyped = {
    var key: Option[String] = None
    var value: Option[Any] = None

    bsonReader.readStartDocument()
    while ({
      bsonReader.readBsonType ne BsonType.END_OF_DOCUMENT
    }) {
      bsonReader.readName match {
        case "key" ⇒  key = Some(bsonReader.readString())
        case "value" ⇒ value = Some(bsonReader.getCurrentBsonType match {
          case BsonType.STRING ⇒ bsonReader.readString()
          case BsonType.INT32 ⇒ bsonReader.readInt32()
          case BsonType.INT64 ⇒ bsonReader.readInt64().toInt
          case BsonType.DOUBLE ⇒ bsonReader.readDouble()
          case BsonType.BOOLEAN ⇒ bsonReader.readBoolean()
          case BsonType.DATE_TIME ⇒ LocalDateTime.ofEpochSecond(bsonReader.readDateTime(), 0, ZoneOffset.UTC)
          case _ ⇒ throw new Exception("Unsupported BSON type for MetaValue")
        })
        case _ ⇒ throw new Exception("Invalid property name detected for MetaValue")
      }
    }
    if (key.isEmpty || value.isEmpty) {
      throw new Exception("MetaValue must consist of both key and value properties")
    }
    bsonReader.readEndDocument()

    value match {
      case Some(scalarVal: Boolean) ⇒ MetaBoolean(key.get, scalarVal)
      case Some(scalarVal: Int) ⇒ MetaInt(key.get, scalarVal)
      case Some(scalarVal: Double) ⇒ MetaDouble(key.get, scalarVal)
      case Some(scalarVal: LocalDateTime) ⇒ MetaDateTime(key.get, scalarVal)
      case Some(scalarVal: String) ⇒ MetaString(key.get, scalarVal)
      case _ ⇒ throw new Exception("Unable to decode MetaValue value")
    }

  }

  override def encode(bsonWriter: BsonWriter, t: MetaValueUntyped, encoderContext: EncoderContext): Unit = {
    val typed: MetaValue[_] = t.asInstanceOf[MetaValue[_]]
    bsonWriter.writeStartDocument()
    bsonWriter.writeName(typed.getKey)
    typed.getValue match {
      case scalarVal: Boolean ⇒ bsonWriter.writeBoolean(scalarVal)
      case scalarVal: Int ⇒ bsonWriter.writeInt32(scalarVal)
      case scalarVal: Double ⇒ bsonWriter.writeDouble(scalarVal)
      case scalarVal: LocalDateTime ⇒ bsonWriter.writeDateTime(scalarVal.toInstant(ZoneOffset.UTC).toEpochMilli)
      case scalarVal: String ⇒ bsonWriter.writeString(scalarVal)
      case _ ⇒ throw new Exception("Unsupported Value type for encoding")
    }
    bsonWriter.writeEndDocument()
  }

  override def getEncoderClass: Class[MetaValueUntyped] = classOf[MetaValueUntyped]
}