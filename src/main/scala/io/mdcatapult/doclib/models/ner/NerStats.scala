package io.mdcatapult.doclib.models.ner

import java.time.LocalDateTime

import io.mdcatapult.doclib.models.metadata.MetaValue

case class NerStats(
                      bySchema: List[MetaValue[_]],
                      byDictionary: List[MetaValue[_]],
                      byResolvedEntity: List[MetaValue[_]],
                      byEntityType: List[MetaValue[_]],
                 )
