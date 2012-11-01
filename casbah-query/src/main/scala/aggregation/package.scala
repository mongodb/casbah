/**
 * Copyright (c) 2010 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 *     http://github.com/mongodb/casbah
 *
 */

package com.mongodb.casbah.query.dsl

import com.mongodb.casbah.commons.Logging

import scalaj.collection.Imports._

import com.mongodb.casbah.query.Imports._

import scala.util.matching._
import scala.collection.Iterable
import scala.collection.mutable.{ Seq => MutableSeq }

import org.bson._
import org.bson.types.BasicBSONList

/**
 * Base traits and configuration for aggregation framework.
 *
 * @author brendan
 *
 */
package object aggregation {

  /**
   * Base trait for a Pipeline Operator for
   * the Aggregation Framework.
   * These operators are the "core" of Aggregation,
   * representing the primary pipeline.
   */
  trait PipelineOperator {
    def list: MongoDBList

    protected def op(oper: String, target: Any) =
      PipelineOperator(oper, target)(list)

    override def toString = list.toString

  }

  object PipelineOperator {

    // TODO - this should be a LIST, not a DBObject.
    def apply[A <: String, B <: Any](kv: (A, B))(pipeline: MongoDBList): DBObject with PipelineOperations with PipelineOperator = {
      val obj = new BasicDBObject with PipelineOperations with PipelineOperator { val list = pipeline }
      obj.put(kv._1, kv._2)
      pipeline += obj
      obj
    }
  }

  // TODO - Validations of things like "ran group after sort" for certain opers
  trait PipelineOperations extends GroupOperator
    with LimitOperator
    with SkipOperator
    with MatchOperator
    with ProjectOperator
    with SortOperator
    with UnwindOperator

  trait BasePipelineOperations extends PipelineOperations { val list = MongoDBList.empty }

  trait GroupSubOperators extends GroupSumOperator
    with GroupPushOperator
    with GroupAvgOperator
    with GroupMinOperator
    with GroupMaxOperator
    with GroupFirstOperator
    with GroupLastOperator
    with GroupAddToSetOperator

}