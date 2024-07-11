/*
 * Copyright 2024 Medicines Discovery Catapult
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.doclib.common.metrics

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
