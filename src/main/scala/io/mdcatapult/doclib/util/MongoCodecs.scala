package io.mdcatapult.doclib.util

import io.mdcatapult.doclib.bson._
import io.mdcatapult.doclib.messages._
import io.mdcatapult.doclib.models._
import io.mdcatapult.doclib.models.document.TextFragment
import io.mdcatapult.doclib.models.metadata._
import io.mdcatapult.doclib.models.ner.{Count, NerDocument, Occurrence, Schema, Stats}
import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
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
      classOf[NerDocument],
      classOf[Occurrence],
      classOf[Schema],
      classOf[TextFragment],
      classOf[ConsumerVersion],
      classOf[Count],
      classOf[Stats],
      classOf[ConsumerVersion],
      classOf[PrefetchMsg],
      classOf[ArchiveMsg],
      classOf[EmptyMsg],
      classOf[DoclibMsg],
      classOf[NerMsg],
      classOf[SupervisorMsg],
      classOf[DoclibFlagState],
    ),
    fromCodecs(
      new LemonLabsAbsoluteUrlCodec,
      new LemonLabsRelativeUrlCodec,
      new LemonLabsUrlCodec,
      new LemonLabsUriCodec,
      new MetaValueCodec,
      new EnvelopeCodec,
      new UuidCodec(UuidRepresentation.STANDARD),
    ),
    DEFAULT_CODEC_REGISTRY
  )
}
