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

package io.doclib.common.codec

import io.lemonlabs.uri.AbsoluteUrl
import org.bson._
import org.bson.codecs._

import scala.util._

class LemonLabsAbsoluteUrlCodec extends Codec[AbsoluteUrl] {

  override def decode(bsonReader: BsonReader, decoderContext: DecoderContext): AbsoluteUrl =
    AbsoluteUrl.parseTry(bsonReader.readString()) match {
      case Success(uri) => uri
      case Failure(e) => throw e
    }

  override def encode(bsonWriter: BsonWriter, t: AbsoluteUrl, encoderContext: EncoderContext): Unit =
    bsonWriter.writeString(t.toString)

  override def getEncoderClass: Class[AbsoluteUrl] = classOf[AbsoluteUrl]


}
