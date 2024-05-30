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

package io.mdcatapult.doclib.path

import java.time.LocalDateTime

import io.mdcatapult.doclib.models.{DoclibFlag}
import io.mdcatapult.util.models.Version
import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.CodecRegistry

/** DoclibFlag codec that allows null to be written for the started LocalDateTime field.
 * This throws an exception on write in the normal case.  Alas there are some documents in Mongo
 * that are already defined with started==null.  This class overrides the case when started==null
 * to enable null to be written into Mongo.  All other cases, including all reads, use the Codec
 * as defined in the main code.
 *
 * @param codecs codecs as defined by the main code
 */
class NullWritableLocalDateTime(codecs: CodecRegistry) extends Codec[DoclibFlag] {

  private val doclibFlagCodec = codecs.get(classOf[DoclibFlag])
  private val consumerVersionCodec = codecs.get(classOf[Version])
  private val localDateTimeCodec = codecs.get(classOf[LocalDateTime])

  override def getEncoderClass: Class[DoclibFlag] = classOf[DoclibFlag]

  override def encode(writer: BsonWriter, value: DoclibFlag, encoderContext: EncoderContext): Unit = {
    if (value.started != null)
      doclibFlagCodec.encode(writer, value, encoderContext)
    else {
      writer.writeStartDocument()
      writer.writeName("key")
      writer.writeString(value.key)
      writer.writeName("version")
      encoderContext.encodeWithChildContext(consumerVersionCodec, writer, value.version)
      writer.writeNull("started")
      writeOptionLocalDateTime(writer, "ended", value.ended, encoderContext)
      writeOptionLocalDateTime(writer, "errored", value.errored, encoderContext)
      writer.writeBoolean("queued", value.isQueued)
      writer.writeEndDocument()
    }
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): DoclibFlag = {
    doclibFlagCodec.decode(reader, decoderContext)
  }

  private def writeOptionLocalDateTime(w: BsonWriter, name: String, value: Option[LocalDateTime], encoderContext: EncoderContext): Unit = {
    value match {
      case Some(x: LocalDateTime) =>
        w.writeName(name)
        encoderContext.encodeWithChildContext(localDateTimeCodec, w, x)
      case _ => w.writeNull(name)
    }
  }
}
