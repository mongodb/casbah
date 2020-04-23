/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed un  def $cond(der the License is distributed on an "AS IS" BASIS,
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
package query

import scala.language.implicitConversions
import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.query.dsl.{FluidQueryOperators, GeoCoords, ValueTestFluidQueryOperators}

import scala.math.Ordering.Float.TotalOrdering

// scalastyle:off number.of.types

trait Implicits {

  /**
   * Implicit extension methods for String values (e.g. a field name)
   * to add Mongo's query operators, minimizing the need to write long series'
   * of nested maps.
   *
   * Mixes in the QueryOperators defined in the QueryOperators mixin.
   * The NestedQuery implicit [Defined below] allows you to call chained operators on the return value of this
   * method.  Chained operators will place the subsequent operators within the same DBObject,
   * e.g. <code>"fooDate" \$lte yesterday \$gte tomorrow</code> maps to a Mongo query of:
   * <code>{"fooDate": {"\$lte": <yesterday>, "\$gte": <tomorrow>}}</code>
   *
   * @param left A string which should be the field name, the left hand of the query
   * @return Tuple2[String, DBObject] A tuple containing the field name and the mapped operator value, suitable for instantiating a Map
   */
  implicit def mongoQueryStatements(left: String): FluidQueryOperators = new {
    val field = left
  } with dsl.FluidQueryOperators // with dsl.aggregation.ProjectSubOperators

  /**
   * Implicit extension methods for Tuple2[String, DBObject] values
   * to add Mongo's query operators, minimizing the need to write long series'
   * of nested maps.
   *
   * Mixes in the QueryOperators defined in the QueryOperators mixin.
   * The NestedQuery implicits allows you to call chained operators on the return value of the
   * base String method method.  Chained operators will place the subsequent operators within the same DBObject,
   * e.g. <code>"fooDate" \$lte yesterday \$gte tomorrow</code> maps to a Mongo query of:
   * <code>{"fooDate": {"\$lte": <yesterday>, "\$gte": <tomorrow>}}</code>
   *
   * @param left A string which should be the field name, the left hand of the query
   * @return Tuple2[String, DBObject] A tuple containing the field name and the mapped operator value, suitable for
   *         instantiating a Map
   */
  implicit def mongoNestedDBObjectQueryStatements(nested: DBObject with dsl.QueryExpressionObject): ValueTestFluidQueryOperators = {
    new {
      val field = nested.field
    } with dsl.ValueTestFluidQueryOperators {
      dbObj = nested.getAs[DBObject](nested.field) // TODO - shore the safety of this up
    }
  }

  implicit def tupleToGeoCoords[A: ValidNumericType: Manifest, B: ValidNumericType: Manifest](coords: (A, B)): GeoCoords[A, B] =
    dsl.GeoCoords(coords._1, coords._2)

}

trait ChainedOperator {
  def field: String

  protected var dbObj: Option[DBObject] = None
}

object Implicits extends query.Implicits with commons.Implicits

/*
 *object Imports extends query.Imports with commons.Imports
 */
object Imports extends query.Imports with commons.Imports

object BaseImports extends query.BaseImports with commons.BaseImports

object TypeImports extends query.TypeImports with commons.TypeImports

trait Imports extends query.BaseImports
  with query.TypeImports
  with query.Implicits
  with dsl.FluidQueryBarewordOps
  with ValidBarewordExpressionArgTypeHolder
  with ValidDateTypeHolder
  with ValidNumericTypeHolder
  with ValidDateOrNumericTypeHolder

trait BaseImports {
  val GeoCoords = com.mongodb.casbah.query.dsl.GeoCoords
  val AsQueryParam = query.AsQueryParam
}

trait TypeImports {
  type GeoCoords = com.mongodb.casbah.query.dsl.GeoCoords[_, _]
  type ValidNumericType[T] = query.ValidNumericType[T]
  type ValidDateType[T] = query.ValidDateType[T]
  type ValidDateOrNumericType[T] = query.ValidDateOrNumericType[T]
  type ValidBarewordExpressionArgType[T] = query.ValidBarewordExpressionArgType[T]
  type AsQueryParam[T] = query.AsQueryParam[T]
  // type AggregationPipeline = dsl.aggregation.AggregationPipeline
}

trait ValidNumericType[T]

trait ValidDateType[T]

trait ValidDateOrNumericType[T]

trait ValidDateTypeHolder {

  import com.mongodb.casbah.query.ValidTypes.{ JDKDateOk, JodaDateTimeOk }

  implicit object JDKDateOk extends JDKDateOk

  implicit object JodaDateTimeOk extends JodaDateTimeOk

}

object ValidTypes {

  trait JDKDateOk extends ValidDateType[java.util.Date]

  trait JodaDateTimeOk extends ValidDateType[org.joda.time.DateTime]

  trait KVPair[A] extends ValidBarewordExpressionArgType[(String, A)] {
    def toDBObject(arg: (String, A)): DBObject = MongoDBObject(arg)
  }

  // Valid Bareword Query Expression entries
  trait CoreOperatorResultObj extends ValidBarewordExpressionArgType[DBObject with dsl.QueryExpressionObject] {
    def toDBObject(arg: DBObject with dsl.QueryExpressionObject): DBObject = arg
  }

  trait ConcreteDBObject extends ValidBarewordExpressionArgType[DBObject] {
    def toDBObject(arg: DBObject): DBObject = arg
  }

  // ValidNumericTypes
  trait IntOk extends ValidNumericType[Int] with Numeric.IntIsIntegral with Ordering.IntOrdering

  trait ShortOk extends ValidNumericType[Short] with Numeric.ShortIsIntegral with Ordering.ShortOrdering

  trait ByteOk extends ValidNumericType[Byte] with Numeric.ByteIsIntegral with Ordering.ByteOrdering

  trait LongOk extends ValidNumericType[Long] with Numeric.LongIsIntegral with Ordering.LongOrdering

  trait FloatOk extends ValidNumericType[Float] with Numeric.FloatIsFractional with Ordering.Float.TotalOrdering

  trait DoubleOk extends ValidNumericType[Double] with Numeric.DoubleIsFractional with Ordering.Double.TotalOrdering

}

trait ValidBarewordExpressionArgType[T] {
  def toDBObject(arg: T): DBObject
}

trait ValidBarewordExpressionArgTypeHolder {

  import com.mongodb.casbah.query.ValidTypes.{ ConcreteDBObject, CoreOperatorResultObj, KVPair }

  implicit def kvPairOk[A]: KVPair[A] = new KVPair[A] {}

  implicit object ConcreteDBObjectOk extends ConcreteDBObject

  implicit object CoreOperatorResultObjOk extends CoreOperatorResultObj

}

@annotation.implicitNotFound("${A} is not a valid query parameter.")
trait AsQueryParam[A] {
  def asQueryParam(a: A): Any
}

// scalastyle:off line.size.limit
// scalastyle:off public.methods.have.type
object AsQueryParam {
  def apply[A](implicit a: AsQueryParam[A]) = a

  private def as[A](f: A => Any): AsQueryParam[A] = new AsQueryParam[A] {
    def asQueryParam(a: A) = f(a)
  }

  implicit val string = as[String](identity)
  implicit val none = as[None.type](identity)
  implicit def dbObject[A <: DBObject] = as[A](identity)

  implicit val dbRef = as[DBRef](identity)
  implicit val objectId = as[ObjectId](identity)
  implicit val boolean = as[Boolean](identity)
  implicit val regex = as[scala.util.matching.Regex](identity)
  implicit val bsonTimestamp = as[BSONTimestamp](identity)

  implicit def array[A] = as[Array[A]](_.toList)

  implicit def iterable[A <: Iterable[_]] = as[A](_.toList)

  implicit def dateOrNumeric[A: ValidDateOrNumericType] = as[A](identity)

  implicit def tuple1[A1] = as[Tuple1[A1]](_.productIterator.toList)

  implicit def tuple2[A1, A2] = as[(A1, A2)](_.productIterator.toList)

  implicit def tuple3[A1, A2, A3] = as[(A1, A2, A3)](_.productIterator.toList)

  implicit def tuple4[A1, A2, A3, A4] = as[(A1, A2, A3, A4)](_.productIterator.toList)

  implicit def tuple5[A1, A2, A3, A4, A5] = as[(A1, A2, A3, A4, A5)](_.productIterator.toList)

  implicit def tuple6[A1, A2, A3, A4, A5, A6] = as[(A1, A2, A3, A4, A5, A6)](_.productIterator.toList)

  implicit def tuple7[A1, A2, A3, A4, A5, A6, A7] = as[(A1, A2, A3, A4, A5, A6, A7)](_.productIterator.toList)

  implicit def tuple8[A1, A2, A3, A4, A5, A6, A7, A8] = as[(A1, A2, A3, A4, A5, A6, A7, A8)](_.productIterator.toList)

  implicit def tuple9[A1, A2, A3, A4, A5, A6, A7, A8, A9] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9)](_.productIterator.toList)

  implicit def tuple10[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)](_.productIterator.toList)

  implicit def tuple11[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)](_.productIterator.toList)

  implicit def tuple12[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)](_.productIterator.toList)

  implicit def tuple13[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)](_.productIterator.toList)

  implicit def tuple14[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)](_.productIterator.toList)

  implicit def tuple15[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)](_.productIterator.toList)

  implicit def tuple16[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)](_.productIterator.toList)

  implicit def tuple17[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)](_.productIterator.toList)

  implicit def tuple18[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)](_.productIterator.toList)

  implicit def tuple19[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)](_.productIterator.toList)

  implicit def tuple20[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)](_.productIterator.toList)

  implicit def tuple21[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)](_.productIterator.toList)

  implicit def tuple22[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22] = as[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22)](_.productIterator.toList)
}
// scalastyle:on line.size.limit
// scalastyle:on public.methods.have.type

trait ValidNumericTypeHolder {

  import com.mongodb.casbah.query.ValidTypes.{ ByteOk, DoubleOk, FloatOk, IntOk, LongOk, ShortOk }

  implicit object IntOk extends IntOk

  implicit object ShortOk extends ShortOk

  implicit object ByteOk extends ByteOk

  implicit object LongOk extends LongOk

  implicit object FloatOk extends FloatOk

  implicit object DoubleOk extends DoubleOk

}

trait ValidDateOrNumericTypeHolder {

  implicit object JDKDateDoNOk extends ValidDateOrNumericType[java.util.Date]

  implicit object JodaDateTimeDoNOk extends ValidDateOrNumericType[org.joda.time.DateTime]

  implicit object IntDoNOk extends ValidDateOrNumericType[Int]

  implicit object ShortDoNOk extends ValidDateOrNumericType[Short]

  implicit object ByteDoNOk extends ValidDateOrNumericType[Byte]

  implicit object LongDoNOk extends ValidDateOrNumericType[Long]

  implicit object FloatDoNOk extends ValidDateOrNumericType[Float]

  implicit object DoubleDoNOk extends ValidDateOrNumericType[Double]

}
