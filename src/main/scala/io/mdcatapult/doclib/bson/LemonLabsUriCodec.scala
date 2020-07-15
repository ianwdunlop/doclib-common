package io.mdcatapult.doclib.bson

import io.lemonlabs.uri.Uri
import org.bson._
import org.bson.codecs._

import scala.util._

class LemonLabsUriCodec extends Codec[Uri] {

  override def decode(bsonReader: BsonReader, decoderContext: DecoderContext): Uri =
    Uri.parseTry(bsonReader.readString()) match {
      case Success(uri) => uri
      case Failure(e) => throw e
    }

  override def encode(bsonWriter: BsonWriter, t: Uri, encoderContext: EncoderContext): Unit =
    bsonWriter.writeString(t.toString)

  override def getEncoderClass: Class[Uri] = classOf[Uri]

}
