/**
 * Copyright (c) 2010 - 2012 10gen, Inc. <http://10gen.com>
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

package com.mongodb.casbah.test.core

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.conversions.scala._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification

import com.github.nscala_time.time.Imports._

import com.mongodb.casbah.Imports._


class CoreWrappersSpec extends CasbahMutableSpecification {

  "Casbah behavior between Scala and Java versions of Objects" should {

    "provide working .asScala methods on the Java version of the objects" in {

      val javaConn = new com.mongodb.MongoClient() // Java connection

      "Connection objects" in {

        val scalaConn = javaConn.asScala

        scalaConn.underlying must beEqualTo(javaConn)
      }

      val javaDb = javaConn.getDB("test")

      "DB objects" in {

        val scalaDb = javaDb.asScala

        scalaDb.underlying must beEqualTo(javaDb)
      }

      val javaCollection = javaDb.getCollection("test")

      "Collection objects" in {

        val scalaCollection = javaCollection.asScala


        scalaCollection.underlying must beEqualTo(javaCollection)
      }
    }

    "be directly instantiable, with working apply methods" in {
      var conn: MongoClient = MongoClient()
      var db: MongoDB = conn("test")
      var coll: MongoCollection = db("collection.in")

      "MongoClient" in {
        "direct instantiation" in {
          conn.underlying must haveClass[com.mongodb.MongoClient]
        }

        "the apply method works" in {
          db.underlying must haveSuperclass[com.mongodb.DB]

        }
      }

      "MongoDB" in {
        "has a working apply method" in {
          coll.underlying must beAnInstanceOf[com.mongodb.DBCollection]
        }
      }

    }

    "allow indexes to work as expected" in {
      val db = MongoClient()("casbahTest")

      val coll = db("indexTest")
      coll.drop()

      coll.insert(MongoDBObject("foo" -> "bar"))
      coll.indexInfo.length must beEqualTo(1)

      coll.ensureIndex(MongoDBObject("uid"->1), "user_index", true)
      coll.indexInfo.length must beEqualTo(2)

      coll.indexInfo(1)("key") == MongoDBObject("uid" -> 1)
    }

    "Renaming a collection successfully tracks the rename in MongoCollection" in {
      val db = MongoClient()("casbahTest")
      db("collection").drop()
      val coll = db("collectoin")
      coll.drop()
      coll.insert(MongoDBObject("foo" -> "bar"))
      coll must beAnInstanceOf[com.mongodb.casbah.MongoCollection]
      coll.name must beEqualTo("collectoin")

      val newColl = coll.rename("collection")
      newColl must beAnInstanceOf[com.mongodb.casbah.MongoCollection]
      newColl.name must beEqualTo("collection")

      // no mutability in the old collection
      coll.name must beEqualTo("collectoin")
      // collection should be gone so rename fails
      newColl.rename("collection") must throwA[MongoException]

    }
  }

  "findOne operations" should {
    val db = MongoClient()("casbahTest")

    "Not fail as reported by Max Afonov in SCALA-11" in {
      val coll = db("brand_new_coll_%d".format(System.currentTimeMillis))

      coll.insert(MongoDBObject("foo" -> "bar"))
      val basicFind = coll.find(MongoDBObject("foo" -> "bar"))

      basicFind.size must beEqualTo(1)

      val findOne = coll.findOne()

      findOne must beSome

      val findOneMatch = coll.findOne(MongoDBObject("foo" -> "bar"))

      findOneMatch must beSome

    }
  }

  "Cursor Operations" should {
    import scala.util.Random

    val db = MongoClient()("casbahTest")
    val coll = db("test_coll_%d".format(System.currentTimeMillis))

    for (i <- 1 to 100)
      coll += MongoDBObject("foo" -> "bar", "x" -> Random.nextDouble())

    val first5 = coll.find(MongoDBObject("foo" -> "bar")) limit 5

    "Behave in chains" in {
/*
 *      "For loops for idiomatic cleanness" in {
 *
 *        // todo - add limit, skip etc on COLLECTION for cleaner chains like this?
 *        val items = for (x <- coll.find(MongoDBObject("foo" -> "bar")) skip 5 limit 20) yield x
 *
 *        items must haveSize(20)
 *        // TODO - Matchers that support freaking cursors, etc
 *        [>items must haveSameElementsAs(first5).not<]
 *      }
 */

      "Chain operations must return the proper *subtype*" in {
        val cur = coll.find(MongoDBObject("foo" -> "bar")) skip 5
        cur must beAnInstanceOf[MongoCursor]

        val cur2 = coll.find(MongoDBObject("foo" -> "bar")) limit 25 skip 12
        cur2 must beAnInstanceOf[MongoCursor]

      }
    }
  }

  "Distinct operations" should {
      val db = MongoClient()("casbahTest")
      val coll = db("distinct")
      coll.drop()

      for (i <- 1 to 99)
        coll += MongoDBObject("_id" -> i, "x" -> i % 10)

      "except just a key" in {
        val l = coll.distinct( "x" )
        l.size must beEqualTo(10)
      }

      "except key and query" in {
        val l = coll.distinct( "x" , "_id" $gt 95 )
        l.size must beEqualTo(4)
      }

      "except key and readPref" in {
        val l = coll.distinct( "x", readPrefs=ReadPreference.Primary )
        l.size must beEqualTo(10)
      }

      "except key, query and readPref" in {
        val l = coll.distinct( "x" , "_id" $gt 95, ReadPreference.Primary )
        l.size must beEqualTo(4)
      }

  }

}

