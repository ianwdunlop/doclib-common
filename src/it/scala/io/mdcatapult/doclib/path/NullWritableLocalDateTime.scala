package io.mdcatapult.doclib.path

import java.time.LocalDateTime

import io.mdcatapult.doclib.models.{ConsumerVersion, DoclibFlag}
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
  private val consumerVersionCodec = codecs.get(classOf[ConsumerVersion])
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
