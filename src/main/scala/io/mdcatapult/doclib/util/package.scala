package io.mdcatapult.doclib

package object util {
  def sanitizeName(name: String): String =
    name.replaceAll("""[^a-zA-Z0-9-_]""","-").replaceAll("""^[-_]+""","")
}
