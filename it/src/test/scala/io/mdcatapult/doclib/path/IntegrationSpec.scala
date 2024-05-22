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

package io.mdcatapult.doclib.path

import com.typesafe.config.Config
import io.mdcatapult.doclib.IntegrationFixture
import io.mdcatapult.doclib.codec.MongoCodecs
import org.bson.codecs.configuration.CodecRegistries.fromCodecs
import org.bson.codecs.configuration.CodecRegistry
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait IntegrationSpec extends AnyFlatSpec with Matchers {

  val longTimeout: Timeout = IntegrationFixture.longTimeout

  implicit val codecs: CodecRegistry = {
    val coreCodecs: CodecRegistry = MongoCodecs.get

    MongoCodecs.includeProvider(
      fromCodecs(
        new NullWritableLocalDateTime(coreCodecs)
      )
    )
  }

  protected def collectionName(suffix: String, prefixConfigName: String = "mongo.documents-collection")(implicit config: Config): String = {
    val prefix = config.getString(prefixConfigName)

    s"${prefix}_$suffix"
  }
}
