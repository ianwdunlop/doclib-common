package io.mdcatapult.doclib.models

case class ConsumerConfig(name: String,
                          concurrency: Int,
                          queue: String,
                          exchange: Option[String])
