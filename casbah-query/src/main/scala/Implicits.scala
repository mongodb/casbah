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



trait Implicits {// extends FluidQueryBarewordOps {

  /**
   * Implicit extension methods for String values (e.g. a field name)
   * to add Mongo's query operators, minimizing the need to write long series'
   * of nested maps.
   *
   * Mixes in the QueryOperators defined in the QueryOperators mixin.
   * The NestedQuery implicit [Defined below] allows you to call chained operators on the return value of this
   * method.  Chained operators will place the subsequent operators within the same DBObject,
   * e.g. <code>"fooDate" $lte yesterday $gte tomorrow</code> maps to a Mongo query of:
   * <code>{"fooDate": {"$lte": <yesterday>, "$gte": <tomorrow>}}</code>
   * 
   * @param left A string which should be the field name, the left hand of the query
   * @return Tuple2[String, DBObject] A tuple containing the field name and the mapped operator value, suitable for instantiating a Map
   */
  implicit def mongoQueryStatements(left: String) = new {
    val field = left
  } with dsl.FluidQueryOperators

  /**
   * Implicit extension methods for Tuple2[String, DBObject] values
   * to add Mongo's query operators, minimizing the need to write long series'
   * of nested maps.
   *
   * Mixes in the QueryOperators defined in the QueryOperators mixin.
   * The NestedQuery implicits allows you to call chained operators on the return value of the
   * base String method method.  Chained operators will place the subsequent operators within the same DBObject,
   * e.g. <code>"fooDate" $lte yesterday $gte tomorrow</code> maps to a Mongo query of:
   * <code>{"fooDate": {"$lte": <yesterday>, "$gte": <tomorrow>}}</code>
   *
   * @param left A string which should be the field name, the left hand of the query
   * @return Tuple2[String, DBObject] A tuple containing the field name and the mapped operator value, suitable for instantiating a Map
   */
  implicit def mongoNestedDBObjectQueryStatements(nested: DBObject with dsl.DSLDBObject) = {
    new {
      val field = nested.field
    } with dsl.ValueTestFluidQueryOperators {
      dbObj = nested.getAs[DBObject](nested.field) // TODO - shore the safety of this up
    }
  }

  implicit def tupleToGeoCoords[A: ValidNumericType: Manifest, B: ValidNumericType: Manifest](coords: (A, B)) = dsl.GeoCoords(coords._1, coords._2)

}

/*@deprecated("The Imports._ semantic has been deprecated.  Please import 'com.mongodb.casbah.query._' instead.")
object Imports extends query.Imports with commons.Imports*/
trait Imports extends dsl.FluidQueryBarewordOps with query.BaseImports with query.TypeImports with query.Implicits with commons.Imports with commons.Exports with ValidDateOrNumericTypeHolder

object `package` extends Imports // query.dsl.FluidQueryBarewordOps with commons.Exports with query.BaseImports with query.TypeImports with query.Implicits with commons.Imports with ValidDateOrNumericTypeHolder
trait BaseImports {
  val GeoCoords = com.mongodb.casbah.query.dsl.GeoCoords
}

trait TypeImports {
  type GeoCoords = com.mongodb.casbah.query.dsl.GeoCoords[_, _]
}

trait Exports {
  type ValidNumericType[T] = query.ValidNumericType[T]
  type ValidDateType[T] = query.ValidDateType[T]
  type ValidDateOrNumericType[T] = query.ValidDateOrNumericType[T]
}

object ValidTypes {
  trait JDKDateOk extends ValidDateType[java.util.Date]
  trait JodaDateTimeOk extends ValidDateOrNumericType[org.joda.time.DateTime]

  trait BigIntOk extends ValidNumericType[BigInt] with Numeric.BigIntIsIntegral with Ordering.BigIntOrdering
  trait IntOk extends ValidNumericType[Int] with Numeric.IntIsIntegral with Ordering.IntOrdering
  trait ShortOk extends ValidNumericType[Short] with Numeric.ShortIsIntegral with Ordering.ShortOrdering
  trait ByteOk extends ValidNumericType[Byte] with Numeric.ByteIsIntegral with Ordering.ByteOrdering
  trait LongOk extends ValidNumericType[Long] with Numeric.LongIsIntegral with Ordering.LongOrdering
  trait FloatOk extends ValidNumericType[Float] with Numeric.FloatIsFractional with Ordering.FloatOrdering
  trait BigDecimalOk extends ValidNumericType[BigDecimal] with Numeric.BigDecimalIsFractional with Ordering.BigDecimalOrdering
  trait DoubleOk extends ValidNumericType[Double] with Numeric.DoubleIsFractional with Ordering.DoubleOrdering
}


trait ValidNumericType[T]

trait ValidDateType[T]

trait ValidDateOrNumericType[T]

trait ValidDateTypeHolder {
  import com.mongodb.casbah.query.ValidTypes.{JDKDateOk, JodaDateTimeOk}
  implicit object JDKDateOk extends JDKDateOk
  implicit object JodaDateTimeOk extends JodaDateTimeOk
}

trait ValidNumericTypeHolder {
  import com.mongodb.casbah.query.ValidTypes.{BigIntOk, IntOk, ShortOk, ByteOk, LongOk,
                                              FloatOk, BigDecimalOk, DoubleOk}
  implicit object BigIntOk extends BigIntOk
  implicit object IntOk extends IntOk
  implicit object ShortOk extends ShortOk
  implicit object ByteOk extends ByteOk
  implicit object LongOk extends LongOk
  implicit object FloatOk extends FloatOk
  implicit object BigDecimalOk extends BigDecimalOk
  implicit object DoubleOk extends DoubleOk
}

trait ValidDateOrNumericTypeHolder {
  import com.mongodb.casbah.query.ValidTypes.{JDKDateOk, BigIntOk, IntOk, ShortOk, ByteOk,
                                              LongOk, FloatOk, BigDecimalOk, DoubleOk}
  implicit object JDKDateDoNOk extends JDKDateOk with ValidDateOrNumericType[java.util.Date]
  implicit object JodaDateTimeDoNOk extends JDKDateOk with ValidDateOrNumericType[org.joda.time.DateTime]
  implicit object BigIntDoNOk extends BigIntOk with ValidDateOrNumericType[BigInt]
  implicit object IntDoNOk extends IntOk with ValidDateOrNumericType[Int]
  implicit object ShortDoNOk extends ShortOk with ValidDateOrNumericType[Short]
  implicit object ByteDoNOk extends ByteOk with ValidDateOrNumericType[Byte]
  implicit object LongDoNOk extends LongOk with ValidDateOrNumericType[Long]
  implicit object FloatDoNOk extends FloatOk with ValidDateOrNumericType[Float]
  implicit object BigDecimalDoNOk extends BigDecimalOk with ValidDateOrNumericType[BigDecimal]
  implicit object DoubleDoNOk extends DoubleOk with ValidDateOrNumericType[Double]
}

// vim: set ts=2 sw=2 sts=2 et:
