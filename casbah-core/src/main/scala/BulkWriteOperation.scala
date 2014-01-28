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

import com.mongodb.{BulkWriteOperation => JBulkWriteOperation, BulkWriteRequestBuilder}

import com.mongodb.casbah.Imports._

/**
 * A builder for a bulk write operation.
 *
 * @since 2.7
 */
case class BulkWriteOperation(underlying: JBulkWriteOperation) {

  /**
   * Returns true if this is building an ordered bulk write request.
   *
   * @return whether this is building an ordered bulk write operation
   *
   * @see MongoCollection#initializeOrderedBulkOperation()
   * @see MongoCollection#initializeUnorderedBulkOperation()
   */
  def isOrdered: Boolean = underlying.isOrdered

  /**
   * Add an insert request to the bulk operation
   *
   * @param document the document to insert
   */
  def insert(document: DBObject): Unit = underlying.insert(document)

  /**
   * Start building a write request to add to the bulk write operation.
   *
   * @param query the query for an update, replace or remove request
   * @return a builder for a single write request
   */
  def find(query: DBObject): BulkWriteRequestBuilder = underlying.find(query)

  /**
   * Execute the bulk write operation.
   *
   * @return the result of the bulk write operation.
   * @throws com.mongodb.BulkWriteException if there was an error with the bulk write
   * @throws com.mongodb.MongoException if there was a mongodb error
   */
  def execute(): BulkWriteResult = BulkWriteResult(underlying.execute())

  /**
   * Execute the bulk write operation with the given write concern.
   *
   * @param writeConcern the write concern to apply to the bulk operation
   *                     runReplicaSetStatusCommand
   * @return the result of the bulk write operation.
   * @throws com.mongodb.BulkWriteException if there was an error with the bulk write
   * @throws com.mongodb.MongoException if there was a mongodb error
   */
  def execute(writeConcern: WriteConcern): BulkWriteResult = BulkWriteResult(underlying.execute(writeConcern))
}
