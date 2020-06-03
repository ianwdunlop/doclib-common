package io.mdcatapult.doclib.models

import java.util.UUID

import io.mdcatapult.doclib.models.metadata.MetaValueUntyped

case class ParentChildMapping(
                               _id: UUID,
                               parent: UUID,
                               child: Option[UUID] = None,
                               childPath: String,
                               metadata: Option[List[MetaValueUntyped]] = None,
                               consumer: Option[String] = None)
