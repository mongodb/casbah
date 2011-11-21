/**
 * Copyright (c) 2008 - 2011 10gen, Inc. <http://10gen.com>
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
 */

package com.mongodb.casbah.query.dsl
package light

import com.mongodb.casbah.util.Logging

import scalaj.collection.Imports._

import com.mongodb.casbah.query._

import scala.util.matching._
import scala.collection.Iterable

import org.bson._
import org.bson.types.BasicBSONList


/**
 * Mixed trait Lightwhich provides all possible
 * operators.  See Implicits for examples of usage.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
trait LightFluidQueryOperators extends LightNotEqualsOp
                            with LightLessThanOp
                            with LightLessThanEqualOp
                            with LightGreaterThanOp
                            with LightGreaterThanEqualOp
/*                            with LightInOp*/
                            with LightNotInOp
                            with LightModuloOp
                            with LightSizeOp
                            with LightExistsOp
                            with LightAllOp
                            with LightWhereOp
                            with LightNotOp
/*                            with LightSliceOp*/
                            with LightTypeOp
                            with LightElemMatchOp
                            with LightGeospatialOps

trait LightValueTestFluidQueryOperators extends LightLessThanOp
                                    with LightLessThanEqualOp
                                    with LightGreaterThanOp
                                    with LightGreaterThanEqualOp
                                    with LightModuloOp
                                    with LightSizeOp
                                    with LightAllOp
                                    with LightWhereOp
                                    with LightNotEqualsOp
                                    with LightTypeOp

/**
 * trait to provide the $ne (Not Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24ne
 */
trait LightNotEqualsOp extends QueryOperator {
  private val oper = "$ne"

  def ne(target: String) = op(oper, target)
  def ne(target: DBObject) = op(oper, target)
  def ne(target: DBRef) = op(oper, target)
  def ne(target: ObjectId) = op(oper, target)
  def ne(target: Boolean) = op(oper, target)
  def ne(target: Array[_]) = op(oper, target.toList)
  def ne(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def ne(target: Iterable[_]) = op(oper, target.toList)
  def ne[T: ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * trait to provide the $lt (Less Than) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait LightLessThanOp extends QueryOperator {
  private val oper = "$lt"

  def lt(target: String) = op(oper, target)
  def lt(target: DBObject) = op(oper, target)
  def lt(target: Array[_]) = op(oper, target.toList)
  def lt(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lt(target: Iterable[_]) = op(oper, target.toList)
  def lt[T: ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * trait to provide the $lte (Less Than Or Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.*
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait LightLessThanEqualOp extends QueryOperator {
  private val oper = "$lte"

  def lte(target: String) = op(oper, target)
  def lte(target: DBObject) = op(oper, target)
  def lte(target: Array[_]) = op(oper, target.toList)
  def lte(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def lte(target: Iterable[_]) = op(oper, target.toList)
  def lte[T: ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * trait to provide the $gt (Greater Than) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.*
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait LightGreaterThanOp extends QueryOperator {
  private val oper = "$gt"

  def gt(target: String) = op(oper, target)
  def gt(target: DBObject) = op(oper, target)
  def gt(target: Array[_]) = op(oper, target.toList)
  def gt(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gt(target: Iterable[_]) = op(oper, target.toList)
  def gt[T: ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * trait to provide the $gte (Greater Than Or Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.*
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait LightGreaterThanEqualOp extends QueryOperator {
  private val oper = "$gte"

  def gte(target: String) = op(oper, target)
  def gte(target: DBObject) = op(oper, target)
  def gte(target: Array[_]) = op(oper, target.toList)
  def gte(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def gte(target: Iterable[_]) = op(oper, target.toList)
  def gte[T: ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * trait to provide the $in (In Array) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Arrays of [Any] and variable argument lists of Any.
 *
 * Note that the magic of Scala DSLey-ness means that you can write a method such as:
 *
 * <code>var x = "foo" $in (1, 2, 3, 5, 28)</code>
 *
 * As a valid statement - (1...28) is taken as the argument list to $in and converted
 * to an Array under the covers.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24in
 */
trait LightInOp extends QueryOperator {
  private val oper = "$in"

  def in(target: Array[_]) = op(oper, target.toList)
  def in(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def in(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def in(target: Iterable[_]) = op(oper, target.toList)
}

/**
 * trait to provide the $nin (NOT In Array) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Arrays of [Any] and variable argument lists of Any.
 *
 * Note that the magic of Scala DSLey-ness means that you can write a method such as:
 *
 * <code>var x = "foo" $nin (1, 2, 3, 5, 28)</code>
 *
 * As a valid statement - (1...28) is taken as the argument list to $nin and converted
 * to an Array under the covers.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24nin
 */
trait LightNotInOp extends QueryOperator {
  private val oper = "$nin"

  def nin(target: Array[_]) = op(oper, target.toList)
  def nin(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def nin(target: Iterable[_]) = op(oper, target.toList)
}

/**
 * trait to provide the $all (Match ALL In Array) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Arrays of [Any] and variable argument lists of Any.
 *
 * Note that the magic of Scala DSLey-ness means that you can write a method such as:
 *
 * <code>var x = "foo" $all (1, 2, 3, 5, 28)</code>
 *
 * As a valid statement - (1...28) is taken as the argument list to $all and converted
 * to an Array under the covers.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24all
 */
trait LightAllOp extends QueryOperator {
  private val oper = "$all"

  def all(target: Array[_]) = op(oper, target.toList)
  def all(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def all(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def all(target: Iterable[_]) = op(oper, target.toList)
}

/**
 * trait to provide the $mod (Modulo) method on appropriate callers.
 *
 * Targets a left and right value where the formula is (field % left == right)
 *
 * Left and Right can be any ValidNumericType and of two differing types (e.g. one int, one float)
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24mod
 */
trait LightModuloOp extends QueryOperator {
  private val oper = "$mod"

  def mod[A: ValidNumericType, B: ValidNumericType](left: A, right: B) = op(oper, MongoDBList(left, right))
}

/**
 * trait to provide the $size (Size) method on appropriate callers.
 *
 * Test value must be an Int or BigInt.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24size
 */
trait LightSizeOp extends QueryOperator {
  private val oper = "$size"

  def size(target: Int) = op(oper, target)
  def size(target: BigInt) = op(oper, target)
}

/**
 * trait to provide the $exists (Exists) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Booleans.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%7B%7B%24exists%7D%7D
 */
trait LightExistsOp extends QueryOperator {
  private val oper = "$exists"

  def exists(target: Boolean) = op(oper, target)
}

/**
 * trait to provide the $where (Where) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) JSFunction [which is currently just as string containing a javascript function]
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-JavascriptExpressionsand%7B%7B%24where%7D%7D
 */
trait LightWhereOp extends QueryOperator {
  private val oper = "$where"

  def where(target: JSFunction) = op(oper, target)
}

/**
 * trait to provide the $not (Not) negation method on appropriate callers.
 *
 * Make sure your anchor it when you have multiple operators e.g.
 *
 * "foo".$not $mod(5, 10)
 *
 * Targets (takes a right-hand value of) DBObject or a Scala RegEx
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-Metaoperator%3A%24not
 */
trait LightNotOp extends QueryOperator {
  private val oper = "$not"

  def not(inner: LightFluidQueryOperators => DBObject) = {
    val dbObj = inner(new LightFluidQueryOperators {
      val field = oper
    })
    MongoDBObject(field -> dbObj)
  }

  def not(re: scala.util.matching.Regex) = op(oper, re.pattern)
  def not(re: java.util.regex.Pattern) = op(oper, re)
}

/**
 * trait to provide the $slice (Slice of Array) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) either an Int of slice indicator or a tuple
 * of skip and limit.
 *
 * &gt; "foo" $slice 5
 * res0: (String, com.mongodb.DBObject) = (foo,{ "$slice" : 5})
 *
 * &gt; "foo" $slice (5, -1)
 * res1: (String, com.mongodb.DBObject) = (foo,{ "$slice" : [ 5 , -1]})
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24sliceoperator
 *
 */
trait LightSliceOp extends QueryOperator {
  private val oper = "$slice"

  def slice(target: Int) = op(oper, target)
  def slice(slice: Int, limit: Int) = op(oper, MongoDBList(slice, limit))
}

/**
 * trait to provide the $elemMatch method on appropriate callers.
 *
 * Targets (takes a right-hand value of) a DBObject view context
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Dot+Notation+(Reaching+into+Objects)#DotNotation%28ReachingintoObjects%29-Matchingwith%24elemMatch
 *
 */
trait LightElemMatchOp extends QueryOperator {
  private val oper = "$elemMatch"

  def elemMatch[A <% DBObject](target: A) = op(oper, target)
}

abstract class BSONType[A]

object BSONType {
  implicit object BSONDouble extends BSONType[Double]
  implicit object BSONString extends BSONType[String]
  implicit object BSONObject extends BSONType[BSONObject]
  implicit object DBObject extends BSONType[DBObject]
  implicit object DBList extends BSONType[BasicDBList]
  implicit object BSONDBList extends BSONType[BasicBSONList]
  implicit object BSONBinary extends BSONType[Array[Byte]]
  implicit object BSONArray extends BSONType[Array[_]]
  implicit object BSONList extends BSONType[List[_]]
  implicit object BSONObjectId extends BSONType[ObjectId]
  implicit object BSONBoolean extends BSONType[Boolean]
  implicit object BSONJDKDate extends BSONType[java.util.Date]
  implicit object BSONJodaDateTime extends BSONType[org.joda.time.DateTime]
  implicit object BSONNull extends BSONType[Option[Nothing]]
  implicit object BSONRegex extends BSONType[Regex]
  implicit object BSONSymbol extends BSONType[Symbol]
  implicit object BSON32BitInt extends BSONType[Int]
  implicit object BSON64BitInt extends BSONType[Long]
  implicit object BSONSQLTimestamp extends BSONType[java.sql.Timestamp]
}

/**
 * $type operator to query by type.
 *
 * Can type a BSON.<enum value> or a Context Bounded check.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%7B%7B%24type%7D%7D
 */
trait LightTypeOp extends QueryOperator {
  private val oper = "$type"

  /**
   * For those who want to pass the static byte from org.bson.BSON explicitly
   * (or with the simple BSON spec indicator)
   * TODO: Test for a valid byte, right now we accept anything you say.
   */
  def `type`(arg: Byte) = op(oper, arg)

  /**
   * Matches types based on a Context Bound.
   * Requires anchoring to prevent compiler confusion:
   *
   *    "foo".`type`[Double]
   *
   */
  def `type`[A: BSONType: Manifest] =
    if (manifest[A] <:< manifest[Double])
      op(oper, BSON.NUMBER)
    else if (manifest[A] <:< manifest[String])
      op(oper, BSON.STRING)
    else if (manifest[A] <:< manifest[BasicDBList] ||
      manifest[A] <:< manifest[BasicBSONList])
      op(oper, BSON.ARRAY)
    else if (manifest[A] <:< manifest[BSONObject] ||
      manifest[A] <:< manifest[DBObject])
      op(oper, BSON.OBJECT)
    else if (manifest[A] <:< manifest[ObjectId])
      op(oper, BSON.OID)
    else if (manifest[A] <:< manifest[Boolean])
      op(oper, BSON.BOOLEAN)
    else if (manifest[A] <:< manifest[java.sql.Timestamp])
      op(oper, BSON.TIMESTAMP)
    else if (manifest[A] <:< manifest[java.util.Date] ||
      manifest[A] <:< manifest[org.joda.time.DateTime])
      op(oper, BSON.DATE)
    else if (manifest[A] <:< manifest[Option[Nothing]])
      op(oper, BSON.NULL)
    else if (manifest[A] <:< manifest[Regex])
      op(oper, BSON.REGEX)
    else if (manifest[A] <:< manifest[Symbol])
      op(oper, BSON.SYMBOL)
    else if (manifest[A] <:< manifest[Int])
      op(oper, BSON.NUMBER_INT)
    else if (manifest[A] <:< manifest[Long])
      op(oper, BSON.NUMBER_LONG)
    else if (manifest[A].erasure.isArray &&
      manifest[A] <:< manifest[Array[Byte]])
      op(oper, BSON.BINARY)
    else
      throw new IllegalArgumentException("Invalid BSON Type '%s' for matching".format(manifest.erasure))
}

trait LightGeospatialOps extends LightGeoNearOp
                            with LightGeoNearSphereOp
                            with LightGeoWithinOps

/**
 *
 * trait to provide the $near geospatial search method on appropriate callers
 *
 * Note that the args aren't TECHNICALLY latitude and longitude as they depend on:
 *   a) the order you specified your actual index in
 *   b) if you're using actual world maps or something else
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Geospatial+Indexing
 */
trait LightGeoNearOp extends QueryOperator {
  private val oper = "$near"

  def near(coords: dsl.GeoCoords[_, _]) = new NearOpWrapper(coords)

  sealed class NearOpWrapper(coords: dsl.GeoCoords[_, _]) extends BasicDBObject {
    put(field, new BasicDBObject("$near", coords.toList))

    def maxDistance[T: Numeric](radius: T): DBObject = {
      get(field).asInstanceOf[DBObject].put("$maxDistance", radius)
      this
    }

  }

}

/**
 *
 * trait to provide the $nearSphere geospatial search method on appropriate callers
 *
 *
 * Note that  the args aren't TECHNICALLY latitude and longitude as they depend on:
 *   a) the order you specified your actual index in
 *   b) if you're using actual world maps or something else
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Geospatial+Indexing
 */
trait LightGeoNearSphereOp extends QueryOperator {
  private val oper = "$nearSphere"

  def nearSphere(coords: dsl.GeoCoords[_, _]) = op(oper, coords.toList)
}

/**
 *
 * trait to provide the $within geospatial search method on appropriate callers
 *
 *
 * Note that  the args aren't TECHNICALLY latitude and longitude as they depend on:
 *   a) the order you specified your actual index in
 *   b) if you're using actual world maps or something else
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Geospatial+Indexing
 */
trait LightGeoWithinOps extends QueryOperator {
  self =>
  private val oper = "$within"

  def within = new QueryOperator {
    val field = "$within"

    def box(lowerLeft: dsl.GeoCoords[_, _], upperRight: dsl.GeoCoords[_, _]) =
      MongoDBObject(
        self.field ->
          op("$box", MongoDBList(lowerLeft.toList, upperRight.toList)))

    def center[T: Numeric](center: dsl.GeoCoords[_, _], radius: T) =
      MongoDBObject(
        self.field ->
          op("$center", MongoDBList(center.toList, radius)))

    def centerSphere[T: Numeric](center: dsl.GeoCoords[_, _], radius: T) =
      MongoDBObject(
        self.field ->
          op("$centerSphere", MongoDBList(center.toList, radius)))
  }

}

