package io.mdcatapult.doclib.util

import io.mdcatapult.doclib.bson.{LemonLabsAbsoluteUrlCodec, _}
import io.mdcatapult.doclib.messages._
import io.mdcatapult.doclib.models._
import io.mdcatapult.doclib.models.document.TextFragment
import io.mdcatapult.doclib.models.metadata._
import io.mdcatapult.doclib.models.ner.{DocumentOccurrence, FragmentOccurrence, NerDocument, Schema}
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
      classOf[NerDocument],
      classOf[DocumentOccurrence],
      classOf[FragmentOccurrence],
      classOf[Schema],
      classOf[TextFragment],
      classOf[ConsumerVersion],
      classOf[PrefetchMsg],
      classOf[ArchiveMsg],
      classOf[EmptyMsg],
      classOf[DoclibMsg],
      classOf[NerMsg],
      classOf[SupervisorMsg],
    ),
    CodecRegistries.fromCodecs(
//      new LocalDateTimeCodec,
      new LemonLabsAbsoluteUrlCodec,
      new LemonLabsRelativeUrlCodec,
      new LemonLabsUrlCodec,
      new LemonLabsUriCodec,
      new MetaValueCodec,
      new NerOccurrenceCodec
    ),
    DEFAULT_CODEC_REGISTRY
  )
}
