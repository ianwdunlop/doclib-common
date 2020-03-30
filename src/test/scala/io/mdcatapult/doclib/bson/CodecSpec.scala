package io.mdcatapult.doclib.bson

import java.nio.ByteBuffer

import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.io.{BasicOutputBuffer, ByteBufferBsonInput}
import org.bson.{BsonBinaryReader, BsonBinaryWriter, ByteBufNIO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class CodecSpec extends AnyFlatSpec with Matchers {

  def roundTrip[C](original: C, codec: Codec[C]): C = {
    val writer: BsonBinaryWriter = new BsonBinaryWriter(new BasicOutputBuffer())
    codec.encode(writer, original, EncoderContext.builder().build())
    val buffer: BasicOutputBuffer = writer.getBsonOutput.asInstanceOf[BasicOutputBuffer]
    val reader: BsonBinaryReader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(buffer.toByteArray))))
    codec.decode(reader, DecoderContext.builder().build())
  }
}
