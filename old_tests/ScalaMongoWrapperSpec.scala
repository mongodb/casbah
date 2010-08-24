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
 */

package com.novus.casbah
package mongodb
package test

import util.Logging

import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers

import net.lag.configgy.Configgy
import net.lag.logging.Logger

import com.novus.casbah.mongodb.Imports._

class MongoWrapperSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers with Logging {
    Configgy.configure("src/test/resources/casbah.config")
    feature("The implicit extension methods allow for receiving wrapped versions of the Mongo Java objects.") {

      info("the Scala wrappers should provide .asScala methods on each of the Mongo Java connection-related objects.")

      val conn = new com.mongodb.Mongo()
      scenario("A Mongo connection object can be converted into a MongoConnection wrapper instance.") {
        given("A Mongo object connected to the default [localhost]")
        assert(conn != null)
        when("The asScala extension method is invoked.")
        val scalaConn = conn.asScala
        then("A MongoConnectionWrapper wrapper instance is returned.")
        assert(scalaConn != null)
        assert(scalaConn.isInstanceOf[MongoConnection])
        and("The underlying connection is the Java mongo connection object.")
        assert(scalaConn.underlying == conn)
      }

      val db = conn.getDB("test")
      scenario("A DB (database handle) object can be converted into a MongoDB wrapper instance.") {
        given("A Mongo DB handle upon the test db")
        assert(db != null)
        when("The asScala extension method is invoked.")
        val scalaDB = db.asScala
        then("A MongoDB wrapper instance is returned.")
        assert(scalaDB != null)
        assert(scalaDB.isInstanceOf[MongoDB])
        and("The underlying db is the Java mongo db object.")
        assert(scalaDB.underlying == db)
      }

      val coll = db.getCollection("foo")
      scenario("A DBCollection object can be converted into a MongoCollection wrapper instance.") {
        given("A Mongo DBCollection handle upon the test.foo collection")
        assert(coll != null)
        when("The asScala extension method is invoked.")
        val scalaColl = coll.asScala
        then("A MongoCollection wrapper instance is returned.")
        assert(scalaColl != null)
        assert(scalaColl.isInstanceOf[MongoCollection])
        and("The underlying collection is the Java mongo collection object.")
        assert(scalaColl.underlying != null)
        assert(scalaColl.underlying == coll)
      }
    }

    feature("The Scala wrapper connection can be instantiated directly, and the apply methods work for dbs/collections.") {
      val conn = MongoConnection()
      scenario("A MongoConnection object can be directly instantiated.") {
        given("A connected instance.")
        assert(conn != null)
        assert(conn.isInstanceOf[MongoConnection])
        then("The connected instance's underlying connection is the Java mongo connection object.")
        assert(conn.underlying != null)
        assert(conn.underlying.isInstanceOf[com.mongodb.Mongo])
      }
      scenario("The apply method can be invoked on Connections instead of getDB.") {
        given("A connected instance.")
        assert(conn != null)
        when("A database name is passed via apply()")
        val db = conn("test")
        then("A valid instance of MongoDB is returned.")
        assert(db != null)
        assert(db.isInstanceOf[MongoDB])
        and("It is connected to the proper database.")
        assert(db.underlying != null)
        assert(db.getName == "test")
        and("The underlying database is the Java mongo database object.")
        assert(db.underlying.isInstanceOf[com.mongodb.DB])
      }
      scenario("The apply method can be invoked upon DBs instead of getCollection.") {
        given("A connected MongoDB Instance.")
        assert(conn != null)
        val db = conn("test")
        when("A collection name is passed via apply()")
        val coll = db("foo")
        then("A valid instance of the [non-genericized] MongoCollection is returned.")
        assert(coll != null)
        assert(coll.isInstanceOf[MongoCollection])
      }
    }
    feature("Tutorial related edge cases function properly...") {
      val mongoColl = MongoConnection()("casbah_test")("test_data")
      scenario("$Exists tests are working") {
        when("A explicit cast of fluid text is done...")
        val q: DBObject = "email" $exists true
        then("The expected object is assembled")
        log.info("Q: %s", q)
        q should be (MongoDBObject("email" -> MongoDBObject("$exists" -> true).asDBObject).asDBObject) 
      }
      scenario("Tracking down doc bugs in single test") {
        val q = "email" $exists true
        // q: (String, com.mongodb.DBObject) = 
        // (email,{ "$exists" : true})
        val users = for (x <- mongoColl.find(q)) yield x
        log.info("users: %s", users.size)
      }
      scenario("Implicit conversion can take place when passing into something that needs a DBObject") {
        when("Finding off a fluid query")
        val q = "email" $exists true
        // q: (String, com.mongodb.DBObject) = 
        // (email,{ "$exists" : true})
        val n = for (x <- mongoColl.find(q)) yield x
        log.info("N: %s", n.next)
        then("Nothing blew up") // yes ,this test is poor.
        and("Another quick test works...")
        val q2  = MongoDBObject.empty
        val fields = MongoDBObject("user" -> 1)
        val z = for (x <- mongoColl.find(q2, fields)) yield x
        log.info("Z: %s / %s", z, z.next)
      }
    }
}
