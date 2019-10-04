package io.mdcatapult.doclib.models.ner

case class NerOccurence(
                         entityType: String,
                         schema: String,
                         occurrenceType: String,
                         value: String,
                         resolvedEntity: String,
                         characterStart: Option[Int] = None,
                         characterEnd: Option[Int] = None,
                         xpath: Option[String] = None
                 )
