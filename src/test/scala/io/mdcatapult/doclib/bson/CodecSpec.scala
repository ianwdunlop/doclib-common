package io.mdcatapult.doclib.bson

import java.nio.ByteBuffer

import io.mdcatapult.doclib.util.MongoCodecs
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.io.{BasicOutputBuffer, ByteBufferBsonInput}
import org.bson.{BsonBinaryReader, BsonBinaryWriter, ByteBufNIO}
import org.scalatest.{FlatSpec, Matchers}

abstract class CodecSpec extends FlatSpec with Matchers {

  val registry = MongoCodecs.get

  def roundTrip[C](original: C, codec: Codec[C]): C = {
    val writer: BsonBinaryWriter = new BsonBinaryWriter(new BasicOutputBuffer())
    codec.encode(writer, original, EncoderContext.builder().build())
    val buffer: BasicOutputBuffer = writer.getBsonOutput.asInstanceOf[BasicOutputBuffer];
    val reader: BsonBinaryReader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(buffer.toByteArray))))
    codec.decode(reader, DecoderContext.builder().build())
  }
}
