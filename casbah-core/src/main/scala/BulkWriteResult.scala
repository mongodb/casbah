/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/mongodb/casbah
 *
 */

package com.mongodb.casbah

import scala.collection.mutable
import scala.jdk.CollectionConverters._

import com.mongodb.{ BulkWriteResult => JBulkWriteResult, BulkWriteUpsert }
import scala.util.{ Try, Success, Failure }

case class BulkWriteResult(underlying: JBulkWriteResult) {
  /**
   * Returns true if the write was acknowledged.
   *
   * @return true if the write was acknowledged
   * @see WriteConcern#Unacknowledged
   */
  def isAcknowledged: Boolean = underlying.isAcknowledged

  /**
   * Returns the number of documents inserted by the write operation.
   *
   * @return the number of documents inserted by the write operation
   *
   * @throws UnacknowledgedWriteException if the write was unacknowledged.
   * @see WriteConcern#Unacknowledged
   */
  def insertedCount: Int = underlying.getInsertedCount

  /**
   * Returns the number of documents matched by updates or replacements in the write operation.  This will include documents that
   * matched the query but where the modification didn't result in any actual change to the document; for example,
   * if you set the value of some field, and the field already has that value, that will still count as an update.
   *
   * @return the number of documents matched by updates in the write operation
   *
   * @throws UnacknowledgedWriteException if the write was unacknowledged.
   * @see WriteConcern#Unacknowledged
   */
  def matchedCount: Int = underlying.getMatchedCount

  /**
   * Returns the number of documents removed by the write operation.
   *
   * @return the number of documents removed by the write operation
   *
   * @throws UnacknowledgedWriteException if the write was unacknowledged.
   * @see WriteConcern#Unacknowledged
   */
  def removedCount: Int = underlying.getRemovedCount

  /**
   * Returns the number of documents modified by updates or replacements in the write operation.  This will only count
   * documents that were actually changed; for example, if you set the value of some field, and the field already has
   * that value, that will not count as a modification.
   *
   * If the server is not able to provide a count of modified documents (which can happen if the server is not at
   * least version 2.6), then this method will return a [[Failure(UnsupportedOperationException)]]
   *
   * @return the [[Success(number)]] of documents modified by the write operation or
   *         [[Failure(UnacknowledgedWriteException)]] or  [[Failure(UnsupportedOperationException)]] if no modified
   *         count is available
   * @see WriteConcern#UNACKNOWLEDGED
   */
  def modifiedCount: Try[Int] = Try(underlying.getModifiedCount)

  /**
   * Gets a list of upserted items, or the empty list if there were none.
   *
   * @return a list of upserted items, or the empty list if there were none.
   *
   * @throws UnacknowledgedWriteException if the write was unacknowledged.
   * @see WriteConcern#Unacknowledged
   */
  def upserts: mutable.Buffer[BulkWriteUpsert] = underlying.getUpserts.asScala

  /* Java api mirrors */
  def getModifiedCount: Try[Int] = modifiedCount

  def getInsertedCount: Int = insertedCount

  def getMatchedCount: Int = matchedCount

  def getRemovedCount: Int = removedCount

  def getUpserts: mutable.Buffer[BulkWriteUpsert] = upserts

  override def equals(obj: Any): Boolean = underlying.equals(obj)

  override def hashCode(): Int = underlying.hashCode()
}
