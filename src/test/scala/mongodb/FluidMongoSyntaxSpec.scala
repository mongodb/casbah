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

package com.novus.casbah
package mongodb
package test

import Implicits._

import com.mongodb._

import scalaj.collection.Imports._
import org.scalatest.{GivenWhenThen, FeatureSpec}

class FluidMongoSyntaxSpec extends FeatureSpec with GivenWhenThen {
  feature("DBObject related syntax conversions.") {
    scenario("Products/Tuples can be cast to Mongo DBObjects.") {
      given("A tuple of tuple2s (a quirk of this syntax is that direct casting to DBObject doesn't work).")
      val x = (("foo" -> "bar"), ("n" -> 1))
      assert(!x.isInstanceOf[DBObject])
      when("The tuple is recast to DBObject")
      val dbObj: DBObject = x
      then("The conversion succeeds.")
      assert(dbObj.isInstanceOf[DBObject])
      and("Conforms to the DBObject spec.")
      assert(dbObj.get("foo").equals("bar"))
    }
    scenario("Maps of [String, Any] can be converted to DBObjects."){}
  }
  feature("Basic mongo style query syntax operators function."){
    scenario("Conditional operators function against a string.") {
      given("A field, <LESS THAN OPERATOR>, target statement")
      val lt = "foo" $lt 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(lt == ("foo" -> new BasicDBObject("$lt", 5)))
      given("A field, <LESS THAN OR EQUAL OPERATOR>, target statement")
      val lte = "foo" $lte 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(lte == ("foo" -> new BasicDBObject("$lte", 5)))
      given("A field, <GREATER THAN OPERATOR>, target statement")
      val gt = "foo" $gt 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(gt == ("foo" -> new BasicDBObject("$gt", 5)))
      given("A field, <GREATER THAN OR EQUAL OPERATOR>, target statement")
      val gte = "foo" $gte 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(gte == ("foo" -> new BasicDBObject("$gte", 5)))
      given("A field, <NOT EQUAL TO OPERATOR>, target statement")
      val ne = "foo" $ne 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(ne == ("foo" -> new BasicDBObject("$ne", 5)))
      given("A field, <SIZE OPERATOR>, target statement")
      val size = "foo" $size 8
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(size == ("foo" -> new BasicDBObject("$size", 8)))
      given("A field, <MODULUS OPERATOR>, target statement")
      val mod = "foo" $mod 2
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(mod == ("foo" -> new BasicDBObject("$mod", 2)))
      given("A field, <EXISTS OPERATOR>, target statement")
      val exists = "foo" $exists false
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(exists == ("foo" -> new BasicDBObject("$exists", false)))
      given("A field, <IN OPERATOR>, target array")
      val in = "foo" $in (1, 8, 12)
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(in._1 == "foo")
      // @TODO Figure out the proper type comparison statement. I know it's an array but the Scala conversion muddies it
      //assert(in._2.get("$in").asInstanceOf[RichSSeq[_]] == Seq(1, 8, 12).asJava)
      given("A field, <NOT IN OPERATOR>, target array")
      val nin = "foo" $nin (1, 8, 12)
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(nin._1 == "foo")
      // @TODO Figure out the proper type comparison statement. I know it's an array but the Scala conversion muddies it
      //assert(in._2.get("$in").asInstanceOf[RichSSeq[_]] == Seq(1, 8, 12).asJava)
      given("A field, <ALL OPERATOR>, target array")
      val all = "foo" $all (1, 8, 12)
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(all._1 == "foo")
      // @TODO Figure out the proper type comparison statement. I know it's an array but the Scala conversion muddies it
      //assert(in._2.get("$in").asInstanceOf[RichSSeq[_]] == Seq(1, 8, 12).asJava)
      given("A field, <WHERE OPERATOR>, target statement")
      val where = "foo" $where "function (x) { x.foo = 5; }"
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(where == ("foo" -> new BasicDBObject("$where", "function (x) { x.foo = 5; }")))
    }
    scenario("Operator chaining works.") {
      given("A field, <LESS THAN OPERATOR>, target statement")
      val lt = "foo" $exists true $lt 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(lt == ("foo" -> new BasicDBObject("$exists", true).append("$lt", 5)))
      given("A field, <LESS THAN OR EQUAL OPERATOR>, target statement")
      val lte = "foo" $exists true $lte 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(lte == ("foo" -> new BasicDBObject("$exists", true).append("$lte", 5)))
      given("A field, <GREATER THAN OPERATOR>, target statement")
      val gt = "foo" $exists true $gt 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(gt == ("foo" -> new BasicDBObject("$exists", true).append("$gt", 5)))
      given("A field, <GREATER THAN OR EQUAL OPERATOR>, target statement")
      val gte = "foo" $exists true $gte 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(gte == ("foo" -> new BasicDBObject("$exists", true).append("$gte", 5)))
      given("A field, <NOT EQUAL TO OPERATOR>, target statement")
      val ne = "foo" $exists true $ne 5
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(ne == ("foo" -> new BasicDBObject("$exists", true).append("$ne", 5)))
      given("A field, <SIZE OPERATOR>, target statement")
      val size = "foo" $exists true $size 8
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(size == ("foo" -> new BasicDBObject("$exists", true).append("$size", 8)))
      given("A field, <MODULUS OPERATOR>, target statement")
      val mod = "foo" $exists true $mod 2
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(mod == ("foo" -> new BasicDBObject("$exists", true).append("$mod", 2)))
    }
  }
}
