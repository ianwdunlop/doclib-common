package io.mdcatapult.doclib.models.ner

import java.time.LocalDateTime

case class NerSchema(
                 key: String,
                 tool: String,
                 config: String,
                 version: String,
                 lastRun: LocalDateTime
                 )
