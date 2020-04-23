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

import com.mongodb.{ BulkWriteError, BulkWriteException => JBulkWriteException, ServerAddress, WriteConcernError }

case class BulkWriteException(message: String, cause: Throwable, underlying: JBulkWriteException) extends Exception(message: String, cause: Throwable) {

  /**
   * The result of all successfully processed write operations.  This will never be null.
   *
   * @return the bulk write result
   */
  def writeResult: BulkWriteResult = BulkWriteResult(underlying.getWriteResult)

  /**
   * The list of errors, which will not be null, but may be empty (if the write concern error is not null).
   *
   * @return the list of errors
   */
  def writeErrors: mutable.Buffer[BulkWriteError] = underlying.getWriteErrors.asScala

  /**
   * The write concern error, which may be null (in which case the list of errors will not be empty).
   *
   * @return the write concern error
   */
  def writeConcernError: WriteConcernError = underlying.getWriteConcernError

  /**
   * The address of the server which performed the bulk write operation.
   *
   * @return the address
   */
  def serverAddress: ServerAddress = underlying.getServerAddress

  /* Java api mirrors */
  def getWriteResult: BulkWriteResult = writeResult

  def getWriteErrors: mutable.Buffer[BulkWriteError] = writeErrors

  def getWriteConcernError: WriteConcernError = writeConcernError

  def getServerAddress: ServerAddress = serverAddress

  override def equals(obj: Any): Boolean = underlying.equals(obj)

  override def hashCode(): Int = underlying.hashCode()
}
