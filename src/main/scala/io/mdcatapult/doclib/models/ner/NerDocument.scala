package io.mdcatapult.doclib.models.ner

import java.util.UUID

case class NerDocument(
                      _id: UUID,
                      value: String,
                      hash: String,
                      document: UUID,
                      entityType: Option[String] = None,
                      entityGroup: Option[String] = None,
                      resolvedEntity: Option[String] = None,
                      resolvedEntityHash: Option[String] = None,
                      schema: Option[Schema] = None
                 )
