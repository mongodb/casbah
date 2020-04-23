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

import com.mongodb.casbah.Imports._
import com.mongodb.{ AggregationOutput => JavaAggregationOutput }

import scala.jdk.CollectionConverters._

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
   * string representation of the aggregation command
   */
  override def toString: String = underlying.toString

}
