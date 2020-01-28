package io.mdcatapult.doclib.bson

import java.time.{LocalDateTime, ZoneOffset}

import io.mdcatapult.doclib.messages._
import io.mdcatapult.doclib.models.ner.{FragmentOccurrence, Occurrence}
import io.mdcatapult.klein.queue.Envelope
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.types.ObjectId
import org.bson.{BsonReader, BsonType, BsonWriter}
import io.mdcatapult.doclib.bson.MetaValueCodec
import io.mdcatapult.doclib.bson.traits.Decodable
import io.mdcatapult.doclib.models.Origin

import scala.collection.mutable
import io.mdcatapult.doclib.util.MongoCodecs

class EnvelopeCodec extends Codec[Envelope] with Decodable {

  val originCodec: Codec[Origin] = MongoCodecs.get.get[Origin](classOf[Origin])
  val metaValueCodec: MetaValueCodec = new MetaValueCodec


  /**
    * decode Bson MetaValue Document to relevant MetaValue based on supported value types
    * read the document and assigned to key/value variables as we cannot guarantee ordering of properties
    */
  override def decode(r: BsonReader, c: DecoderContext): Envelope = {
    msgFromEnvelope(getMap(r, c))
  }
  
  override def encode(w: BsonWriter, t: Envelope, c: EncoderContext): Unit = {
    w.writeStartDocument()
    t match {
      case v: ArchiveMsg ⇒
        if (v.id.isDefined) w.writeObjectId("id", new ObjectId(v.id.get))
        if (v.source.isDefined) w.writeString("source", v.source.get)
      case v: DoclibMsg ⇒
        w.writeObjectId("id", new ObjectId(v.id))
      case v: NerMsg ⇒
        w.writeObjectId("id", new ObjectId(v.id))
        if (v.requires.isDefined) {
          w.writeName("requires")
          w.writeStartArray()
          v.requires.get.foreach(w.writeString)
          w.writeEndArray()
        }
      case v: PrefetchMsg ⇒
        w.writeString("source", v.source)
        if (v.origin.isDefined) {
          w.writeName("origin")
          w.writeStartArray()
          v.origin.get.foreach(t => originCodec.encode(w, t, c))
          w.writeEndArray()
        }
        if (v.metadata.isDefined) {
          w.writeName("metadata")
          w.writeStartArray()
          v.metadata.get.foreach(t => metaValueCodec.encode(w, t, c))
          w.writeEndArray()
        }
        if (v.tags.isDefined) {
          w.writeName("tags")
          w.writeStartArray()
          v.tags.get.foreach(w.writeString)
          w.writeEndArray()
        }
        if (v.derivative.isDefined) {
          w.writeName("derivative")
          w.writeBoolean(v.derivative.getOrElse(false))
        }

      case v: SupervisorMsg ⇒
        w.writeObjectId("id", new ObjectId(v.id))
      case _ ⇒ // do nothing
    }
    w.writeEndDocument()
  }

  override def getEncoderClass: Class[Envelope] = classOf[Envelope]
}