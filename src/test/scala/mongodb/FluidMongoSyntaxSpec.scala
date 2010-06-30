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

import util.Logging

import Imports._

import scala.collection.JavaConversions._
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers

class FluidMongoSyntaxSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers with Logging {
  feature("DBObject related syntax conversions.") {
    // Disabled automatic conversion - this causes people's code to catch fire and swallow small children
    scenario("Products/Tuples can be cast to Mongo DBObjects.") {
      given("A tuple of tuples (a quirk of this syntax is that direct casting to DBObject doesn't work).")
      val x = ("foo" -> "bar", "n" -> 1)
      assert(!x.isInstanceOf[DBObject])
      when("The tuple is recast to DBObject")
      val dbObj = x.asDBObject
      then("The conversion succeeds.")
      assert(dbObj.isInstanceOf[DBObject])
      and("Conforms to the DBObject spec.")
      assert(dbObj.get("foo").equals("bar"))
    }
    scenario("Maps of [String, Any] can be converted to DBObjects."){
      given("A Map of data")
      val x = Map("foo" -> "bar", "n" -> 1)
      assert(!x.isInstanceOf[DBObject])
      when("The Map can be recast to DBObject")
      val dbObj: DBObject = x
      then("The conversion succeeds.")
      assert(dbObj.isInstanceOf[DBObject])
      and("Conforms to the DBObject spec.")
      assert(dbObj.get("foo").equals("bar"))
      and("It can also be explicitly requested as a DBObject")
      val explicit = x.asDBObject
      assert(explicit.isInstanceOf[DBObject])
      assert(explicit.get("foo").equals("bar"))
    }
    /*scenario("Lists and Array type operations work correctly.") {
      val mongoDB = MongoConnection()("test") 
      mongoDB.dropDatabase
      given("An Object with an Array")
      val x = ("foo" -> "bar", "n" -> Set(2, 5, 8, 12, 24, 452, 9, 12, 13))
      when("Serialized")
      mongoDB("arrayTest").insert(x.asDBObject)
      then("It doesn't fail")
      and("Returns correctly.")
    }*/
  }
  feature("Scala Style DBObject builder & factory for DBObject works correctly.") {
    scenario("Factory apply is called with a new set of parameters") {
      given("A series of parameters") 
      // You must explicitly cast the result to a DBObject to receive it as such; you can also request it 'asDBObject'
      val newObj: DBObject = MongoDBObject("foo" -> "bar", "x" -> "y", "pie" -> 3.14, "spam" -> "eggs")
      then(" A proper Mongo DBObject is returned")
      log.info("newObj: %s [%s]", newObj, newObj.getClass)
      assert(newObj.isInstanceOf[com.mongodb.DBObject]) // matches mongodb dbobj and not just our loca import alias
      and("A proper DB Spec is met")
      newObj should be (com.mongodb.BasicDBObjectBuilder.start("foo", "bar").add("x", "y").add("pie", 3.14).add("spam", "eggs").get)
    }
    scenario(" A builder works as well.") {
      given("A new builder") 
      val builder = MongoDBObject.newBuilder
      when("The result is invoked after adding data")
      // You must explicitly cast the result to a DBObject to receive it as such; you can also request it 'asDBObject'
      //("foo" -> "bar", "x" -> "y", "pie" -> 3.14, "spam" -> "eggs")
      builder += "foo" -> "bar"
      builder += "x" -> "y"
      builder += ("pie" -> 3.14)
      builder += ("spam" -> "eggs", "mmm" -> "bacon")
      val newObj = builder.result.asDBObject
      log.info("newObj: %s [%s]", newObj, newObj.getClass)
      assert(newObj.isInstanceOf[com.mongodb.DBObject]) // matches mongodb dbobj and not just our loca import alias
      and("A proper DB Spec is met")
      newObj should be (com.mongodb.BasicDBObjectBuilder.start("foo", "bar").add("x", "y").add("pie", 3.14).add("spam", "eggs").add("mmm", "bacon").get)

    }
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
      assert(in.toString != null) // Test to verify mongo's toString serialization doesn't choke
      // @TODO Figure out the proper type comparison statement. I know it's an array but the Scala conversion muddies it
      //assert(in._2.get("$in").asInstanceOf[RichSSeq[_]] == Seq(1, 8, 12).asJava)
      given("A field, <NOT IN OPERATOR>, target array")
      val nin = "foo" $nin (1, 8, 12)
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(nin._1 == "foo")
      assert(nin.toString != null) // Test to verify mongo's toString serialization doesn't choke
      // @TODO Figure out the proper type comparison statement. I know it's an array but the Scala conversion muddies it
      //assert(in._2.get("$in").asInstanceOf[RichSSeq[_]] == Seq(1, 8, 12).asJava)
      given("A field, <ALL OPERATOR>, target array")
      val all = "foo" $all (1, 8, 12)
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(all._1 == "foo")
      assert(all.toString != null) // Test to verify mongo's toString serialization doesn't choke
      // @TODO Figure out the proper type comparison statement. I know it's an array but the Scala conversion muddies it
      //assert(in._2.get("$in").asInstanceOf[RichSSeq[_]] == Seq(1, 8, 12).asJava)
      given("A field, <WHERE OPERATOR>, target statement")
      val where = "foo" $where "function (x) { x.foo = 5; }"
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(where == ("foo" -> new BasicDBObject("$where", "function (x) { x.foo = 5; }")))

      given("A field, <NOT OPERATOR>, target statement, to negate statements")
      // You must anchor the $not to the field.. but after that it will pickup any query syntax on itself.
      val not = "foo".$not $gt 5 
      then("The implicit conversions provide a properly tiered DBObject matching the expected query.")
      assert(not == ("foo" -> new BasicDBObject("$not", new BasicDBObject("$gt", 5))))
      //log.info("Placeheld? %s containing %s - %s", no, n.get("foo").asInstanceOf[DBObject].get("$not").getClass, n.get("foo").asInstanceOf[DBObject].get("$not"))
      and("Regular Expression matching functions as well.")

      given("A <SET OPERATOR>, a field & value")
      then("A proper DBObject is returned with the expected data")
      val set = $set ("foo" -> 5)
      assert(set == Map("$set" -> Map("foo" -> 5).asDBObject).asDBObject)
      and("Setting multiple values functions as well")
      val setMulti = $set ("foo" -> 5, "bar" -> "N", "spam" -> "eggs")
      setMulti should be (Map("$set" -> Map("foo" -> 5, "bar" -> "N", "spam" -> "eggs").asDBObject).asDBObject)

      given("An <UNSET OPERATOR>, and a field")
      then("A proper DBObject is returned, with an appropriate dataset")
      val unset = $unset ("foo")
      unset should be (Map("$unset" -> Map("foo" -> 1).asDBObject).asDBObject)
      and("Setting multiple values functions as well")
      val unsetMulti = $unset ("foo", "bar", "baz", "spam", "eggs")
      unsetMulti should be (Map("$unset" -> Map("foo" -> 1, "bar" -> 1, "baz" -> 1, "spam" -> 1, "eggs" -> 1).asDBObject).asDBObject)

      given("An <INCREMENT OPERATOR> and a field + value")
      then("A proper increment DBObject is returned with an appropriate dataset")
      val inc = $inc ("foo" -> 1)
      inc should be (Map("$inc" -> Map("foo" -> 1).asDBObject).asDBObject)
      and("Multiple fields works")
      val incMulti = $inc ("foo" -> 5.0, "bar" -> 1.2, "spam" -> 212.0)
      incMulti should be (Map("$inc" ->  Map("foo" -> 5, "bar" -> 1.2, "spam" -> 212.0).asDBObject).asDBObject)
      //TODO - Finish testing array operators
      given("A PushAll Operator")
      then("It should return a structured object of the appropriate type.")
      /*val pushAll = $pushAll ("foo" -> Array(5, 10, 12, "foo"))
      log.info("Push All: %s", pushAll.get("$pushAll").asInstanceOf[DBObject].get("foo").getClass)
      pushAll should be (Map("$pushAll" -> Map("foo" -> Array(5, 10, 12, "foo")).asDBObject).asDBObject)*/

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
      log.info("Mod: %s", mod)
      then("The implicit conversions provide a map-entry formatted Tuple-set matching the query.")
      assert(mod == ("foo" -> new BasicDBObject("$exists", true).append("$mod", 2)))
      given("A field, <NOT OPERATOR>, target statement, to negate statements")
    }

  }
}
