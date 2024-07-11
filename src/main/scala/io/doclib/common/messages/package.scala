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

package io.doclib.common

import java.time.LocalDateTime

import io.lemonlabs.uri.Uri
import io.doclib.common.models.Origin
import io.doclib.common.models.metadata._
import io.doclib.queue.Envelope

package object messages {

  def getValFrom[T](key: String, values: Map[String, Any]): Option[T] = {
    if (!values.exists(_._1 == key)) None
    else values(key).asInstanceOf[T] match {
      case None => None
      case v => Some(v)
    }
  }

  def decodeMetaData(data: List[Map[String, Any]]): List[MetaValueUntyped] = {
    data.map(mv => mv("value") match {
      case scalarVal: Boolean => MetaBoolean(mv("key").asInstanceOf[String], scalarVal)
      case scalarVal: Int => MetaInt(mv("key").asInstanceOf[String], scalarVal)
      case scalarVal: Double => MetaDouble(mv("key").asInstanceOf[String], scalarVal)
      case scalarVal: LocalDateTime => MetaDateTime(mv("key").asInstanceOf[String], scalarVal)
      case scalarVal: String => MetaString(mv("key").asInstanceOf[String], scalarVal)
      case other => throw new MatchError(other)
    })
  }

  def msgFromEnvelope(values: Map[String, Any]): Envelope = {

    if (values.contains("source") && values.contains("id")) {
      ArchiveMsg(
        id = getValFrom[String]("id", values),
        source = getValFrom[String]("source", values)
      )
    } else if (values.contains("source")) {
      PrefetchMsg(
        source = getValFrom[String]("source", values).get,
        origins = getValFrom[List[Map[String, _]]]("origins", values).map(a => a.map(o => Origin(
            scheme= o("scheme").asInstanceOf[String],
            hostname = getValFrom[String]("hostname", o),
            uri = getValFrom[String]("uri", o).map(Uri.parse),
            headers = getValFrom[Map[String, Seq[String]]]("headers", o),
            metadata = getValFrom[List[Map[String, Any]]]("metadata",o).map(decodeMetaData)
        ))),
        tags = getValFrom[List[String]]("tags", values),
        metadata = getValFrom[List[Map[String, Any]]]("metadata", values).map(decodeMetaData),
        derivative = getValFrom[Boolean]("derivative", values)
      )
    } else if (values.contains("id") && values.contains("requires")) {
      NerMsg(
        id = getValFrom[String]("id", values).get,
        requires = getValFrom[List[String]]("requires", values)
      )
    } else if (values.contains("id") && values.contains("reset")) {
      SupervisorMsg(
        id = getValFrom[String]("id", values).get,
        reset = getValFrom[List[String]]("reset", values)
      )
    } else if (values.contains("id")) {
      DoclibMsg(id = getValFrom[String]("id", values).get)
    } else {
      EmptyMsg()
    }
  }
}
