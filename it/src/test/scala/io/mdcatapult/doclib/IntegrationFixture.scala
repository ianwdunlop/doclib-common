package io.mdcatapult.doclib

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}

object IntegrationFixture {

  val longTimeout: Timeout = Timeout(Span(10, Seconds))

}
