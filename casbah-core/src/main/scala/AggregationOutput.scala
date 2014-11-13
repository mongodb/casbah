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

import scala.collection.JavaConverters._
import scala.collection.mutable

import com.mongodb.casbah.Imports._
import com.mongodb.{AggregationOutput => JavaAggregationOutput}


/**
 * Wrapper object for AggregationOutput
 *
 * @since 2.5
 */
case class AggregationOutput(underlying: JavaAggregationOutput) {

  /**
   * returns an iterator to the results of the aggregation
   * @return
   */
  def results: Iterable[DBObject] = underlying.results.asScala

  /**
   * returns the command result of the aggregation
   * @return the command result
   * @deprecated there is no replacement for this method
   */
  @deprecated("This will be removed in a future release", "2.8")
  @SuppressWarnings(Array("deprecation"))
  def getCommandResult: mutable.Map[String, AnyRef] = underlying.getCommandResult.asScala

  /**
   * returns the command result of the aggregation
   * @return the command result
   * @deprecated there is no replacement for this method
   */
  @deprecated("This will be removed in a future release", "2.8")
  @SuppressWarnings(Array("deprecation"))
  def commandResult: mutable.Map[String, AnyRef] = underlying.getCommandResult.asScala

  /**
   * returns the original aggregation command
   * @return the command
   * @deprecated there is no replacement for this method
   */
  @deprecated("This will be removed in a future release", "2.8")
  @SuppressWarnings(Array("deprecation"))
  def getCommand: DBObject = underlying.getCommand

  /**
   * returns the original aggregation command
   * @return the command
   * @deprecated there is no replacement for this method
   */
  @deprecated("This will be removed in a future release", "2.8")
  @SuppressWarnings(Array("deprecation"))
  def command: DBObject = underlying.getCommand

  /**
   * returns the address of the server used to execute the aggregation
   * @return the server which executed the aggregation
   * @deprecated there is no replacement for this method
   */
  @deprecated("This will be removed in a future release", "2.8")
  @SuppressWarnings(Array("deprecation"))
  def getServerUsed: ServerAddress = underlying.getServerUsed

  /**
   * returns the address of the server used to execute the aggregation
   * @return the server which executed the aggregation
   * @deprecated there is no replacement for this method
   */
  @deprecated("This will be removed in a future release", "2.8")
  @SuppressWarnings(Array("deprecation"))
  def serverUsed(): ServerAddress = underlying.getServerUsed

  /**
   * string representation of the aggregation command
   */
  override def toString: String = underlying.toString

}
