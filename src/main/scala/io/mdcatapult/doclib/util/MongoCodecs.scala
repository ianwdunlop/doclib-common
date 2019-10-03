package io.mdcatapult.doclib.util

import io.mdcatapult.doclib.bson._
import io.mdcatapult.doclib.messages.PrefetchMsg
import io.mdcatapult.doclib.models._
import io.mdcatapult.doclib.models.metadata._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecRegistries, CodecRegistry}
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object MongoCodecs {

  def get : CodecRegistry = fromRegistries(
    fromProviders(
      classOf[DoclibDoc],
      classOf[Origin],
      classOf[FileAttrs],
      classOf[DoclibFlag],
      classOf[MetaInt],
      classOf[MetaString],
      classOf[MetaDouble],
      classOf[MetaBoolean],
      classOf[MetaDateTime],
      classOf[Derivative],
    ),
    CodecRegistries.fromCodecs(
      new LocalDateTimeCodec,
      new LemonLabsAbsoluteUrlCodec,
      new LemonLabsRelativeUrlCodec,
      new LemonLabsUrlCodec,
      new MetaValueCodec,
    ),
    DEFAULT_CODEC_REGISTRY
  )
}
