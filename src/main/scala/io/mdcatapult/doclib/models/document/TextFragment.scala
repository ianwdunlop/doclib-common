package io.mdcatapult.doclib.models.document

import java.util.UUID

case class TextFragment(
                         _id: UUID,
                         document: UUID,
                         index: Int,
                         startAt: Int,
                         endAt: Int,
                         length: Int
                       )
