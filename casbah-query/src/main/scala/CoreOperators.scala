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

import com.mongodb.{DBObject, BasicDBObjectBuilder}
import scalaj.collection.Imports._


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


trait ValueTestFluidQueryOperators extends LessThanOp 
                                      with LessThanEqualOp 
                                      with GreaterThanOp 
                                      with GreaterThanEqualOp
                                      with ModuloOp
                                      with SizeOp
                                      with AllOp
                                      with WhereOp
/**
 * Base trait for QueryOperators, children
 * are required to define a value for field, which is a String
 * and refers to the left-hand of the Query (e.g. in Mongo:
 * <code>{"foo": {"$ne": "bar"}}</code> "foo" is the field.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
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
  protected def op(op: String, target: Any) = MongoDBObject(dbObj match {
    case Some(nested) => {
      patchSerialization(target)
      nested.put(op, target)
      (field -> nested)
    }
    case None => {
      patchSerialization(target)
      val opMap = BasicDBObjectBuilder.start(op, target).get
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
     
}

trait NestingQueryHelper extends QueryOperator {
  import com.mongodb.BasicDBObject
  val oper: String
  val _dbObj: Option[DBObject]
  dbObj = _dbObj

  override protected def op(op: String, target: Any) = {
    val entry = MongoDBObject(oper -> MongoDBObject(op -> target))
    dbObj = dbObj match {
      case Some(nested) => nested.put(oper, entry); Some(nested)
      case None => Some(entry)
    }
    dbObj.map { o => field -> o }.head
  }

  def apply(target: Any) = { 
    target match {
      case sRE: scala.util.matching.Regex => op(field, sRE.pattern) 
      case jRE: java.util.regex.Pattern => op(field, jRE)
      case _ => {
        // assume it's some other item we need to nest.
        op(field, target)
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
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24ne
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
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
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
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
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
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait GreaterThanOp extends QueryOperator {
  def $gt(target: String) = op("$gt", target)
  def $gt(target: java.util.Date) = op("$gt", target)
  def $gt(target: AnyVal) = op("$gt", target)
  def $gt(target: DBObject) = op("$gt", target)
  def $gt(target: Map[String, Any]) = op("$gt", target.asDBObject)
  def $gt_:(target: Any) = op("$gt", target) 
}

/**
 * Trait to provide the $gte (Greater Than Or Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject and Map[String, Any].
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
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
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24in 
 */
trait InOp extends QueryOperator {
  def $in(target: Array[Any]) = op("$in", target.toList.asJava)
  def $in(target: Any*) = 
    if (target.size > 1)
      op("$in", target.toList.asJava) 
    else if (!target(0).isInstanceOf[Iterable[_]] &&
             !target(0).isInstanceOf[Array[_]]) 
      op("$in", List(target(0)))
    else op("$in", target(0))
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
  def $nin(target: Array[Any]) = op("$nin", target.toList.asJava)
  def $nin(target: Any*) = 
    if (target.size > 1)
      op("$nin", target.toList.asJava) 
    else if (!target(0).isInstanceOf[Iterable[_]] &&
             !target(0).isInstanceOf[Array[_]]) 
      op("$nin", List(target(0)))
    else op("$nin", target(0))
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
  def $all(target: Array[Any]) = op("$all", target.toList.asJava)
  def $all(target: Any*) = 
    if (target.size > 1)
      op("$all", target.toList.asJava) 
    else if (!target(0).isInstanceOf[Iterable[_]] &&
             !target(0).isInstanceOf[Array[_]])
      op("$all", List(target(0)))
    else op("$all", target(0))
}

/**
 * Trait to provide the $mod (Modulo) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject and Map[String, Any].  
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24mod
 */
trait ModuloOp extends QueryOperator {
  def $mod(left: Int, right: Int) = op("$mod", List(left, right).asJava)
}

/**
 * Trait to provide the $size (Size) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, AnyVal (see Scala docs but basically Int, Long, Char, Byte, etc)
 * DBObject.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24size
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
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%7B%7B%24exists%7D%7D
 */
trait ExistsOp extends QueryOperator {
  def $exists(target: Boolean) = op("$exists", target)
/*  // Shortcut which assumes you meant "true"
  def $exists = op("$exists", true)*/
}

/**
 * Trait to provide the $where (Where) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) JSFunction
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-JavascriptExpressionsand%7B%7B%24where%7D%7D
 */
trait WhereOp extends QueryOperator {
  def $where(target: JSFunction) = op("$where", target)
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
  /** Callbackey Nesting placeholding object for targetting correctly*/
  case class NotOpNester(val field: String, _dbObj: Option[DBObject]) extends NestingQueryHelper 
                                                                      with ValueTestFluidQueryOperators {
    val oper = "$not"
  }

  def $not = NotOpNester(field, dbObj)
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
  def $slice(target: Int) = op("$slice", target)
  def $slice(slice: Int, limit: Int) = op("$slice", MongoDBList(slice, limit))
}

/**
 * Special query operator only available on the right-hand side of an 
 * $addToSet which takes a list of values.
 *
 * THIS WILL NOT WORK IN MONGOD ANYWHERE BUT INSIDE AN ADDTOSET 
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24addToSet
 */
trait AddToSetEachOp extends QueryOperator {
  def $each(target: Array[Any]) = op("$each", target.toList.asJava)
  def $each(target: Any*) = 
    if (target.size > 1)
      op("$each", target.toList.asJava) 
    else if (!target(0).isInstanceOf[Iterable[_]] &&
             !target(0).isInstanceOf[Array[_]])
      op("$each", List(target(0)))
    else op("$each", target(0))
}
