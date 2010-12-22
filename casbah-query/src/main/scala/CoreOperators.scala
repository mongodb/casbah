/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
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

package com.mongodb.casbah
package query

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.commons.Logging


import com.mongodb.{DBObject, BasicDBObjectBuilder}
import scalaj.collection.Imports._

import scala.util.matching._
import scala.collection.Iterable

import org.bson._
import org.bson.types.BasicBSONList

/**
 * Mixed trait which provides all possible
 * operators.  See Implicits for examples of usage.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
trait FluidQueryOperators extends NotEqualsOp 
                             with LessThanOp 
                             with LessThanEqualOp 
                             with GreaterThanOp 
                             with GreaterThanEqualOp 
                             with InOp 
                             with NotInOp 
                             with ModuloOp 
                             with SizeOp 
                             with ExistsOp 
                             with AllOp 
                             with WhereOp 
                             with NotOp
                             with SliceOp
                             with TypeOp
                             with ElemMatchOp
                             with GeospatialOps


trait ValueTestFluidQueryOperators extends LessThanOp 
                                      with LessThanEqualOp 
                                      with GreaterThanOp 
                                      with GreaterThanEqualOp
                                      with ModuloOp
                                      with SizeOp
                                      with AllOp
                                      with WhereOp
                                      with NotEqualsOp
                                      with TypeOp

trait DSLDBObject {
  val field: String
}

object DSLDBObject {
  
  def apply[A <: String, B <: Any](kv: (A, B)): DBObject with DSLDBObject = {
    val obj = new BasicDBObject with DSLDBObject { val field = kv._1 }
    obj.put(kv._1, kv._2)
    obj
  }


}
/**
 * Base trait for QueryOperators, children
 * are required to define a value for field, which is a String
 * and refers to the left-hand of the Query (e.g. in Mongo:
 * <code>{"foo": {"$ne": "bar"}}</code> "foo" is the field.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
sealed trait QueryOperator extends Logging {
  val field: String
  protected var dbObj: Option[DBObject] = None

  /**
   * Base method for children to call to convert an operator call
   * into a Mongo DBObject.
   *
   * e.g. <code>"foo" $ne "bar"</code> will convert to
   * <code>{"foo": {"$ne": "bar"}}</code>
   * 
   * Optionally, if dbObj, being <code>Some(DBObject)<code> is defined,
   * the <code>op(oper, )</code> method will nest the target value and operator
   * inside the existing dbObj - this is useful for things like mixing
   * <code>$lte</code> and <code>$gte</code>
   *
   * WARNING: This does NOT check that target is a serializable type.
   * That is, for the moment, your own problem.
   */
  protected def op(oper: String, target: Any) = DSLDBObject(dbObj match {
    case Some(nested) => {
      patchSerialization(target)
      nested.put(oper, target)
      (field -> nested)
    }
    case None => {
      patchSerialization(target)
      val opMap = MongoDBObject(oper -> target)
      (field -> opMap)
    }
  })
  /** 
   * Temporary fix code for making sure certain edge cases w/ the serialization libs 
   * Don't happen.  This may impose a slight performance penalty.
   */
  protected def patchSerialization(target: Any): Unit = target match {
    case _ => {}
  }

  def anyListOp(oper: String, target: Any*) = 
    if (target.size > 1)
      op(oper, target.toList.asJava) 
    else if (!target(0).isInstanceOf[Iterable[_]] &&
             !target(0).isInstanceOf[Array[_]]) 
      op(oper, List(target(0)))
    else op(oper, target(0))
     
}


/**
 * Trait to provide the $ne (Not Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric,  
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24ne
 */
trait NotEqualsOp extends QueryOperator {
  private val oper = "$ne" 

  def $ne(target: String) = op(oper, target)
  def $ne(target: DBObject) = op(oper, target)
  def $ne(target: DBRef) = op(oper, target)
  def $ne(target: ObjectId) = op(oper, target)
  def $ne(target: Boolean) = op(oper, target)
  def $ne(target: Array[_]) = op(oper, target.toList.asJava)
  def $ne(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $ne(target: Iterable[_]) = op(oper, target.toList)
  def $ne[T : ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * Trait to provide the $lt (Less Than) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates, 
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait LessThanOp extends QueryOperator {
  private val oper = "$lt" 

  def $lt(target: String) = op(oper, target)
  def $lt(target: DBObject) = op(oper, target)
  def $lt(target: Array[_]) = op(oper, target.toList.asJava)
  def $lt(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lt(target: Iterable[_]) = op(oper, target.toList)
  def $lt[T : ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * Trait to provide the $lte (Less Than Or Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates, 
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.*
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait LessThanEqualOp extends QueryOperator {
  private val oper = "$lte" 

  def $lte(target: String) = op(oper, target)
  def $lte(target: DBObject) = op(oper, target)
  def $lte(target: Array[_]) = op(oper, target.toList.asJava)
  def $lte(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $lte(target: Iterable[_]) = op(oper, target.toList)
  def $lte[T : ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * Trait to provide the $gt (Greater Than) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates, 
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.*
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait GreaterThanOp extends QueryOperator {
  private val oper = "$gt" 

  def $gt(target: String) = op(oper, target)
  def $gt(target: DBObject) = op(oper, target)
  def $gt(target: Array[_]) = op(oper, target.toList.asJava)
  def $gt(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gt(target: Iterable[_]) = op(oper, target.toList)
  def $gt[T : ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * Trait to provide the $gte (Greater Than Or Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates, 
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.*
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait GreaterThanEqualOp extends QueryOperator {
  private val oper = "$gte" 

  def $gte(target: String) = op(oper, target)
  def $gte(target: DBObject) = op(oper, target)
  def $gte(target: Array[_]) = op(oper, target.toList.asJava)
  def $gte(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $gte(target: Iterable[_]) = op(oper, target.toList)
  def $gte[T : ValidDateOrNumericType](target: T) = op(oper, target)
}

/**
 * Trait to provide the $in (In Array) method on appropriate callers.
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
trait InOp extends QueryOperator {
  private val oper = "$in" 
  
  def $in(target: Array[_]) = op(oper, target.toList.asJava)
  def $in(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $in(target: Iterable[_]) = op(oper, target.toList)
}

/**
 * Trait to provide the $nin (NOT In Array) method on appropriate callers.
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
trait NotInOp extends QueryOperator {
  private val oper = "$nin" 

  def $nin(target: Array[_]) = op(oper, target.toList.asJava)
  def $nin(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $nin(target: Iterable[_]) = op(oper, target.toList)
}

/**
 * Trait to provide the $all (Match ALL In Array) method on appropriate callers.
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
trait AllOp extends QueryOperator {
  private val oper = "$all" 

  def $all(target: Array[_]) = op(oper, target.toList.asJava)
  def $all(target: Tuple1[_]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple2[_, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple3[_, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple4[_, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple5[_, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple6[_, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple7[_, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple8[_, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple9[_, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple10[_, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple11[_, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = op(oper, target.productIterator.toList)
  def $all(target: Iterable[_]) = op(oper, target.toList)
}

/**
 * Trait to provide the $mod (Modulo) method on appropriate callers.
 *
 * Targets a left and right value where the formula is (field % left == right)
 *
 * Left and Right can be any ValidNumericType and of two differing types (e.g. one int, one float)
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24mod
 */
trait ModuloOp extends QueryOperator {
  private val oper = "$mod" 

  def $mod[A : ValidNumericType, B : ValidNumericType](left: A, right: B) = op(oper, MongoDBList(left, right))
}

/**
 * Trait to provide the $size (Size) method on appropriate callers.
 *
 * Test value must be an Int or BigInt.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24size
 */
trait SizeOp extends QueryOperator {
  private val oper = "$size"

  def $size(target: Int) = op(oper, target)
  def $size(target: BigInt) = op(oper, target)
}

/**
 * Trait to provide the $exists (Exists) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Booleans.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%7B%7B%24exists%7D%7D
 */
trait ExistsOp extends QueryOperator {
  private val oper = "$exists"

  def $exists(target: Boolean) = op(oper, target)
}

/**
 * Trait to provide the $where (Where) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) JSFunction [which is currently just as string containing a javascript function]
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-JavascriptExpressionsand%7B%7B%24where%7D%7D
 */
trait WhereOp extends QueryOperator {
  private val oper = "$where"

  def $where(target: JSFunction) = op(oper, target)
}

/**
 * Trait to provide the $not (Not) negation method on appropriate callers.
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
trait NotOp extends QueryOperator {
  private val oper = "$not"

  def $not(inner: FluidQueryOperators => DBObject) = {
    val dbObj = inner(new FluidQueryOperators {
      val field = oper
    })
    MongoDBObject(field -> dbObj)
  }

  def $not(re: scala.util.matching.Regex) = op(field, re.pattern)
  def $not(re: java.util.regex.Pattern) = op(field, re)
}

/**
 * Trait to provide the $slice (Slice of Array) method on appropriate callers.
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
trait SliceOp extends QueryOperator {
  private val oper = "$slice"

  def $slice(target: Int) = op(oper, target)
  def $slice(slice: Int, limit: Int) = op(oper, MongoDBList(slice, limit))
}



/**
 * Trait to provide the $elemMatch method on appropriate callers.
 *
 * Targets (takes a right-hand value of) a DBObject view context
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Dot+Notation+(Reaching+into+Objects)#DotNotation%28ReachingintoObjects%29-Matchingwith%24elemMatch 
 *
 */
trait ElemMatchOp extends QueryOperator {
  private val oper = "$elemMatch"

  def $elemMatch[A <% DBObject](target: A) = op(oper, target)
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
trait TypeOp extends QueryOperator {
  private val oper = "$type" 

  /** 
   * For those who want to pass the static byte from org.bson.BSON explicitly
   * (or with the simple BSON spec indicator)
   * TODO: Test for a valid byte, right now we accept anything you say.
   */
  def $type(arg: Byte) = op(oper, arg)

  /**
   * Matches types based on a Context Bound.
   * Requires anchoring to prevent compiler confusion:
   *
   *    "foo".$type[Double]
   *
   */
  def $type[A : BSONType : Manifest] = 
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


trait GeospatialOps extends GeoNearOp
                       with GeoNearSphereOp
                       with GeoWithinOps


case class GeoCoords[A : ValidNumericType : Manifest, B : ValidNumericType : Manifest]
                    (val lat: A, val lon: B) {
  def toList = MongoDBList(lat, lon)

  override def toString = "GeoCoords(%s, %s)".format(lat, lon)
}
  

/**
 * 
 * Trait to provide the $near geospatial search method on appropriate callers
 *
 * Note that the args aren't TECHNICALLY latitude and longitude as they depend on:
 *   a) the order you specified your actual index in
 *   b) if you're using actual world maps or something else
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Geospatial+Indexing
 */
trait GeoNearOp extends QueryOperator {
  private val oper = "$near"

  def $near(coords: GeoCoords[_, _]) = new NearOpWrapper(coords)
  
  sealed class NearOpWrapper(coords: GeoCoords[_, _]) extends BasicDBObject {  
    put(field, new BasicDBObject("$near", coords.toList))

    def $maxDistance[T : Numeric](radius: T): DBObject = {
      get(field).asInstanceOf[DBObject].put("$maxDistance", radius)
      this
    }

  }

}

/**
 * 
 * Trait to provide the $nearSphere geospatial search method on appropriate callers
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
trait GeoNearSphereOp extends QueryOperator {
  private val oper = "$nearSphere"
  
  def $nearSphere(coords: GeoCoords[_,_]) = op(oper, coords.toList)
}

/**
 * 
 * Trait to provide the $within geospatial search method on appropriate callers
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
trait GeoWithinOps extends QueryOperator {
  private val oper = "$within" 

  def $within = new QueryOperator {
    val nestedOper = "$within"
    val field = "$within" 

    def $box(lowerLeft: GeoCoords[_,_], upperRight: GeoCoords[_,_]) =
      op(oper, MongoDBList(lowerLeft.toList, upperRight.toList))

    def $center[T : Numeric](center: GeoCoords[_,_], radius: T) = 
      op(oper, MongoDBList(center.toList, radius))

    def $centerSphere[T : Numeric](center: GeoCoords[_,_], radius: T) = 
      op(oper, MongoDBList(center.toList, radius))
  }
  
}
