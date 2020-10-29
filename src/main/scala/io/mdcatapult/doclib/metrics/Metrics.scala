package io.mdcatapult.doclib.metrics

import io.prometheus.client.{Counter, Summary}

object Metrics {
  val mongoLatency: Summary = Summary.build()
    .name("mongo_latency")
    .help("Time taken for a mongo request to return.")
    .quantile(0.5, 0.05)
    .quantile(0.9, 0.01)
    .labelNames("consumer", "operation")
    .register()

  val handlerCount: Counter = Counter.build()
    .name("handler_count")
    .help("Counts number of requests received by the handler.")
    .labelNames("consumer", "source", "result")
    .register()
}
