package io.mdcatapult.doclib.bson

import io.mdcatapult.doclib.bson.traits.Decodable
import io.mdcatapult.doclib.models.ner._
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.types.ObjectId
import org.bson.{BsonReader, BsonWriter}

class NerOccurrenceCodec extends Codec[Occurrence] with Decodable {

  /**
    * decode NerOccurence Bson document to the appropriate case class
    */
  override def decode(r: BsonReader, c: DecoderContext): Occurrence = {
    Occurrence(getMap(r, c))
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