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

import scalaj.collection.Imports._

trait Implicits extends FluidQueryBarewordOps {

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
  } with FluidQueryOperators


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
  implicit def mongoNestedDBObjectQueryStatements(nested: DBObject with DSLDBObject) = { 
    new {
      val field = nested.field
    } with ValueTestFluidQueryOperators { 
      dbObj = nested.getAs[DBObject](nested.field)  // TODO - shore the safety of this up
    }
  }

  implicit def tupleToGeoCoords[A : ValidNumericType : Manifest, B : ValidNumericType : Manifest](coords: (A, B)) = GeoCoords(coords._1, coords._2)
  



}

object Implicits extends query.Implicits with commons.Implicits 
object Imports extends query.Imports with commons.Imports
object BaseImports extends query.BaseImports with commons.BaseImports
object TypeImports extends query.TypeImports with commons.TypeImports

trait Imports extends query.BaseImports with query.TypeImports with query.Implicits with ValidDateOrNumericTypeHolder

trait BaseImports

trait TypeImports { 
  type GeoCoords = com.mongodb.casbah.query.GeoCoords[_,_]
  type ValidNumericType[T] = query.ValidNumericType[T]
  type ValidDateType[T] = query.ValidDateType[T]
  type ValidDateOrNumericType[T] = query.ValidDateOrNumericType[T]
}

trait ValidNumericType[T]

trait ValidDateType[T] 

trait ValidDateOrNumericType[T]

trait ValidDateTypeHolder {
  trait JDKDateOk extends ValidDateType[java.util.Date]
  implicit object JDKDateOk extends JDKDateOk
  trait JodaDateTimeOk extends ValidDateOrNumericType[org.joda.time.DateTime]
  implicit object JodaDateTimeOk extends JodaDateTimeOk
}

trait ValidNumericTypeHolder {
  import Numeric._
  trait BigIntOk extends ValidNumericType[BigInt] with BigIntIsIntegral with Ordering.BigIntOrdering
  implicit object BigIntOk extends BigIntOk
  trait IntOk extends ValidNumericType[Int] with IntIsIntegral with Ordering.IntOrdering
  implicit object IntOk extends IntOk
  trait ShortOk extends ValidNumericType[Short] with ShortIsIntegral with Ordering.ShortOrdering
  implicit object ShortOk extends ShortOk
  trait ByteOk extends ValidNumericType[Byte] with ByteIsIntegral with Ordering.ByteOrdering
  implicit object ByteOk extends ByteOk
  trait LongOk extends ValidNumericType[Long] with LongIsIntegral with Ordering.LongOrdering
  implicit object LongOk extends LongOk
  trait FloatOk extends ValidNumericType[Float] with FloatIsFractional with Ordering.FloatOrdering
  implicit object FloatOk extends FloatOk
  trait BigDecimalOk extends ValidNumericType[BigDecimal] with BigDecimalIsFractional with Ordering.BigDecimalOrdering 
  implicit object BigDecimalOk extends BigDecimalOk
  trait DoubleOk extends ValidNumericType[Double] with DoubleIsFractional with Ordering.DoubleOrdering
  implicit object DoubleOk extends DoubleOk
}

trait ValidDateOrNumericTypeHolder extends ValidDateTypeHolder with ValidNumericTypeHolder {
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
