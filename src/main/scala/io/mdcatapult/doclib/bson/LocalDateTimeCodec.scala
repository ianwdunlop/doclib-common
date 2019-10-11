package io.mdcatapult.doclib.bson

import java.time.{Instant, LocalDateTime, ZoneOffset}

import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.{BsonReader, BsonWriter}

class LocalDateTimeCodec extends Codec[LocalDateTime] {

  override def decode(bsonReader: BsonReader, decoderContext: DecoderContext): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(bsonReader.readDateTime()), ZoneOffset.UTC)

  override def encode(bsonWriter: BsonWriter, t: LocalDateTime, encoderContext: EncoderContext): Unit =
    bsonWriter.writeDateTime(t.toInstant(ZoneOffset.UTC).toEpochMilli)

  override def getEncoderClass: Class[LocalDateTime] = classOf[LocalDateTime]
}