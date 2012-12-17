/**
 * Copyright (c) 2010 - 2012 10gen, Inc. <http://10gen.com>
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

import org.scala_tools.time.Imports._


class MongoClientSpec extends CasbahMutableSpecification {

  "MongoClient connections should be the same as Java MongoClient" should {

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

    "And be directly instantiable, with working apply methods" in {
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

  }

  "MongoConnection connections should be the same as Java Mongo" should {

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

    "And be directly instantiable, with working apply methods" in {
      var conn: MongoConnection = MongoConnection()
      var db: MongoDB = conn("test")
      var coll: MongoCollection = db("collection.in")

      "MongoConnection" in {
        "direct instantiation" in {
          conn.underlying must haveClass[com.mongodb.Mongo]
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
  }
}

