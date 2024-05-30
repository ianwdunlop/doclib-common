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

package io.mdcatapult.doclib.codec

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
