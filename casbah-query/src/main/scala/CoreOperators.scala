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

package com.mongodb.casbah.query.dsl

import com.mongodb.casbah.commons.Logging

import scala.collection.JavaConverters._

import com.mongodb.casbah.query.Imports._

import com.mongodb.casbah.query.ChainedOperator

import scala.util.matching._
import scala.collection.Iterable

import org.bson._
import org.bson.types.BasicBSONList

/**
 * Mixed trait which provides all possible
 * operators.  See Implicits for examples of usage.
 *
 */
trait FluidQueryOperators extends EqualsOp
  with NotEqualsOp
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
  with NotEqualsOp
  with TypeOp

trait QueryExpressionObject {
  self: DBObject =>
  def field: String
}

object QueryExpressionObject {

  def apply[A <: String, B <: Any](kv: (A, B)): DBObject with QueryExpressionObject = {
    val obj = new BasicDBObject with QueryExpressionObject { val field = kv._1 }
    obj.put(kv._1, kv._2)
    obj
  }

}

/**
 * Base trait for QueryOperators, children
 * are required to define a value for field, which is a String
 * and refers to the left-hand of the Query (e.g. in Mongo:
 * <code>{"foo": {"\$ne": "bar"}}</code> "foo" is the field.
 *
 */
trait QueryOperator extends ChainedOperator {
  /**
   * Base method for children to call to convert an operator call
   * into a nested Mongo DBObject.
   *
   * e.g. <code>"foo" \$ne "bar"</code> will convert to
   * <code>{"foo": {"\$ne": "bar"}}</code>
   *
   * Optionally, if dbObj, being <code>Some(DBObject)<code> is defined,
   * the <code>op(oper, )</code> method will nest the target value and operator
   * inside the existing dbObj - this is useful for things like mixing
   * <code>\$lte</code> and <code>\$gte</code>
   *
   * WARNING: This does NOT check that target is a serializable type.
   * That is, for the moment, your own problem.
   */
  protected def queryOp(oper: String, target: Any): DBObject with QueryExpressionObject = QueryExpressionObject(dbObj match {
    case Some(nested) => {
      nested.put(oper, target)
      (field -> nested)
    }
    case None => {
      val opMap = MongoDBObject(oper -> target)
      (field -> opMap)
    }
  })

  /**
   * Base method for children to call to convert an operator call
   * into a Mongo DBObject.
   *
   * e.g. <code>"foo" \$eq "bar"</code> will convert to
   * <code>{"foo": "bar"}</code>
   *
   * WARNING: This does NOT check that target is a serializable type.
   * That is, for the moment, your own problem.
   */
  protected def queryEq(target: Any): DBObject with QueryExpressionObject = QueryExpressionObject((field -> target))

}

/**
 * Trait to provide an equals method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.
 *
 */
trait EqualsOp extends QueryOperator {
  def $eq[A : AsQueryParam](a:A) = queryEq(AsQueryParam[A].asQueryParam(a))
}

/**
 * Trait to provide the \$ne (Not Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.
 *
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24ne
 */
trait NotEqualsOp extends QueryOperator {
  private val oper = "$ne"

  def $ne[A : AsQueryParam](a:A) = queryOp(oper, AsQueryParam[A].asQueryParam(a))
}

/**
 * Trait to provide the \$lt (Less Than) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.
 *
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait LessThanOp extends QueryOperator {
  private val oper = "$lt"

  def $lt[A : AsQueryParam](a: A) = queryOp(oper, AsQueryParam[A].asQueryParam(a))
}

/**
 * Trait to provide the \$lte (Less Than Or Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.*
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait LessThanEqualOp extends QueryOperator {
  private val oper = "$lte"

  def $lte[A : AsQueryParam](a: A) = queryOp(oper, AsQueryParam[A].asQueryParam(a))
}

/**
 * Trait to provide the \$gt (Greater Than) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.*
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait GreaterThanOp extends QueryOperator {
  private val oper = "$gt"

  def $gt[A : AsQueryParam](a: A) = queryOp(oper, AsQueryParam[A].asQueryParam(a))
}

/**
 * Trait to provide the \$gte (Greater Than Or Equal To) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) String, Numeric, JDK And Joda Dates,
 * Array, DBObject (and DBList), Iterable[_] and Tuple1->22.*
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%3C%2C%3C%3D%2C%3E%2C%3E%3D
 */
trait GreaterThanEqualOp extends QueryOperator {
  private val oper = "$gte"

  def $gte[A : AsQueryParam](a: A) = queryOp(oper, AsQueryParam[A].asQueryParam(a))
}

/**
 * Trait to provide the \$in (In Array) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Arrays of [Any] and variable argument lists of Any.
 *
 * Note that the magic of Scala DSLey-ness means that you can write a method such as:
 *
 * <code>var x = "foo" \$in (1, 2, 3, 5, 28)</code>
 *
 * As a valid statement - (1...28) is taken as the argument list to \$in and converted
 * to an Array under the covers.
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24in
 */
trait InOp extends QueryOperator {
  private val oper = "$in"

  def $in[A : AsQueryParam](a: A) = queryOp(oper, AsQueryParam[A].asQueryParam(a))
}

/**
 * Trait to provide the \$nin (NOT In Array) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Arrays of [Any] and variable argument lists of Any.
 *
 * Note that the magic of Scala DSLey-ness means that you can write a method such as:
 *
 * <code>var x = "foo" \$nin (1, 2, 3, 5, 28)</code>
 *
 * As a valid statement - (1...28) is taken as the argument list to \$nin and converted
 * to an Array under the covers.
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24nin
 */
trait NotInOp extends QueryOperator {
  private val oper = "$nin"

  def $nin[A : AsQueryParam](a: A) = queryOp(oper, AsQueryParam[A].asQueryParam(a))
}

/**
 * Trait to provide the \$all (Match ALL In Array) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Arrays of [Any] and variable argument lists of Any.
 *
 * Note that the magic of Scala DSLey-ness means that you can write a method such as:
 *
 * <code>var x = "foo" \$all (1, 2, 3, 5, 28)</code>
 *
 * As a valid statement - (1...28) is taken as the argument list to \$all and converted
 * to an Array under the covers.
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24all
 */
trait AllOp extends QueryOperator {
  private val oper = "$all"

  def $all[A : AsQueryParam](a: A) = queryOp(oper, AsQueryParam[A].asQueryParam(a))
}

/**
 * Trait to provide the \$mod (Modulo) method on appropriate callers.
 *
 * Targets a left and right value where the formula is (field % left == right)
 *
 * Left and Right can be any ValidNumericType and of two differing types (e.g. one int, one float)
 *
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24mod
 */
trait ModuloOp extends QueryOperator {
  private val oper = "$mod"

  def $mod[A: ValidNumericType, B: ValidNumericType](left: A, right: B) = queryOp(oper, MongoDBList(left, right))
}

/**
 * Trait to provide the \$size (Size) method on appropriate callers.
 *
 * Test value must be an Int or BigInt.
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24size
 */
trait SizeOp extends QueryOperator {
  private val oper = "$size"


  // TODO - Accept Numeric? As long as we can downconvert for mongo type?
  def $size(target: Int) = queryOp(oper, target)
  def $size(target: BigInt) = queryOp(oper, target)
}

/**
 * Trait to provide the \$exists (Exists) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) Booleans.
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%7B%7B%24exists%7D%7D
 */
trait ExistsOp extends QueryOperator {
  private val oper = "$exists"

  def $exists(target: Boolean) = queryOp(oper, target)
}

/**
 * Trait to provide the \$not (Not) negation method on appropriate callers.
 *
 * Make sure your anchor it when you have multiple operators e.g.
 *
 * <code>"foo".\$not \$mod(5, 10)</code>
 *
 * Targets (takes a right-hand value of) DBObject or a Scala RegEx
 *
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

  def $not(re: scala.util.matching.Regex) = queryOp(oper, re.pattern)
  def $not(re: java.util.regex.Pattern) = queryOp(oper, re)
}

/**
 * Trait to provide the \$slice (Slice of Array) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) either an Int of slice indicator or a tuple
 * of skip and limit.
 *
 * &gt; "foo" \$slice 5
 * res0: (String, com.mongodb.DBObject) = (foo,{ "\$slice" : 5})
 *
 * &gt; "foo" \$slice (5, -1)
 * res1: (String, com.mongodb.DBObject) = (foo,{ "\$slice" : [ 5 , -1]})
 *
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24sliceoperator
 *
 */
trait SliceOp extends QueryOperator {
  private val oper = "$slice"

  def $slice(target: Int) = queryOp(oper, target)
  def $slice(slice: Int, limit: Int) = queryOp(oper, MongoDBList(slice, limit))
}

/**
 * Trait to provide the \$elemMatch method on appropriate callers.
 *
 * Targets (takes a right-hand value of) a DBObject view context
 *
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Dot+Notation+(Reaching+into+Objects)#DotNotation%28ReachingintoObjects%29-Matchingwith%24elemMatch
 *
 */
trait ElemMatchOp extends QueryOperator {
  private val oper = "$elemMatch"

  def $elemMatch[A <% DBObject](target: A) = queryOp(oper, target)
}

sealed abstract class BSONType[A](val operator: Byte)

object BSONType {
  implicit object BSONDouble extends BSONType[Double](BSON.NUMBER)
  implicit object BSONString extends BSONType[String](BSON.STRING)
  implicit object BSONObject extends BSONType[BSONObject](BSON.OBJECT)
  implicit object DBObject extends BSONType[DBObject](BSON.OBJECT)
  implicit object DBList extends BSONType[BasicDBList](BSON.ARRAY)
  implicit object BSONDBList extends BSONType[BasicBSONList](BSON.ARRAY)
  implicit object BSONBinary extends BSONType[Array[Byte]](BSON.BINARY)
  //  implicit object BSONArray extends BSONType[Array[_]]
  //  implicit object BSONList extends BSONType[List[_]]
  implicit object BSONObjectId extends BSONType[ObjectId](BSON.OID)
  implicit object BSONBoolean extends BSONType[Boolean](BSON.BOOLEAN)
  implicit object BSONJDKDate extends BSONType[java.util.Date](BSON.DATE)
  implicit object BSONJodaDateTime extends BSONType[org.joda.time.DateTime](BSON.DATE)
  implicit object BSONNull extends BSONType[Option[Nothing]](BSON.NULL)
  implicit object BSONRegex extends BSONType[Regex](BSON.REGEX)
  implicit object BSONSymbol extends BSONType[Symbol](BSON.SYMBOL)
  implicit object BSON32BitInt extends BSONType[Int](BSON.NUMBER_INT)
  implicit object BSON64BitInt extends BSONType[Long](BSON.NUMBER_LONG)
  implicit object BSONSQLTimestamp extends BSONType[java.sql.Timestamp](BSON.TIMESTAMP)
}

/**
 * \$type operator to query by type.
 *
 * Can type a BSON.<enum value> or a Context Bounded check.
 *
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
  def $type(arg: Byte) = queryOp(oper, arg)

  /**
   * Matches types based on a Context Bound.
   * Requires anchoring to prevent compiler confusion:
   *
   *    "foo".\$type[Double]
   *
   */
  def $type[A](implicit bsonType: BSONType[A]) = queryOp(oper, bsonType.operator)
}

trait GeospatialOps extends GeoNearOp
  with GeoNearSphereOp
  with GeoWithinOps

case class GeoCoords[A: ValidNumericType: Manifest, B: ValidNumericType: Manifest](val lat: A, val lon: B) {
  def toList = MongoDBList(lat, lon)

  override def toString = "GeoCoords(%s, %s)".format(lat, lon)
}

/**
 *
 * Trait to provide the \$near geospatial search method on appropriate callers
 *
 * Note that the args aren't TECHNICALLY latitude and longitude as they depend on:
 *   a) the order you specified your actual index in
 *   b) if you're using actual world maps or something else
 *
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Geospatial+Indexing
 */
trait GeoNearOp extends QueryOperator {
  private val oper = "$near"

  def $near(coords: GeoCoords[_, _]) = new NearOpWrapper(coords)

  sealed class NearOpWrapper(coords: GeoCoords[_, _]) extends BasicDBObject {
    put(field, new BasicDBObject("$near", coords.toList))

    def $maxDistance[T: Numeric](radius: T): DBObject = {
      get(field).asInstanceOf[DBObject].put("$maxDistance", radius)
      this
    }

  }

}

/**
 *
 * Trait to provide the \$nearSphere geospatial search method on appropriate callers
 *
 *
 * Note that  the args aren't TECHNICALLY latitude and longitude as they depend on:
 *   a) the order you specified your actual index in
 *   b) if you're using actual world maps or something else
 *
 *
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Geospatial+Indexing
 */
trait GeoNearSphereOp extends QueryOperator {
  private val oper = "$nearSphere"

  def $nearSphere(coords: GeoCoords[_, _]) = queryOp(oper, coords.toList)
}

/**
 *
 * Trait to provide the \$within geospatial search method on appropriate callers
 *
 *
 * Note that  the args aren't TECHNICALLY latitude and longitude as they depend on:
 *   a) the order you specified your actual index in
 *   b) if you're using actual world maps or something else
 *
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Geospatial+Indexing
 */
trait GeoWithinOps extends QueryOperator {
  self =>
  private val oper = "$within"

  def $within = new QueryOperator {
    val field = "$within"

    def $box(lowerLeft: GeoCoords[_, _], upperRight: GeoCoords[_, _]) =
      MongoDBObject(
        self.field ->
        queryOp("$box", MongoDBList(lowerLeft.toList, upperRight.toList)))

    def $center[T: Numeric](center: GeoCoords[_, _], radius: T) =
      MongoDBObject(
        self.field ->
        queryOp("$center", MongoDBList(center.toList, radius)))

    def $centerSphere[T: Numeric](center: GeoCoords[_, _], radius: T) =
      MongoDBObject(
        self.field ->
        queryOp("$centerSphere", MongoDBList(center.toList, radius)))
  }

}
