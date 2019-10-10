package io.mdcatapult.doclib.models.ner

import java.time.LocalDateTime

case class Schema(
                 key: String,
                 tool: String,
                 config: String,
                 version: String,
                 lastRun: LocalDateTime
                 )
