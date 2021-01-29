package io.mdcatapult.doclib

package object util {
  /**
    * Sanatises a name to make it usable for the akka actor system.
    *
    * @param name
    * @return a sanitized string with no special characters and not beginning with a hyphen or underscore.
    */
  def sanitiseName(name: String): String =
    name.replaceAll("""[^a-zA-Z0-9-_]""","-").replaceAll("""^[-_]+""","")
}
