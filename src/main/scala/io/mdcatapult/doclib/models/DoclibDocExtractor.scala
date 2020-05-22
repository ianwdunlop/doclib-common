package io.mdcatapult.doclib.models

import java.time.temporal.TemporalAmount

import com.typesafe.config.Config
import io.mdcatapult.doclib.util.Now

object DoclibDocExtractor {

  def apply()(implicit config: Config): DoclibDocExtractor = {

    val defaultFlagKey =
      config.getString("doclib.flag")

    val recentRunTolerance: TemporalAmount =
      if (config.hasPath("doclib.tolerance"))
        config.getTemporal("doclib.tolerance")
      else
        java.time.Duration.ofSeconds(10)

    DoclibDocExtractor(defaultFlagKey, recentRunTolerance)
  }
}

case class DoclibDocExtractor(defaultFlagKey: String, recentRunTolerance: TemporalAmount) {

  /** Determine if a consumer with a given (or default) flag key has started within the configured recent
   * run tolerance.  The ultimate state of the flag is ignored - only the start timestamp is considered.
   *
   * @param d       doc with array of flag states
   * @param flagKey key of flag of interest
   * @return true if recently run
   */
  def isRunRecently(d: DoclibDoc, flagKey: Option[String] = None)(implicit time: Now): Boolean = {
    val key = flagKey.getOrElse(defaultFlagKey)

    d.getFlag(key).exists(
      _.started.exists { _.plus(recentRunTolerance).isAfter(time.now()) }
    )
  }
}
