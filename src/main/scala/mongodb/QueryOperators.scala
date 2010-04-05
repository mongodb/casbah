/**
 * Copyright (c) 2010, Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: Portions of this work are derived from the Apache License 2.0 "mongo-scala-driver" work
 * by Alexander Azarov <azarov@osinka.ru>, available from http://github.com/alaz/mongo-scala-driver
 */

package com.novus.mongodb

import com.mongodb.{DBObject, BasicDBObjectBuilder}
import scala.collection.JavaConversions._
import Implicits._

/**
 * Mixed trait which provides all possible
 * operators.  See Implicits for examples of usage.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait QueryOperators extends NotEqualsOp with
                             LessThanOp with
                             LessThanEqualOp with
                             GreaterThanOp with
                             GreaterThanEqualOp with
                             InOp with
                             NotInOp with
                             ModuloOp with
                             SizeOp with
                             ExistsOp with AllOp with WhereOp


/**
 * Base trait for QueryOperators, children
 * are required to define a value for field, which is a String
 * and refers to the left-hand of the Query (e.g. in Mongo:
 * <code>{"foo": {"$ne": "bar"}}</code> "foo" is the field.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
sealed trait QueryOperator {
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
   * the <code>op()</code> method will nest the target value and operator
   * inside the existing dbObj - this is useful for things like mixing
   * <code>$lte</code> and <code>$gte</code>
   *
   * WARNING: This does NOT check that target is a serializable type.
   * That is, for the moment, your own problem.
   */
  protected def op(op: String, target: Any) = {
    dbObj match {
      case Some(nested) => {
        nested.put(op, target)
        (field -> nested)
      }
      case None => {
        val opMap = BasicDBObjectBuilder.start(op, target).get
        (field -> opMap)
      }
    }
  }
}

/**
 * Trait to provide the $ne (Not Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject and Map[String, Any].
 *
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait NotEqualsOp extends QueryOperator {
  def $ne(target: String) = op("$ne", target)
  def $ne(target: AnyVal) = op("$ne", target)
  def $ne(target: DBObject) = op("$ne", target)
  def $ne(target: Map[String, Any]) = op("$ne", target.asDBObject)
}

/**
 * Trait to provide the $lt (Less Than) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject and Map[String, Any].
 *
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait LessThanOp extends QueryOperator {
  def $lt(target: String) = op("$lt", target)
  def $lt(target: java.util.Date) = op("$lt", target)
  def $lt(target: AnyVal) = op("$lt", target)
  def $lt(target: DBObject) = op("$lt", target)
  def $lt(target: Map[String, Any]) = op("$lt", target.asDBObject)
}

/**
 * Trait to provide the $lte (Less Than Or Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject and Map[String, Any].
 *
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait LessThanEqualOp extends QueryOperator {
  def $lte(target: String) = op("$lte", target)
  def $lte(target: java.util.Date) = op("$lte", target)
  def $lte(target: AnyVal) = op("$lte", target)
  def $lte(target: DBObject) = op("$lte", target)
  def $lte(target: Map[String, Any]) = op("$lte", target.asDBObject)
}

/**
 * Trait to provide the $gt (Greater Than) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject and Map[String, Any].
 *
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait GreaterThanOp extends QueryOperator {
  def $gt(target: String) = op("$gt", target)
  def $gt(target: java.util.Date) = op("$gt", target)
  def $gt(target: AnyVal) = op("$gt", target)
  def $gt(target: DBObject) = op("$gt", target)
  def $gt(target: Map[String, Any]) = op("$gt", target.asDBObject)
}

/**
 * Trait to provide the $gte (Greater Than Or Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject and Map[String, Any].
 *
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait GreaterThanEqualOp extends QueryOperator {
  def $gte(target: String) = op("$gte", target)
  def $gte(target: java.util.Date) = op("$gte", target)
  def $gte(target: AnyVal) = op("$gte", target)
  def $gte(target: DBObject) = op("$gte", target)
  def $gte(target: Map[String, Any]) = op("$gte", target.asDBObject)
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
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait InOp extends QueryOperator {
  def $in(target: Array[Any]) = op("$in", target)
  def $in(target: Any*) = op("$in", target)
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
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait NotInOp extends QueryOperator {
  def $nin(target: Array[Any]) = op("$nin", target)
  def $nin(target: Any*) = op("$nin", target)
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
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait AllOp extends QueryOperator {
  def $all(target: Array[Any]) = op("$all", target)
  def $all(target: Any*) = op("$all", target)
}

/**
 * Trait to provide the $mod (Modulo) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject and Map[String, Any].  
 *
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait ModuloOp extends QueryOperator {
  def $mod(target: String) = op("$mod", target)
  def $mod(target: AnyVal) = op("$mod", target)
  def $mod(target: DBObject) = op("$mod", target)
  def $mod(target: Map[String, Any]) = op("$mod", target.asDBObject)
}

/**
 * Trait to provide the $size (Size) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait SizeOp extends QueryOperator {
  def $size(target: String) = op("$size", target)
  def $size(target: AnyVal) = op("$size", target)
  def $size(target: DBObject) = op("$size", target)
}

/**
 * Trait to provide the $exists (Exists) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Booleans.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait ExistsOp extends QueryOperator {
  def $exists(target: Boolean) = op("$exists", target)
}

/**
 * Trait to provide the $where (Where) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) JSFunction
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait WhereOp extends QueryOperator {
  def $where(target: JSFunction) = op("$where", target)
}


// @Todo Regex support

