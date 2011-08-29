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

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.util.Logging
import com.mongodb.casbah.commons.conversions.scala._

import org.scala_tools.time.Imports._
import com.mongodb.casbah.commons.test.CasbahSpecification

class CoreWrappersSpec extends CasbahSpecification {

  "Casbah behavior between Scala and Java versions of Objects" should {

    "provide working .asScala methods on the Java version of the objects" in {

      val javaConn = new com.mongodb.Mongo() // Java connection

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
      lazy val conn: MongoConnection = MongoConnection()
      lazy val db: MongoDB = conn("test")
      lazy val coll: MongoCollection = db("collection.in")

      "MongoConnection" in {
        "direct instantiation" in {
          conn.underlying must haveClass[com.mongodb.Mongo]
        }
        "the apply method works" in {
          db.underlying must haveSuperclass[com.mongodb.DB]
        }
        "MongoDB" in {
          "has a working apply method" in {
            coll.underlying must haveSuperclass[com.mongodb.DBCollection]
          }
        }
      }
    }

    "Renaming a collection successfully tracks the rename in MongoCollection" in {
      val db = MongoConnection()("casbahTest")
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
    val db = MongoConnection()("casbahTest")

    "Not fail as reported by Max Afonov in SCALA-11" in {
      val coll = db("brand_new_coll_%d".format(System.currentTimeMillis))

      coll.insert(MongoDBObject("foo" -> "bar"))
      val basicFind = coll.find(MongoDBObject("foo" -> "bar"))

      basicFind must haveSize(1)

      val findOne = coll.findOne()

      findOne must beSome

      val findOneMatch = coll.findOne(MongoDBObject("foo" -> "bar"))

      findOneMatch must beSome

    }
  }

  "Cursor Operations" should {
    import scala.util.Random

    val db = MongoConnection()("casbahTest")
    val coll = db("test_coll_%d".format(System.currentTimeMillis))

    for (i <- 1 to 100)
      coll += MongoDBObject("foo" -> "bar", "x" -> Random.nextDouble())

    val first5 = coll.find(MongoDBObject("foo" -> "bar")) limit 5

    "Behave in chains" in {
      //"For loops for idiomatic cleanness" in {

        //// todo - add limit, skip etc on COLLECTION for cleaner chains like this?
        //val items = for (x <- coll.find(MongoDBObject("foo" -> "bar")) skip 5 limit 20) yield x

        //items must haveSize(20)
        //// TODO - Matchers that support freaking cursors, etc
        //[>items must haveSameElementsAs(first5).not<]
      //}

      "Chain operations must return the proper *subtype*" in {
        val cur = coll.find(MongoDBObject("foo" -> "bar")) skip 5
        cur must beAnInstanceOf[MongoCursor]

        val cur2 = coll.find(MongoDBObject("foo" -> "bar")) limit 25 skip 12
        cur2 must beAnInstanceOf[MongoCursor]

      }

    }

  }

}

// vim: set ts=2 sw=2 sts=2 et:
