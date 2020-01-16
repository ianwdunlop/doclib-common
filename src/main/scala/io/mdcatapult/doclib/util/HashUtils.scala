package io.mdcatapult.doclib.util

import java.io.{File, FileInputStream}
import java.math.BigInteger
import java.security.{DigestInputStream, MessageDigest}

object HashUtils {

  /**
   * Generates and MD5 hash of the file contents
   * @param source file whose contents is to be hashed
   * @return
   */
  def md5(source: File): String = {
    val buffer = new Array[Byte](8192)
    val md5 = MessageDigest.getInstance("MD5")

    val dis = new DigestInputStream(new FileInputStream(source), md5)
    try {
      while (dis.read(buffer) != -1) {}
    } finally {
      dis.close()
    }

    md5.digest.map("%02x".format(_)).mkString
  }

  /**
    * Generates an md5 hash of a string
    * @param value raw text to be hashed
    * @return
    */
  def md5(value : String): String =
    MessageDigest.getInstance("MD5").digest(value.getBytes).map("%02x".format(_)).mkString

  /**
   * Generates an md5 hash of a string
   * @param s text to hash
   * @return
   */
  @deprecated("can give spurious matches, use md5(String) instead")
  def md5VariableLengthHashString(s: String): String = {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.getBytes)

    new BigInteger(1, digest).toString(16)
  }

}
