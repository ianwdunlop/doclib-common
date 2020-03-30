package io.mdcatapult.doclib.models

import java.time.LocalDateTime

import play.api.libs.json.{Format, Json}

object FileAttrs {
  implicit val msgFormatter: Format[FileAttrs] = Json.format[FileAttrs]
}

case class FileAttrs(
                      path: String,
                      name: String,
                      mtime: LocalDateTime,
                      ctime: LocalDateTime,
                      atime: LocalDateTime,
                      size: Long,
                    )
