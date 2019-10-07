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
    val document = new Document

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
      }
    }
    bsonReader.readEndDocument()

    value match {
      case Some(v: Int) ⇒ MetaInt(key.get, v)
      case Some(v: Double) ⇒ MetaDouble(key.get, v)
      case Some(v: String) ⇒ MetaString(key.get, v)
      case Some(v: Int) ⇒ MetaInt(key.get, v)
      case Some(v: Boolean) ⇒ MetaBoolean(key.get, v)
      case Some(v: LocalDateTime) ⇒ MetaDateTime(key.get, v)
      case None ⇒ throw new Exception("Unable to decode MetaValue value")
    }

  }

  override def encode(bsonWriter: BsonWriter, t: MetaValueUntyped, encoderContext: EncoderContext): Unit = {

    val typed: MetaValue[_] = t match {
      case v: MetaString ⇒ v
      case v: MetaDateTime ⇒ v
      case v: MetaDouble ⇒ v
      case v: MetaInt ⇒ v
      case v: MetaString ⇒ v
      case _ ⇒ throw new Exception("Unsupported MetaValue type for detection")
    }
    bsonWriter.writeStartDocument()
    bsonWriter.writeName(typed.getKey)
    typed.getValue match {
      case v: LocalDateTime ⇒ bsonWriter.writeDateTime(v.toInstant(ZoneOffset.UTC).toEpochMilli)
      case v: Boolean ⇒ bsonWriter.writeBoolean(v)
      case v: Int ⇒ bsonWriter.writeInt32(v)
      case v: Double ⇒ bsonWriter.writeDouble(v)
      case v: String ⇒ bsonWriter.writeString(v)
      case _ ⇒ throw new Exception("Unsupported Value type for encoding")
    }
    bsonWriter.writeEndDocument()
  }

  override def getEncoderClass: Class[MetaValueUntyped] = classOf[MetaValueUntyped]
}