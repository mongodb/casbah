/**
 * Copyright (c) 2010 - 2012 10gen, Inc. <http://10gen.com>
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

import com.mongodb.casbah.util.Logging

import scalaj.collection.Imports._

import com.mongodb.casbah.query._

import scala.util.matching._
import scala.collection.Iterable

import org.bson._
import org.bson.types.BasicBSONList

object AggregationFramework {}

/**
 * Base trait for a Pipeline Operator for 
 * the Aggregation Framework.
 * These operators are the "core" of Aggregation,
 * representing the primary pipeline.
 */
trait PipelineOperator {

  //protected def op(oper: String, target: Any): Map[String, Any] 
}

/** 
 * Base trait for expressions in the Pipeline 
 */
trait PipelineExpression {
}


// TODO - Validations of things like "ran group after sort" for certain opers
trait PipelineOperations extends GroupOperator
  with LimitOperator
  with SkipOperator 
  with MatchOperator
  with ProjectOperator
  with SortOperator
  with UnwindOperator

trait LimitOperator extends PipelineOperator {
  private val operator = "$limit"

  // TODO - Accept Numeric? As long as we can downconvert for mongo type?
  def $limit(target: Int) = {
    val obj = new BasicDBObject with PipelineOperations
    obj put (operator, target)
    obj
  }

  def $limit(target: BigInt) = {
    val obj = new BasicDBObject with PipelineOperations
    obj put (operator, target)
    obj
  }

}

trait SkipOperator extends PipelineOperator {
  private val operator = "$skip"

  // TODO - Accept Numeric? As long as we can downconvert for mongo type?
  def $skip(target: Int) = {
    val obj = new BasicDBObject with PipelineOperations
    obj put (operator, target)
    obj
  }
  def $skip(target: BigInt) = {
    val obj = new BasicDBObject with PipelineOperations
    obj put (operator, target)
    obj
  } 

}

trait MatchOperator extends PipelineOperator {
  private val operator = "$match"

  def $match(query: DBObject) = 
    MongoDBObject(operator -> query)
}

trait SortOperator extends PipelineOperator {
  private val operator = "$sort"

  def $sort(fields: (String, Int)*) = {
     val bldr = MongoDBObject.newBuilder
     for ((k, v) <- fields) bldr += k -> v
     MongoDBObject(operator -> bldr.result)
  }
}

trait UnwindOperator extends PipelineOperator {
  private val operator = "$unwind"

  def $unwind(target: String) = {
    require(target.startsWith("$"), "The $unwind operator only accepts a $<fieldName> argument; bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_unwind")
    MongoDBObject("$unwind" -> target)
  }
}

trait ProjectOperator extends PipelineOperator {
  private val operator = "$project"
}


trait GroupOperatorBase extends PipelineOperator {

  /** composable with suboperators */
  protected def _group(field: String) = {
    new {
      protected def op(target: Any) = 
        MongoDBObject("$group" -> MongoDBObject(field -> target))

      /** 
       * Returns an array of all the values found in the selected field among 
       * the documents in that group. Every unique value only appears once 
       * in the result set.
       *
       * RValue should be $&lt;documentFieldName&gt;
       */
      def $addToSet(target: String) = {
        require(target.startsWith("$"), "The $group.$addToSet operator only accepts a $<fieldName> argument; bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
        op(MongoDBObject("$addToSet" -> target))   
      }

      /** 
       * Returns the first value it sees for its group.
       *
       * Note Only use $first when the $group follows an $sort operation. 
       * Otherwise, the result of this operation is unpredictable.
       *
       * RValue should be $&lt;documentFieldName&gt;
       */
      def $first(target: String) = {
        require(target.startsWith("$"), "The $group.$first operator only accepts a $<fieldName> argument; bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
        op(MongoDBObject("$first" -> target))
      }

      /** 
       * Returns the last value it sees for its group.
       *
       * Note Only use $last when the $group follows an $sort operation. 
       * Otherwise, the result of this operation is unpredictable.
       *
       * RValue should be $&lt;documentFieldName&gt;
       */
      def $last(target: String) = {
        require(target.startsWith("$"), "The $group.$last operator only accepts a $<fieldName> argument; bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
        op(MongoDBObject("$last" -> target))
      }

      /** 
       * Returns the highest value among all values of the field in all documents selected by this group.
       *
       * RValue should be $&lt;documentFieldName&gt;
       */
      def $max(target: String) = {
        require(target.startsWith("$"), "The $group.$max operator only accepts a $<fieldName> argument; bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
        op(MongoDBObject("$max" -> target))
      }

      /** 
       * Returns the lowest value among all values of the field in all documents selected by this group.
       *
       * RValue should be $&lt;documentFieldName&gt;
       */
      def $min(target: String) = {
        require(target.startsWith("$"), "The $group.$min operator only accepts a $<fieldName> argument; bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
        op(MongoDBObject("$min" -> target))
      }

      /** 
       * Returns the average of all values of the field in all documents selected by this group.
       *
       * RValue should be $&lt;documentFieldName&gt;
       */
      def $avg(target: String) = {
        require(target.startsWith("$"), "The $group.$avg operator only accepts a $<fieldName> argument; bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
        op(MongoDBObject("$avg" -> target))
      }

      /** 
       * Returns an array of all the values found in the selected field among 
       * the documents in that group. A value may appear more than once in the 
       * result set if more than one field in the grouped documents has that value.
       *
       * RValue should be $&lt;documentFieldName&gt;
       */
      def $push(target: String) = {
        require(target.startsWith("$"), "The $group.$push operator only accepts a $<fieldName> argument; bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
        op(MongoDBObject("$push" -> target))
      }

      /** 
       * Returns the sum of all the values for a specified field in the 
       * grouped documents, as in the second use above.
       * 
       * The standard usage is to indicate "1" as the value, which counts all the 
       * members in the group.
       *
       * Alternately, if you specify a field value as an argument, $sum will 
       * increment this field by the specified value for every document in the 
       * grouping. 
       *
       */
      def $sum(target: String) = {
        require(target.startsWith("$"), "The $group.$sum operator only accepts a $<fieldName> argument (or '1'); bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
        op(MongoDBObject("$sum" -> target))
      }

      /** 
       * Returns the sum of all the values for a specified field in the 
       * grouped documents, as in the second use above.
       * 
       * The standard usage is to indicate "1" as the value, which counts all the 
       * members in the group.
       *
       * Alternately, if you specify a field value as an argument, $sum will 
       * increment this field by the specified value for every document in the 
       * grouping. 
       *
       */
      def $sum(target: Int) = {
        require(target == 1, "The $group.$sum operator only accepts a numeric argument of '1', or a $<FieldName>. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
        op(MongoDBObject("$sum" -> target))
      }

    }
  }
}

// TODO

// *REQUIRES* an _id field to be defined...
// "	for _id, it can be a single field reference, a document containing several field references, or a constant of any type."
trait GroupOperator extends GroupOperatorBase {
  private val operator = "$group"

  def $group(field: String) = _group(field)
}

