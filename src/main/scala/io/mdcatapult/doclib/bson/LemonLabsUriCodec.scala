package io.mdcatapult.doclib.bson

import io.lemonlabs.uri.Uri
import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}

import scala.util.{Failure, Success}

class LemonLabsUriCodec extends Codec[Uri]{
  override def decode(bsonReader: BsonReader, decoderContext: DecoderContext): Uri = {
        Uri.parseTry(bsonReader.readString()) match {
          case Success(uri) ⇒ uri
          case Failure(e) ⇒ throw e
        }
  }

  override def encode(bsonWriter: BsonWriter, value: Uri, encoderContext: EncoderContext): Unit = {
    bsonWriter.writeString(value.toString())
  }

  override def getEncoderClass: Class[Uri] = classOf[Uri]
}