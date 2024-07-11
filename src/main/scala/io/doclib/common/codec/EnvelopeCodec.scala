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

package io.doclib.common.codec

import io.doclib.common.codec.traits.Decodable
import io.doclib.common.messages._
import io.doclib.common.models.Origin
import io.doclib.queue.Envelope
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.{BsonReader, BsonWriter}

class EnvelopeCodec extends Codec[Envelope] with Decodable {

  lazy val originCodec: Codec[Origin] = MongoCodecs.get.get[Origin](classOf[Origin])
  lazy val metaValueCodec: MetaValueCodec = new MetaValueCodec


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
      case v: ArchiveMsg =>
        if (v.id.isDefined) w.writeString("id", v.id.get)
        if (v.source.isDefined) w.writeString("source", v.source.get)
      case v: DoclibMsg =>
        w.writeString("id", v.id)
      case v: NerMsg =>
        w.writeString("id", v.id)
        if (v.requires.isDefined) {
          w.writeName("requires")
          w.writeStartArray()
          v.requires.get.foreach(w.writeString)
          w.writeEndArray()
        }
      case v: PrefetchMsg =>
        w.writeString("source", v.source)
        if (v.origins.isDefined) {
          w.writeName("origins")
          w.writeStartArray()
          v.origins.get.foreach(t => originCodec.encode(w, t, c))
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

      case v: SupervisorMsg =>
        w.writeString("id", v.id)
        if (v.reset.isDefined) {
          w.writeName("reset")
          w.writeStartArray()
          v.reset.get.foreach(w.writeString)
          w.writeEndArray()
        }
      case _ => // do nothing
    }
    w.writeEndDocument()
  }

  override def getEncoderClass: Class[Envelope] = classOf[Envelope]
}
