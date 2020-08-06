package io.mdcatapult.doclib.codec

import io.mdcatapult.doclib.messages.{ArchiveMsg, DoclibMsg, EmptyMsg, NerMsg, PrefetchMsg, SupervisorMsg}
import io.mdcatapult.doclib.models.{Derivative, DoclibDoc, DoclibFlag, DoclibFlagState, FileAttrs, MessageDoc, Origin, ParentChildMapping}
import io.mdcatapult.doclib.models.document.TextFragment
import io.mdcatapult.doclib.models.metadata.{MetaBoolean, MetaDateTime, MetaDouble, MetaInt, MetaString}
import io.mdcatapult.doclib.models.ner.{Count, NerDocument, Occurrence, Schema, Stats}
import io.mdcatapult.util.models.Version
import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY

object MongoCodecs {

  /** Mongo Codecs with the default set provided by [[get]] supplemented by additional Codec providers.
    * Care must be taken to ensure that Codecs are only ever added this way that will only ever by
    * defined and used within a single consumer.
    *
    * For case classes import org.mongodb.scala.bson.codecs.Macros._
    *
    * @param codecProviders defined the bson parsing of a case classes
    * @return mongo codecs
    */
  def include(codecProviders: Seq[CodecProvider]): CodecRegistry =
    if (codecProviders.nonEmpty)
      fromRegistries(
        fromProviders(codecProviders: _*),
        get
      )
    else
      get

  def includeProvider(codec: CodecProvider): CodecRegistry =
    include(Seq(codec))

  /** Mongo Codecs with the default set of codecs for case classes defined within doclib-common.
    *
    * @return mongo codecs
    */
  def get: CodecRegistry = fromRegistries(
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
      classOf[Version],
      classOf[Count],
      classOf[Stats],
      classOf[Version],
      classOf[PrefetchMsg],
      classOf[ArchiveMsg],
      classOf[EmptyMsg],
      classOf[DoclibMsg],
      classOf[NerMsg],
      classOf[SupervisorMsg],
      classOf[DoclibFlagState],
      classOf[ParentChildMapping],
      classOf[MessageDoc],
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
