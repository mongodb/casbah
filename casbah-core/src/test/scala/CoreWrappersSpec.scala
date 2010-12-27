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
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.conversions.scala._

import org.scala_tools.time.Imports._

import org.specs._
import org.specs.specification.PendingUntilFixed

class CoreWrappersSpec extends Specification with PendingUntilFixed with Logging {

  "Casbah behavior between Scala and Java versions of Objects" should {
    shareVariables


    "provide working .asScala methods on the Java version of the objects" in {

      val javaConn = new com.mongodb.Mongo() // Java connection

      "Connection objects" in {
        javaConn must notBeNull

        val scalaConn = javaConn.asScala
        
        scalaConn must notBeNull
        scalaConn must haveSuperClass[com.mongodb.casbah.MongoConnection]

        scalaConn.underlying must beEqualTo(javaConn)
      }

      val javaDb = javaConn.getDB("test")

      "DB objects" in {
        javaDb must notBeNull

        val scalaDb = javaDb.asScala
        
        scalaDb must notBeNull
        scalaDb must haveSuperClass[com.mongodb.casbah.MongoDB]

        scalaDb.underlying must beEqualTo(javaDb)
      }

      val javaCollection = javaDb.getCollection("test")

      "Collection objects" in {
        javaCollection must notBeNull

        val scalaCollection = javaCollection.asScala
        
        scalaCollection must notBeNull
        scalaCollection must haveSuperClass[com.mongodb.casbah.MongoCollection]

        scalaCollection.underlying must beEqualTo(javaCollection)
      }
    }


    "be directly instantiable, with working apply methods" in {
      var conn: MongoConnection = null
      var db: MongoDB = null
      var coll: MongoCollection = null

      "MongoConnection" in {
        "direct instantiation" in {
          conn = MongoConnection()
          conn must notBeNull
          conn must haveSuperClass[com.mongodb.casbah.MongoConnection]

          conn.underlying must notBeNull
          conn.underlying must haveSuperClass[com.mongodb.Mongo]
        }

        "the apply method works" in {
          conn must notBeNull

          db = conn("test")

          db must notBeNull
          db must haveSuperClass[com.mongodb.casbah.MongoDB]

          db.underlying must notBeNull
          db.underlying must haveSuperClass[com.mongodb.DB]

        }
      }

      "MongoDB" in {
        "has a working apply method" in {
          db must notBeNull

          coll = db("collection.in")

          coll must notBeNull
          coll must haveSuperClass[com.mongodb.casbah.MongoCollection]

          coll.underlying must notBeNull
          coll.underlying must haveSuperClass[com.mongodb.DBCollection]
        }
      }
      
    }

    "Renaming a collection successfully tracks the rename in MongoCollection" in {
      val db = MongoConnection()("casbahTest")
      db("collection").drop()
      val coll = db("collectoin")
      coll.drop()
      coll.insert(MongoDBObject("foo" -> "bar"))
      coll must notBeNull
      coll must haveSuperClass[com.mongodb.casbah.MongoCollection]
      coll.name must beEqualTo("collectoin")

      val newColl = coll.rename("collection") 
      newColl must notBeNull
      newColl must haveSuperClass[com.mongodb.casbah.MongoCollection]
      newColl.name must beEqualTo("collection")
  
      // no mutability in the old collection
      coll.name must beEqualTo("collectoin")
      // collection should be gone so rename fails
      newColl.rename("collection") must throwA[MongoException]

    }
  }

  "findOne operations" should {
    shareVariables()
    val db = MongoConnection()("casbahTest")

    "Not fail as reported by Max Afonov in SCALA-11" in {
      val coll = db("brand_new_coll_%d".format(System.currentTimeMillis))
 
      coll.insert(MongoDBObject("foo" -> "bar"))
      val basicFind = coll.find(MongoDBObject("foo" -> "bar")) 

      basicFind must notBeNull
      basicFind must haveSize(1)

      val findOne = coll.findOne()

      findOne must beSomething

      val findOneMatch = coll.findOne(MongoDBObject("foo" -> "bar")) 

      findOneMatch must beSomething

    }
  } 

}


// vim: set ts=2 sw=2 sts=2 et:
