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
package aggregation


import com.mongodb.casbah.query.Imports._

import com.mongodb.casbah.commons.Logging

trait GroupSubOperators extends GroupSumOperator
  with GroupPushOperator
  with GroupAvgOperator
  with GroupMinOperator
  with GroupMaxOperator
  with GroupFirstOperator
  with GroupLastOperator
  with GroupAddToSetOperator

/**
 * Base trait for implementation of $group
 * @author brendan
 *
 */
trait GroupOperator extends PipelineOperator {
  private val operator = "$group"

    // TODO - Require GroupSubExpressionObject
  def $group(target: DBObject) = { 
    require(target contains "_id", "Aggregation $group statements must contain an _id field representing " +
    		"the 'GROUP BY' key. Please see the aggregation docs at " +
    		"http://docs.mongodb.org/manual/reference/aggregation/group/#_S_group")
    op(operator, target)
  }
}

trait GroupSubExpressionObject {
  self: DBObject =>
  def field: String
  
}

object GroupSubExpressionObject {

  def apply[A <: String, B <: Any](kv: (A, B)): DBObject with GroupSubExpressionObject = {
    val obj = new BasicDBObject with GroupSubExpressionObject { val field = kv._1 }
    obj.put(kv._1, kv._2)
    obj
  }

}

trait GroupSubOperator extends Logging {
  def field: String
  protected var dbObj: Option[DBObject] = None

  protected def op(oper: String, target: Any): DBObject with GroupSubExpressionObject = GroupSubExpressionObject(dbObj match {
    case Some(nested) => {
      nested.put(oper, target)
      (field -> nested)
    }
    case None => {
      val opMap = MongoDBObject(oper -> target)
      (field -> opMap)
    }
  })
} 

/** 
 * Returns an array of all the values found in the selected field among 
 * the documents in that group. Every unique value only appears once 
 * in the result set.
 *
 * RValue should be $&lt;documentFieldName&gt;
 */
trait GroupAddToSetOperator extends GroupSubOperator {
  
  def $addToSet(target: String) = {
    require(target.startsWith("$"), "The $group.$addToSet operator only accepts a $<fieldName> argument; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
    op("$addToSet", target)
  }
  
}

/** 
 * Returns the first value it sees for its group.
 *
 * Note Only use $first when the $group follows an $sort operation. 
 * Otherwise, the result of this operation is unpredictable.
 *
 * RValue should be $&lt;documentFieldName&gt;
 */
trait GroupFirstOperator extends GroupSubOperator {
  
  def $first(target: String) = {
    require(target.startsWith("$"), "The $group.$first operator only accepts a $<fieldName> argument; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
    op("$first", target)
  }
}

/** 
 * Returns the last value it sees for its group.
 *
 * Note Only use $last when the $group follows an $sort operation. 
 * Otherwise, the result of this operation is unpredictable.
 *
 * RValue should be $&lt;documentFieldName&gt;
 */
trait GroupLastOperator extends GroupSubOperator {
  
  def $last(target: String) = {
    require(target.startsWith("$"), "The $group.$last operator only accepts a $<fieldName> argument; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
    op("$last", target)
  }
}

/** 
 * Returns the highest value among all values of the field in all documents selected by this group.
 *
 * RValue should be $&lt;documentFieldName&gt;
 */
trait GroupMaxOperator extends GroupSubOperator {
  
  def $max(target: String) = {
    require(target.startsWith("$"), "The $group.$max operator only accepts a $<fieldName> argument; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
    op("$max", target)
  }
}

/** 
 * Returns the lowest value among all values of the field in all documents selected by this group.
 *
 * RValue should be $&lt;documentFieldName&gt;
 */
trait GroupMinOperator extends GroupSubOperator {
  
  def $min(target: String) = {
    require(target.startsWith("$"), "The $group.$min operator only accepts a $<fieldName> argument; bare field names will not function. See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
    op("$min", target)
  }
}

/** 
 * Returns the average of all values of the field in all documents selected by this group.
 *
 * RValue should be $&lt;documentFieldName&gt;
 */
trait GroupAvgOperator extends GroupSubOperator {
  def $avg(target: String) = {
    require(target.startsWith("$"), "The $group.$avg operator only accepts a $<fieldName> argument; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
    op("$avg", target)
  }
}

/** 
 * Returns an array of all the values found in the selected field among 
 * the documents in that group. A value may appear more than once in the 
 * result set if more than one field in the grouped documents has that value.
 *
 * RValue should be $&lt;documentFieldName&gt;
 */
trait GroupPushOperator extends GroupSubOperator {
  def $push(target: String) = {
    require(target.startsWith("$"), "The $group.$push operator only accepts a $<fieldName> argument; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
    op("$push", target)
  }
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
trait GroupSumOperator extends GroupSubOperator {
  def $sum(target: String) = {
    require(target.startsWith("$"), "The $group.$sum operator only accepts a $<fieldName> argument (or '1'); bare field names will not function." +
    		" See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
    op("$sum", target)
  }
  
  def $sum(target: Int) = {
    require(target == 1, "The $group.$sum operator only accepts a numeric argument of '1', or a $<FieldName>. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_group")
    op("$sum", target)
  }
}
