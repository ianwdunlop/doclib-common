package io.mdcatapult.doclib.models

case class AppConfig(name: String,
                     concurrency: Int,
                     queue: String,
                     exchange: Option[String])
