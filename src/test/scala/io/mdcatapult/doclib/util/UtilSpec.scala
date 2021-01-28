package io.mdcatapult.doclib.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UtilSpec extends AnyFlatSpec with Matchers {
  "a name" should "sanitise" in {
    val name = sanitizeName("""`¬!"£$%^&*()_+-=[]{}'#@~,./<>?|\'consumer.name`¬!"£$%^&*()_+-=[]{}'#@~,./<>?|\'shouldn't-be-invalid""")
    assert(name == "consumer-name------------_--------------------shouldn-t-be-invalid")
  }
}
