package io.mdcatapult.doclib.models.ner

import io.mdcatapult.doclib.models.ner.Occurrence.md5
import org.scalacheck.Arbitrary.arbString
import org.scalacheck.Gen.{asciiPrintableStr, const, listOf, oneOf, option, posNum}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

object OccurrenceSpec extends Properties("Occurrence.md5") {

  private val nonNegativeInt = oneOf(const(0), posNum[Int])

  private val genOccurrence: Gen[Occurrence] = for {
    entityType <- asciiPrintableStr
    entityGroup <- option(asciiPrintableStr)
    schema <- asciiPrintableStr
    characterStart <- nonNegativeInt
    characterEnd <- posNum[Int]
    fragment <- option(arbString)
    correctedValue <- option(arbString)
    correctedValueHash <- option(asciiPrintableStr)
    resolvedEntity <- option(arbString)
    resolvedEntityHash <- option(asciiPrintableStr)
    occurrenceType <- oneOf("document", "fragment")
  } yield Occurrence(
    Map(
      "entityType" -> entityType,
      "entityGroup" -> entityGroup,
      "schema" -> schema,
      "characterStart" -> characterStart,
      "characterEnd" -> characterEnd,
      "fragment" -> fragment,
      "correctedValue" -> correctedValue,
      "correctedValueHash" -> correctedValueHash,
      "resolvedEntity" -> resolvedEntity,
      "resolvedEntityHash" -> resolvedEntityHash,
      "type" -> occurrenceType
    )
  )

  property("empty list of occurrences gives consistent value") = forAll(const(List[Occurrence]())) { xs =>
    md5(xs) == "d41d8cd98f00b204e9800998ecf8427e"
  }

  property("individual occurrences are unique") = forAll(genOccurrence, genOccurrence) { (a, b) =>
    md5(Seq(a)) != md5(Seq(b))
  }

  property("individual occurrences are reproducible") = forAll(genOccurrence) { x =>
    md5(Seq(x)) == md5(Seq(x))
  }

  property("list of occurrences are reversible (order independent)") = forAll(listOf(genOccurrence)) { xs =>
    md5(xs.reverse) == md5(xs)
  }

  property("list of occurrences use all occurrences") = forAll(listOf(genOccurrence), nonNegativeInt) { (xs, i) => {
    if (xs.isEmpty) {
      true
    } else {
      val (left, right) = xs.splitAt(i % xs.length)
      val xsWithIndexedRemoved = left ::: right.tail

      md5(xsWithIndexedRemoved) != md5(xs)
    }
  }}
}
