/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
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
import scala.collection.JavaConverters._

import com.mongodb.{BulkWriteResult => JBulkWriteResult, BulkWriteUpsert}


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
   * Returns the number of documents updated by the write operation.  This will include documents that matched the query but where the
   * modification didn't result in any actual change to the document; for example, if you set the value of some field ,
   * and the field already has that value, that will still count as an update.
   *
   * @return the number of documents updated by the write operation
   *
   * @throws UnacknowledgedWriteException if the write was unacknowledged.
   * @see WriteConcern#Unacknowledged
   */
  def updatedCount: Int = underlying.getUpdatedCount

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
   * Returns the number of documents modified by the write operation.  This only applies to updates or replacements,
   * and will only count documents that were actually changed; for example, if you set the value of some field ,
   * and the field already has that value, that will not count as a modification.
   *
   * @return the number of documents modified by the write operation
   *
   * @throws UnacknowledgedWriteException if the write was unacknowledged.
   * @see WriteConcern#Unacknowledged
   */
  def modifiedCount: Int = underlying.getModifiedCount

  /**
   * Gets a list of upserted items, or the empty list if there were none.
   *
   * @return a list of upserted items, or the empty list if there were none.
   *
   * @throws UnacknowledgedWriteException if the write was unacknowledged.
   * @see WriteConcern#Unacknowledged
   */
  def upserts: mutable.Buffer[BulkWriteUpsert] =  underlying.getUpserts.asScala

  /* Java api mirrors */
  def getModifiedCount: Int = modifiedCount

  def getInsertedCount: Int = insertedCount

  def getUpdatedCount: Int = updatedCount

  def getRemovedCount: Int = removedCount

  def getUpserts: mutable.Buffer[BulkWriteUpsert] = upserts

}
