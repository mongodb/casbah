/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/mongodb/casbah
 *
 */

package com.mongodb.casbah.test.core

import com.mongodb.casbah.Imports._

class MongoClientSpec extends CasbahDBTestSpecification {

  "MongoClient connections should be the same as Java MongoClient" should {

    lazy val javaConn = new com.mongodb.MongoClient() // Java connection

    "provide working .asScala methods on the Java version of the objects" in {

      "Connection objects" in {
        val scalaConn = javaConn.asScala
        scalaConn.underlying must beEqualTo(javaConn)
      }

      val javaDb = javaConn.getDB("test")
      val javaCollection = javaDb.getCollection("test")

      "DB objects" in {
        val scalaDb = javaDb.asScala
        scalaDb.underlying must beEqualTo(javaDb)
      }

      "Collection objects" in {
        val scalaCollection = javaCollection.asScala
        scalaCollection.underlying must beEqualTo(javaCollection)
      }
    }

    "And be directly instantiable, with working apply methods" in {
      lazy val conn: MongoClient = MongoClient()
      lazy val db: MongoDB = conn("test")
      lazy val coll: MongoCollection = db("collection.in")

      "MongoClient" in {
        "direct instantiation" in {
          conn.underlying must haveClass[com.mongodb.MongoClient]
        }

        "the apply method works" in {
          db.underlying must haveClass[com.mongodb.DB]
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

      lazy val javaConn = new com.mongodb.Mongo() // Java connection

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
      lazy val conn: MongoConnection = MongoConnection()
      lazy val db: MongoDB = conn("test")
      lazy val coll: MongoCollection = db("collection.in")

      "MongoConnection" in {
        "direct instantiation" in {
          conn.underlying must haveClass[com.mongodb.Mongo]
        }

        "the apply method works" in {
          db.underlying must haveClass[com.mongodb.DB]
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

