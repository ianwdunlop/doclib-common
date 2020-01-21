package io.mdcatapult.doclib.models.ner

/** Count is the labelled number of times a value was found in a document.
  *
  * @param value name of the quantity being counted, such as entity type or entity group
  * @param count number of times quantity was found in doc
  * @param `type` type of the value: "entityType" or "entityGroup"
  */
case class Count(
                  value: String,
                  count: Int,
                  `type`: String,
               )
