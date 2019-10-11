package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.metadata.MetaValue

case class Stats(
                      bySchema: List[MetaValue[_]],
                      byDictionary: List[MetaValue[_]],
                      byResolvedEntity: List[MetaValue[_]],
                      byEntityType: List[MetaValue[_]],
                 )
