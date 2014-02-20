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

import com.mongodb.{AggregationOptions => JAggregationOptions}
import scala.concurrent.duration.Duration

/**
 * Helper object for `com.mongodb.AggregationOptions`
 *
 * @since 2.7
 */
object AggregationOptions {

  val INLINE = JAggregationOptions.OutputMode.INLINE
  val CURSOR = JAggregationOptions.OutputMode.CURSOR

  val default = JAggregationOptions.builder().build()

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  All objects that match will
   *                  be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or
   *                  the # that fit in a batch
   */
  def apply(batchSize: Int): JAggregationOptions = JAggregationOptions.builder().batchSize(batchSize).build()

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param allowDiskUse if true enables external sorting that can write data to the _tmp sub-directory in the
   *                       dbpath directory
   */
  def apply(allowDiskUse: Boolean): JAggregationOptions =
    JAggregationOptions.builder().allowDiskUse(allowDiskUse).build()

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param outputMode either OutputMode.INLINE or OutputMode.CURSOR
   */
  def apply(outputMode: JAggregationOptions.OutputMode): JAggregationOptions =
    JAggregationOptions.builder().outputMode(outputMode).build()

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param maxTime the maximum duration that the server will allow this operation to execute before killing it
   */
  def apply(maxTime: Duration): JAggregationOptions =
    JAggregationOptions.builder().maxTime(maxTime.length, maxTime.unit).build()

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  All objects that match will
   *                  be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or
   *                  the # that fit in a batch
   * @param allowDiskUse if true enables external sorting that can write data to the _tmp sub-directory in the
   *                       dbpath directory
   */
  def apply(batchSize: Int, allowDiskUse: Boolean): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.batchSize(batchSize)
    builder.allowDiskUse(allowDiskUse)
    builder.build()
  }

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  All objects that match will
   *                  be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or
   *                  the # that fit in a batch
   * @param outputMode either OutputMode.INLINE or OutputMode.CURSOR
   */
  def apply(batchSize: Int, outputMode: JAggregationOptions.OutputMode): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.batchSize(batchSize)
    builder.outputMode(outputMode)
    builder.build()
  }

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param allowDiskUse if true enables external sorting that can write data to the _tmp sub-directory in the
   *                       dbpath directory
   * @param outputMode either OutputMode.INLINE or OutputMode.CURSOR
   */
  def apply(allowDiskUse: Boolean, outputMode: JAggregationOptions.OutputMode): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.allowDiskUse(allowDiskUse)
    builder.outputMode(outputMode)
    builder.build()
  }

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  All objects that match will
   *                  be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or
   *                  the # that fit in a batch
   * @param maxTime the maximum duration that the server will allow this operation to execute before killing it
   */
  def apply(batchSize: Int, maxTime: Duration): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.batchSize(batchSize)
    builder.maxTime(maxTime.length, maxTime.unit)
    builder.build()
  }

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param allowDiskUse if true enables external sorting that can write data to the _tmp sub-directory in the
   *                       dbpath directory
   * @param maxTime    the maximum duration that the server will allow this operation to execute before killing it
   */
  def apply(allowDiskUse: Boolean, maxTime: Duration): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.allowDiskUse(allowDiskUse)
    builder.maxTime(maxTime.length, maxTime.unit)
    builder.build()
  }

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param outputMode  either OutputMode.INLINE or OutputMode.CURSOR
   * @param maxTime the maximum duration that the server will allow this operation to execute before killing it
   */
  def apply(outputMode: JAggregationOptions.OutputMode, maxTime: Duration): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.outputMode(outputMode)
    builder.maxTime(maxTime.length, maxTime.unit)
    builder.build()
  }


  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  All objects that match will
   *                  be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or
   *                  the # that fit in a batch
   * @param allowDiskUse if true enables external sorting that can write data to the _tmp sub-directory in the
   *                       dbpath directory
   * @param outputMode either OutputMode.INLINE or OutputMode.CURSOR
   */
  def apply(batchSize: Int, allowDiskUse: Boolean, outputMode: JAggregationOptions.OutputMode): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.batchSize(batchSize)
    builder.allowDiskUse(allowDiskUse)
    builder.outputMode(outputMode)
    builder.build()
  }

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  All objects that match will
   *                  be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or
   *                  the # that fit in a batch
   * @param allowDiskUse if true enables external sorting that can write data to the _tmp sub-directory in the
   *                       dbpath directory
   * @param maxTime    the maximum duration that the server will allow this operation to execute before killing it
   */
  def apply(batchSize: Int, allowDiskUse: Boolean, maxTime: Duration): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.batchSize(batchSize)
    builder.allowDiskUse(allowDiskUse)
    builder.maxTime(maxTime.length, maxTime.unit)
    builder.build()
  }

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param allowDiskUse if true enables external sorting that can write data to the _tmp sub-directory in the
   *                       dbpath directory
   * @param outputMode     either OutputMode.INLINE or OutputMode.CURSOR
   * @param maxTime    the maximum duration that the server will allow this operation to execute before killing it
   */
  def apply(allowDiskUse: Boolean, outputMode: JAggregationOptions.OutputMode, maxTime: Duration): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.allowDiskUse(allowDiskUse)
    builder.outputMode(outputMode)
    builder.maxTime(maxTime.length, maxTime.unit)
    builder.build()
  }

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  All objects that match will
   *                  be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or
   *                  the # that fit in a batch
   * @param outputMode  either OutputMode.INLINE or OutputMode.CURSOR
   * @param maxTime the maximum duration that the server will allow this operation to execute before killing it
   */
  def apply(batchSize: Int, outputMode: JAggregationOptions.OutputMode, maxTime: Duration): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.batchSize(batchSize)
    builder.outputMode(outputMode)
    builder.maxTime(maxTime.length, maxTime.unit)
    builder.build()
  }

  /**
   * Instantiate a new AggregationOptions instance
   *
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  All objects that match will
   *                  be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or
   *                  the # that fit in a batch
   * @param allowDiskUse  if true enables external sorting that can write data to the _tmp sub-directory in the
   *                        dbpath directory
   * @param outputMode      either OutputMode.INLINE or OutputMode.CURSOR
   * @param maxTime     the maximum duration that the server will allow this operation to execute before killing it
   */
  def apply(batchSize: Int, allowDiskUse: Boolean, outputMode: JAggregationOptions.OutputMode,
            maxTime: Duration): JAggregationOptions = {
    val builder = JAggregationOptions.builder()
    builder.batchSize(batchSize)
    builder.allowDiskUse(allowDiskUse)
    builder.outputMode(outputMode)
    builder.maxTime(maxTime.length, maxTime.unit)
    builder.build()
  }
}
