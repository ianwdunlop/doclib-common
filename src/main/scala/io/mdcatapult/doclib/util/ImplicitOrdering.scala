package io.mdcatapult.doclib.util

import java.time.{LocalDateTime, ZoneOffset}

object ImplicitOrdering {

  implicit val localDateOrdering: Ordering[LocalDateTime] =
    Ordering.by(
      ldt =>
        if (ldt == null) // scalastyle:ignore
        0
        else
        ldt.toInstant(ZoneOffset.UTC).toEpochMilli
    )

}
