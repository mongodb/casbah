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
import com.mongodb.casbah.query.ChainedOperator

/**
 * Base trait to implement $project
 *
 */
trait ProjectOperator extends PipelineOperator {
  private val operator = "$project"

  // TODO - Validate only valid field entries?
  def $project(target: DBObject) = op(operator, target)

  def $project(fields: (String, Any)*) = {
     val bldr = MongoDBObject.newBuilder
     for ((k, v) <- fields) bldr += k -> v
     op(operator, bldr.result)
  }
}

trait ProjectSubOperators extends ProjectAndBooleanOperator
  with ProjectOrBooleanOperator
  with ProjectNotBooleanOperator
  with ProjectCmpComparisonOperator
  with ProjectEqComparisonOperator
  with ProjectGtComparisonOperator
  with ProjectGteComparisonOperator
  with ProjectLtComparisonOperator
  with ProjectLteComparisonOperator
  with ProjectNeComparisonOperator
  with ProjectAddArithmeticOperator
  with ProjectDivideArithmeticOperator
  with ProjectMultiplyArithmeticOperator
  with ProjectSubtractArithmeticOperator
  with ProjectCaseInsensitiveCompareStringOperator
  with ProjectSubstringStringOperator
  with ProjectToUpperStringOperator
  with ProjectToLowerStringOperator
  with ProjectDayOfYearDateOperator
  with ProjectDayOfWeekDateOperator
  with ProjectDayOfMonthDateOperator
  with ProjectMonthDateOperator
  with ProjectWeekDateOperator
  with ProjectHourDateOperator
  with ProjectMinuteDateOperator
  with ProjectSecondDateOperator
  with ProjectCondConditionalOperator
  with ProjectIfNullConditionalOperator


trait ProjectSubExpressionObject {
  self: DBObject =>
  def field: String
}

object ProjectSubExpressionObject {

  def apply[A <: String, B <: Any](kv: (A, B)): DBObject with ProjectSubExpressionObject = {
    val obj = new BasicDBObject with ProjectSubExpressionObject { val field = kv._1 }
    obj.put(kv._1, kv._2)
    obj
  }

}

trait ProjectSubOperator extends ChainedOperator {

  protected def projectionOp(oper: String, target: Any): DBObject with ProjectSubExpressionObject = ProjectSubExpressionObject(dbObj match {
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

/**** BOOLEAN OPERATORS */

/**
 * $and
 *
 * BOOLEAN AND
 *
 * Takes an array of one or more values, returning true/false
 * using short circuiting logic.
 *
 */
trait ProjectAndBooleanOperator extends ProjectSubOperator {

  def $and[A : ValidBarewordExpressionArgType](fields: A*): DBObject = {
    val b = Seq.newBuilder[DBObject]
    fields.foreach(x => b += implicitly[ValidBarewordExpressionArgType[A]].toDBObject(x))
    $and(b.result(): Seq[DBObject])
  }

  def $and(list: Seq[DBObject]): DBObject = projectionOp("$and", list)
}

/**
 * $or
 *
 * BOOLEAN OR
 *
 * Takes an array of one or more values, returning true/false
 * using short circuiting logic
 */
trait ProjectOrBooleanOperator extends ProjectSubOperator {
  def $or[A : ValidBarewordExpressionArgType](fields: A*): DBObject = {
    val b = Seq.newBuilder[DBObject]
    fields.foreach(x => b += implicitly[ValidBarewordExpressionArgType[A]].toDBObject(x))
    $or(b.result(): Seq[DBObject])
  }

  def $or(list: Seq[DBObject]): DBObject = projectionOp("$or", list)
}

/**
 *
 * $not
 *
 * BOOLEAN NOT
 *
 * Takes a single argument, either a Boolean or a field containing a boolean,
 * and returns the boolean opposite
 */
trait ProjectNotBooleanOperator extends ProjectSubOperator {

  def $not(target: String) = {
    require(target.startsWith("$"), "The $project.$not operator only accepts a $<fieldName> or boolean argument; bare field names will not function. ")
    projectionOp("$not", target)
  }

  def $not(target: Boolean) = projectionOp("$not", target)

}

/**** COMPARISON OPERATORS
 * These opers take an array w/ a pair of values.
 * All but $cmp return a boolean
 */

/**
 * $cmp
 *
 * Compare two values, returning an integer
 *
 * NEGATIVE - First Value < Second Value
 * POSITIVE - First Value > Second Value
 *        0 - Both Values Equal
 *
 *  ex:
 *     "foo" $cmp ("$name1", "$name2")
 *
 *  remember for sub-fields to use $, we can't cleanly validate here
 */
trait ProjectCmpComparisonOperator extends ProjectSubOperator {
  def $cmp(first: Any, second: Any) = projectionOp("$cmp", MongoDBList(first, second))
}

/**
 * $eq
 *
 * Does first value equal second value?
 *
 * ret. boolean
 */
trait ProjectEqComparisonOperator extends ProjectSubOperator {
  def $eq(first: Any, second: Any) = projectionOp("$eq", MongoDBList(first, second))
}

/**
 * $gt
 *
 * Is first value greater than second value?
 *
 * ret. boolean
 */
trait ProjectGtComparisonOperator extends ProjectSubOperator {
  def $gt(first: Any, second: Any) = projectionOp("$gt", MongoDBList(first, second))
}

/**
 * $gte
 *
 * Is first value greater than or equal to the second value?
 *
 * ret. boolean
 */
trait ProjectGteComparisonOperator extends ProjectSubOperator {
  def $gte(first: Any, second: Any) = projectionOp("$gte", MongoDBList(first, second))
}

/**
 * $lt
 *
 * Is first value less than second value?
 *
 * ret. boolean
 */
trait ProjectLtComparisonOperator extends ProjectSubOperator {
  def $lt(first: Any, second: Any) = projectionOp("$lt", MongoDBList(first, second))
}

/**
 * $lte
 *
 * Is first value less than or equal to the second value?
 *
 * ret. boolean
 */
trait ProjectLteComparisonOperator extends ProjectSubOperator {
  def $lte(first: Any, second: Any) = projectionOp("$lte", MongoDBList(first, second))
}

/**
 * $ne
 *
 * Is first value not equal to the second value?
 *
 * ret. boolean
 */
trait ProjectNeComparisonOperator extends ProjectSubOperator {
  def $ne(first: Any, second: Any) = projectionOp("$ne", MongoDBList(first, second))
}

/****** ARITHMETIC OPERATORS */

/**
 * $add
 *
 * Adds an array of one or more numbers together, returning the resulting sum.
 *
 * Could be #s or field refs.
 *
 * TODO - Typeclass me!
 */
trait ProjectAddArithmeticOperator extends ProjectSubOperator {
  def $add[A : ValidBarewordExpressionArgType](fields: A*): DBObject = {
    val b = Seq.newBuilder[DBObject]
    fields.foreach(x => b += implicitly[ValidBarewordExpressionArgType[A]].toDBObject(x))
    $add(b.result(): Seq[DBObject])
  }

  def $add(list: Seq[DBObject]): DBObject = projectionOp("$add", list)
}

/**
 * $multiply
 *
 * Multiply an array of one or more numbers together, returning the result.
 *
 * Could be #s or field refs.
 *
 * TODO - Typeclass me!
 */
trait ProjectMultiplyArithmeticOperator extends ProjectSubOperator {
  def $multiply[A : ValidBarewordExpressionArgType](fields: A*): DBObject = {
    val b = Seq.newBuilder[DBObject]
    fields.foreach(x => b += implicitly[ValidBarewordExpressionArgType[A]].toDBObject(x))
    $multiply(b.result(): Seq[DBObject])
  }

  def $multiply(list: Seq[DBObject]): DBObject = projectionOp("$multiply", list)
}

/** NOTE $mod already fully implemented in query DSL; exact same syntax/behavior */

/**
 * $divide
 *
 * Takes a PAIR of numbers, divides 1/2, returning result
 * Can be field ref or #
 */
trait ProjectDivideArithmeticOperator extends ProjectSubOperator {
  def $divide(numerator: Any, denominator: Any) = projectionOp("$divide", MongoDBList(numerator, denominator))
}

/**
 * $subtract
 *
 * takes a PAIR of numbers, subtracts 2 from 1, returns difference
 *
 * Can be a field ref or #
 * TODO - Typeclass me
 */
trait ProjectSubtractArithmeticOperator extends ProjectSubOperator {
  def $subtract(first: Any, second: Any) = projectionOp("$subtract", MongoDBList(first, second))
}

/***** STRING OPERATORS
 *  note that most of these string operators have serious bugs with non-roman glyphs.
 */

/**
 * $strcasecmp
 *
 * Takes a PAIR of strings (either a Field ref or an actual string)
 * and compares them in a case-insensitive fashion.
 * RETURN
 *    -# if first > second
 *    +# if first < second
 *     0 if both are identical
 */
trait ProjectCaseInsensitiveCompareStringOperator extends ProjectSubOperator {
  def $strcasecmp(first: String, second: String) = projectionOp("$strcasecmp", MongoDBList(first, second))
}

/**
 * $substr
 *
 * Extracts a substring from a string.
 * (String, Int, Int) -
 *   String - A string or field ref to extract from
 *   Int1 - # of bytes to skip
 *   Int2 - # of bytes to return
 */
trait ProjectSubstringStringOperator extends ProjectSubOperator {
  def $substr(target: String, skip: Int, ret: Int) = projectionOp("$substr", MongoDBList(target, skip, ret))
}

/**
 * $toLower
 *
 * Convert a string to lowercase, returning the result
 *
 * Target: A string or a field ref
 */
trait ProjectToLowerStringOperator extends ProjectSubOperator {
  def $toLower(target: String) = projectionOp("$toLower", target)
}

/**
 * $toUpper
 *
 * Convert a string to uppercase, returning the result
 *
 * Target: A string or a field ref
 */
trait ProjectToUpperStringOperator extends ProjectSubOperator {
  def $toUpper(target: String) = projectionOp("$toUpper", target)
}

/*** DATE OPERATIONS
 * Take a date field or a raw date
 */

/**
 * $dayOfYear
 *
 * Extracts the day of year from a BSON date
 */
trait ProjectDayOfYearDateOperator extends ProjectSubOperator {
  def $dayOfYear(target: String) = {
    require(target.startsWith("$"), "The $project.$dayOfYear operator only accepts a $<fieldName> argument or a raw date; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_dayOfYear")
    projectionOp("$dayOfYear", target)
  }

  def $dayOfYear[T: ValidDateType](target: T) = projectionOp("$dayOfYear", target)
}

/**
 * $dayOfMonth
 *
 * Extracts the day of month from a BSON date
 */
trait ProjectDayOfMonthDateOperator extends ProjectSubOperator {
  def $dayOfMonth(target: String) = {
    require(target.startsWith("$"), "The $project.$dayOfMonth operator only accepts a $<fieldName> argument or a raw date; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_dayOfMonth")
    projectionOp("$dayOfMonth", target)
  }

  def $dayOfMonth[T: ValidDateType](target: T) = projectionOp("$dayOfMonth", target)

}

/**
 * $dayOfWeek
 *
 * Extracts the day of Week from a BSON date
 */
trait ProjectDayOfWeekDateOperator extends ProjectSubOperator {
  def $dayOfWeek(target: String) = {
    require(target.startsWith("$"), "The $project.$dayOfWeek operator only accepts a $<fieldName> argument or a raw date; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_dayOfWeek")
    projectionOp("$dayOfWeek", target)
  }

  def $dayOfWeek[T: ValidDateType](target: T) = projectionOp("$dayOfWeek", target)
}

/**
 * $year
 *
 * Extracts the full year from a BSON date
 */
trait ProjectYearDateOperator extends ProjectSubOperator {
  def $year(target: String) = {
    require(target.startsWith("$"), "The $project.$year operator only accepts a $<fieldName> argument or a raw date; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_year")
    projectionOp("$year", target)
  }

  def $year[T: ValidDateType](target: T) = projectionOp("$year", target)
}

/**
 * $month
 *
 * Extracts the month (1-12) from a BSON date
 */
trait ProjectMonthDateOperator extends ProjectSubOperator {
  def $month(target: String) = {
    require(target.startsWith("$"), "The $project.$month operator only accepts a $<fieldName> argument or a raw date; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_month")
    projectionOp("$month", target)
  }

  def $month[T: ValidDateType](target: T) = projectionOp("$month", target)
}

/**
 * $week
 *
 * Extracts the day of Week from a BSON date
 *  value: 0-53, if a day falls before the first sunday of year, in week 0
 *  Same value as strftime("%U") yields in unix stdlib
 */
trait ProjectWeekDateOperator extends ProjectSubOperator {
  def $week(target: String) = {
    require(target.startsWith("$"), "The $project.$week operator only accepts a $<fieldName> argument or a raw date; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_week")
    projectionOp("$week", target)
  }

  def $week[T: ValidDateType](target: T) = projectionOp("$week", target)
}

/**
 * $hour
 *
 * Extracts the hour (0-23) from a BSON date
 */
trait ProjectHourDateOperator extends ProjectSubOperator {
  def $hour(target: String) = {
    require(target.startsWith("$"), "The $project.$hour operator only accepts a $<fieldName> argument or a raw date; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_hour")
    projectionOp("$hour", target)
  }

  def $hour[T: ValidDateType](target: T) = projectionOp("$hour", target)
}

/**
 * $minute
 *
 * Extracts the minute (0-59) from a BSON date
 */
trait ProjectMinuteDateOperator extends ProjectSubOperator {
  def $minute(target: String) = {
    require(target.startsWith("$"), "The $project.$minute operator only accepts a $<fieldName> argument or a raw date; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_minute")
    projectionOp("$minute", target)
  }

  def $minute[T: ValidDateType](target: T) = projectionOp("$minute", target)
}

/**
 * $second
 *
 * Extracts the second  from a BSON date
 *  value: 0-59 *but*, can be 60 in the case of a leap second
 */
trait ProjectSecondDateOperator extends ProjectSubOperator {
  def $second(target: String) = {
    require(target.startsWith("$"), "The $project.$second operator only accepts a $<fieldName> argument or a raw date; bare field names will not function. " +
    		"See http://docs.mongodb.org/manual/reference/aggregation/#_S_second")
    projectionOp("$second", target)
  }

  def $second[T: ValidDateType](target: T) = projectionOp("$second", target)
}


/*** CONDITIONAL EXPRESSIONS */

/**
 * $cond
 *
 * takes three expressions
 * evaluates first expression to boolean
 *  if first evals true, evaluates & returns second
 *  if first evals false, evaluates & returns third
 *
 * TODO - Better type limiting?
 */
trait ProjectCondConditionalOperator extends ProjectSubOperator {
  def $cond[T: ValidBarewordExpressionArgType](condition: T, ifTrue: Any, elseIfFalse: Any) =
    projectionOp("$cond", MongoDBList(condition, ifTrue, elseIfFalse))
}

/**
 * $ifNull
 *
 * Takes array w/ two expressions
 *  if first evals non-null, returns it
 *  else, returns evaluation of second expression
 *
 *  TODO - Better type limiting
 */
trait ProjectIfNullConditionalOperator extends ProjectSubOperator {
  def $ifNull(expression: Any, replacementIfNull: Any) = projectionOp("$ifNull", MongoDBList(expression, replacementIfNull))
}