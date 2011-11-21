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

package com.mongodb.casbah
package query
package light

trait Implicits {

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
  } with dsl.light.LightFluidQueryOperators

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
  implicit def mongoNestedDBObjectQueryStatements(nested: DBObject with dsl.QueryExpressionObject) = {
    new {
      val field = nested.field
    } with dsl.light.LightValueTestFluidQueryOperators {
      dbObj = nested.getAs[DBObject](nested.field) // TODO - shore the safety of this up
    }
  }

  implicit def tupleToGeoCoords[A: ValidNumericType: Manifest, B: ValidNumericType: Manifest](coords: (A, B)) = dsl.GeoCoords(coords._1, coords._2)

}

trait Imports extends dsl.light.LightFluidQueryBarewordOps with query.BaseImports with query.TypeImports with light.Implicits
                 with commons.Imports with commons.Exports with ValidBarewordExpressionArgTypeHolder with ValidDateTypeHolder
                 with ValidNumericTypeHolder with ValidDateOrNumericTypeHolder

object `package` extends Imports

trait BaseImports {
  val GeoCoords = com.mongodb.casbah.query.dsl.GeoCoords
}

trait TypeImports {
  type GeoCoords = com.mongodb.casbah.query.dsl.GeoCoords[_, _]
}


