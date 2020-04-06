package io.mdcatapult.doclib

import java.io.InputStream
import java.nio.charset.StandardCharsets.UTF_8

import org.apache.commons.io.IOUtils

package object util {

  def stringToInputStream(t: String): InputStream =
    IOUtils.toInputStream(t, UTF_8)

  implicit val nowUtc: FixedTimezoneNow = FixedTimezoneNow.utc()
}
