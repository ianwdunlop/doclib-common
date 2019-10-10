package io.mdcatapult.doclib.bson

import java.time.{LocalDateTime, ZoneOffset}

import io.mdcatapult.doclib.models.metadata._
import io.mdcatapult.doclib.models.ner.{DocumentOccurrence, FragmentOccurrence, Occurrence}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.types.ObjectId
import org.bson.{BsonReader, BsonType, BsonWriter}
import org.mongodb.scala.bson.BsonNull

import scala.collection.mutable

class NerOccurrenceCodec extends Codec[Occurrence] {

  /**
    * decode Bson MetaValue Document to relevant MetaValue based on supported value types
    * read the document and assigned to key/value variables as we cannot guarantee ordering of properties
    */
  override def decode(r: BsonReader, decoderContext: DecoderContext): Occurrence = {

    val values: mutable.Map[String, Any] = mutable.Map[String, Any]()

    val toOption: List[String] = List(
      "correctedValue", "correctedValueHash",
      "resolvedEntity", "resolvedEntityHash"
    )

    r.readStartDocument()
    while ({
      r.readBsonType ne BsonType.END_OF_DOCUMENT
    }) {
      val name = r.readName
      val value = r.getCurrentBsonType match {
          case BsonType.STRING ⇒ r.readString()
          case BsonType.INT32 ⇒ r.readInt32()
          case BsonType.INT64 ⇒ r.readInt64().toInt
          case BsonType.DOUBLE ⇒ r.readDouble()
          case BsonType.BOOLEAN ⇒ r.readBoolean()
          case BsonType.DATE_TIME ⇒ LocalDateTime.ofEpochSecond(r.readDateTime(), 0, ZoneOffset.UTC)
          case BsonType.OBJECT_ID ⇒ r.readObjectId()
          case BsonType.NULL ⇒
            r.readNull()
            None
          case _ ⇒ throw new Exception(s"Unsupported BSON type for ${name}")
        }
      values(name) = value
    }
    r.readEndDocument()

    Occurrence(values.toMap)
  }

  def writeOptionString(w: BsonWriter, name: String, value: Option[String]): Unit = {
    value match {
      case Some(v: String) ⇒ w.writeString(name, v)
      case _ ⇒ w.writeNull(name)
    }
  }
  
  override def encode(w: BsonWriter, t: Occurrence, encoderContext: EncoderContext): Unit = {
    w.writeStartDocument()

    w.writeString("entityType", t.entityType)
    w.writeString("schema", t.schema)
    w.writeInt32("characterStart", t.characterStart)
    w.writeInt32("characterEnd", t.characterEnd)

    t match {
      case v: FragmentOccurrence ⇒ w.writeInt32("wordIndex", v.wordIndex)
      case _ ⇒ // do nothing
    }

    t.fragment match {
      case Some(v: ObjectId) ⇒ w.writeObjectId("fragment", v)
      case _ ⇒ w.writeNull("fragment")
    }
    writeOptionString(w, "correctedValue", t.correctedValue)
    writeOptionString(w, "correctedValueHash", t.correctedValueHash)
    writeOptionString(w, "resolvedEntity", t.resolvedEntity)
    writeOptionString(w, "resolvedEntityHash", t.resolvedEntityHash)
    w.writeString("type", t.`type`)
    w.writeEndDocument()
  }

  override def getEncoderClass: Class[Occurrence] = classOf[Occurrence]
}