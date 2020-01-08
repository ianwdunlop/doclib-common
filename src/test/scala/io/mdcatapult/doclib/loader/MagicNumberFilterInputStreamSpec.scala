package io.mdcatapult.doclib.loader

import org.apache.tools.ant.filters.StringInputStream
import org.scalatest.{FlatSpec, Matchers}

class MagicNumberFilterInputStreamSpec extends FlatSpec with Matchers {

  private val truncator = MagicNumberFilterInputStream.toTruncateAnyWith(List("RDX2", "RDA2").map(_.getBytes)) _

  private val moreText = "more could be read, so end of stream not reached"

  "A MagicNumberFilterInputStream" should "give no content when magic number matches on reject" in {
    val text = "RDX2 is to be matched"
    val bufferSize = text.getBytes.length
    val buffer = new Array[Byte](bufferSize)

    val bytesRead = truncator(new StringInputStream(text + moreText)).read(buffer, 0, buffer.length)

    buffer should equal (new Array[Byte](bufferSize))
    bytesRead should be (-1)
  }

  it should "give no content when different magic number matches on reject" in {
    val text = "RDA2 is to be matched"
    val bufferSize = text.getBytes.length
    val buffer = new Array[Byte](bufferSize)

    val bytesRead = truncator(new StringInputStream(text + moreText)).read(buffer, 0, buffer.length)

    buffer should equal (new Array[Byte](bufferSize))
    bytesRead should be (-1)
  }

  it should "give no more content after truncating" in {
    val text = "RDX2 is to be matched"
    val bufferSize = text.getBytes.length
    val buffer = new Array[Byte](bufferSize)

    val in = truncator(new StringInputStream(text + moreText))

    in.read(buffer, 0, buffer.length)
    val bytesRead = in.read(buffer, 0, buffer.length)

    buffer should equal (new Array[Byte](bufferSize))
    bytesRead should be (-1)
  }

  it should "read content if magic number isn't matched" in {
    val text = "Here is some text that is expected to be read"
    val bufferSize = text.getBytes.length
    val buffer = new Array[Byte](bufferSize)

    val in = truncator(new StringInputStream(text + moreText))
    val bytesRead = in.read(buffer, 0, buffer.length)

    buffer should equal (text.getBytes)
    bytesRead should be (bufferSize)
  }

  it should "read content even if magic number occurs on second read" in {
    val text = "Here is some text that is expected to be read"
    val textWithMagicNumber = "RDX2 is magic, but isn't at the start"

    val firstLineLength = text.getBytes.length

    val bufferSize = textWithMagicNumber.getBytes.length
    val buffer = new Array[Byte](bufferSize)

    val in = truncator(new StringInputStream(text + textWithMagicNumber + moreText))

    in.read(new Array[Byte](firstLineLength), 0, firstLineLength)

    val bytesRead = in.read(buffer, 0, bufferSize)

    buffer should equal (textWithMagicNumber.getBytes())
    bytesRead should be (bufferSize)
  }
}
