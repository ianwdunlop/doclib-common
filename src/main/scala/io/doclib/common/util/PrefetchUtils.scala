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

package io.doclib.common.util

import com.typesafe.config.Config
import io.doclib.common.messages.PrefetchMsg
import io.doclib.common.models.{Derivative, DoclibDoc, Origin}
import io.doclib.common.models.metadata.{MetaString, MetaValueUntyped}
import io.doclib.queue.Sendable
import cats.syntax.option._
import org.bson.types.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.addEachToSet
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.Future

/**
 * Convenience methods to:
 *   - add derivatives to a parent document mongo record
 *   - filter out derivative docs from a parent document's metadata
 *   - send prefetch messages to rabbit for a parent document's derivatives
 *   - fetch a document from mongo
 */
trait PrefetchUtils {

  val doclibConfig: Config
  val prefetchQueue: Sendable[PrefetchMsg]
  val derivativeType: String
  val doclibCollection: MongoCollection[DoclibDoc]

  /**
   * Filter out existing derivative.type metadata from
   * a [[io.mdcatapult.doclib.models.DoclibDoc]]
   * @param doc The [[io.mdcatapult.doclib.models.DoclibDoc]] to be filtered
   * @return Filtered metadata list
   */
  def filterDerivatives(doc: DoclibDoc): List[MetaValueUntyped] = {
    doc.metadata.getOrElse(Nil).filter(_.getKey != "derivative.type")
  }

  def enqueue(source: List[String], doc: DoclibDoc): List[String] = {
    val derivativeMetadata = List[MetaValueUntyped](MetaString("derivative.type", derivativeType))
    source.foreach(path => {
      prefetchQueue.send(PrefetchMsg(
        source = path,
        origins = Some(List(Origin(
          scheme = "mongodb",
          metadata = List(
            MetaString("db", doclibConfig.getString("mongo.database")),
            MetaString("collection", doclibConfig.getString("mongo.collection")),
            MetaString("_id", doc._id.toHexString)).some
        ))),
        tags = doc.tags,
        metadata = (filterDerivatives(doc)::: derivativeMetadata).some,
        derivative =  true.some
      ))
    })
    source
  }

  def persist(doc: DoclibDoc, files: List[String]): Future[Option[UpdateResult]] = {
    doclibCollection.updateOne(equal("_id", doc._id),
      addEachToSet("derivatives", files.map(path => Derivative(derivativeType, path)):_*),
    ).toFutureOption()
  }

  def fetch(id: String): Future[Option[DoclibDoc]] =
    doclibCollection.find(equal("_id", new ObjectId(id))).first().toFutureOption()
}
