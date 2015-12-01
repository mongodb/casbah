/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
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

import scala.language.reflectiveCalls

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.util.{ Try, Random }

import com.mongodb.InsertOptions
import com.mongodb.casbah.Cursor
import com.mongodb.casbah.Imports._

class CoreWrappersSpec extends CasbahDBTestSpecification {

  "Casbah behavior between Scala and Java versions of Objects" should {

    lazy val javaConn = new com.mongodb.MongoClient() // Java connection

    "provide working .asScala methods on the Java version of the objects" in {

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
      lazy val conn: MongoClient = MongoClient()
      lazy val db: MongoDB = conn("casbahTest")
      lazy val coll: MongoCollection = database("collection.in")

      "MongoClient" in {
        "direct instantiation" in {
          conn.underlying must haveClass[com.mongodb.MongoClient]
        }

        "the apply method works" in {
          database.underlying must haveClass[com.mongodb.DB]
        }
      }

      "MongoDB" in {
        "has a working apply method" in {
          coll.underlying must beAnInstanceOf[com.mongodb.DBCollection]
        }
      }

    }

    "allow indexes to work as expected" in {
      collection.drop()

      collection.insert(MongoDBObject("foo" -> "bar"))
      collection.indexInfo.length must beEqualTo(1)

      collection.createIndex(MongoDBObject("uid" -> 1), "user_index", unique = true)
      collection.indexInfo.length must beEqualTo(2)

      collection.indexInfo(1)("key") == MongoDBObject("uid" -> 1)
    }

    "check query failure exception" in {
      collection.drop()

      collection += MongoDBObject("loc" -> List(0, 0))
      val near = "loc" $near (0, 0)
      collection.findOne(near) must throwAn[MongoException]
    }

    "Renaming a collection successfully tracks the rename in MongoCollection" in {
      database("collection").drop()
      val coll = database("collectoin")
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

    "Not fail as reported by Max Afonov in CASBAH-11" in {
      collection.drop()

      collection.insert(MongoDBObject("foo" -> "bar"))
      val basicFind = collection.find(MongoDBObject("foo" -> "bar"))

      basicFind.size must beEqualTo(1)

      val findOne = collection.findOne()

      findOne must beSome

      val findOneMatch = collection.findOne(MongoDBObject("foo" -> "bar"))

      findOneMatch must beSome

    }
  }

  "Cursor Operations" should {

    "load some test data first" in {
      collection.drop()
      for (i <- 1 to 100)
        collection += MongoDBObject("foo" -> "bar", "x" -> Random.nextDouble())
      success
    }

    "Behave in chains" in {

      val cur = collection.find(MongoDBObject("foo" -> "bar")) skip 5
      cur must beAnInstanceOf[MongoCursor]

      val cur2 = collection.find(MongoDBObject("foo" -> "bar")) limit 25 skip 12
      cur2 must beAnInstanceOf[MongoCursor]

    }
  }

  "Distinct operations" should {

    "load some test data first" in {
      collection.drop()
      for (i <- 1 to 99)
        collection += MongoDBObject("_id" -> i, "x" -> i % 10)
      success
    }

    "except just a key" in {
      val l = collection.distinct("x")
      l.size must beEqualTo(10)
    }

    "except key and query" in {
      val l = collection.distinct("x", "_id" $gt 95)
      l.size must beEqualTo(4)
    }

    "except key and readPref" in {
      val l = collection.distinct("x", readPrefs = ReadPreference.Primary)
      l.size must beEqualTo(10)
    }

    "except key, query and readPref" in {
      val l = collection.distinct("x", "_id" $gt 95, ReadPreference.Primary)
      l.size must beEqualTo(4)
    }

  }

  "Aggregation operations" should {

    "load some test data first" in {
      collection.drop()
      for (i <- 1 to 99)
        collection += MongoDBObject("_id" -> i, "score" -> i % 10)
      success
    }
    "except just a single op" in {
      val cursor: AggregationOutput = collection.aggregate(MongoDBObject("$match" -> ("score" $gte 7)))
      cursor.results.size must beEqualTo(30)
    }

    "except multiple ops" in {
      val cursor: AggregationOutput = collection.aggregate(
        MongoDBObject("$match" -> ("score" $gte 7)),
        MongoDBObject("$project" -> MongoDBObject("score" -> 1))
      )
      cursor.results.size must beEqualTo(30)
    }

    "except list of ops" in {
      val cursor: AggregationOutput = collection.aggregate(
        List(
          MongoDBObject("$match" -> ("score" $gte 7)),
          MongoDBObject("$project" -> MongoDBObject("score" -> 1))
        )
      )
      cursor.results.size must beEqualTo(30)
    }

    "return a cursor when options are supplied" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.6")
      val aggregationOptions = AggregationOptions(allowDiskUse = true, outputMode = AggregationOptions.CURSOR)
      val cursor: CommandCursor = collection.aggregate(
        List(
          MongoDBObject("$match" -> ("score" $gte 7)),
          MongoDBObject("$project" -> MongoDBObject("score" -> 1))
        ),
        aggregationOptions
      )
      cursor.toList.size must beEqualTo(30)
    }

    "test allowDiskUse isn't included by default" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.6")
      val profileCollection = database("system.profile")
      val profileLevel = database.command(MongoDBObject("profile" -> -1)).as[Int]("was")
      database.command(MongoDBObject("profile" -> 0))
      profileCollection.drop()
      database.command(MongoDBObject("profile" -> 2))

      collection.aggregate(
        List(MongoDBObject("$match" -> ("score" $gte 7))),
        AggregationOptions(outputMode = AggregationOptions.CURSOR)
      )
      val profile = profileCollection.findOne().get.as[MongoDBObject]("command")
      database.command(MongoDBObject("profile" -> profileLevel))
      profile.contains("allowDiskUse") must beFalse
    }

    "test allowDiskUse is included if set" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.6")
      val profileCollection = database("system.profile")
      val profileLevel = database.command(MongoDBObject("profile" -> -1)).as[Int]("was")
      database.command(MongoDBObject("profile" -> 0))
      profileCollection.drop()
      database.command(MongoDBObject("profile" -> 2))

      collection.aggregate(
        List(MongoDBObject("$match" -> ("score" $gte 7))),
        AggregationOptions(allowDiskUse = true, outputMode = AggregationOptions.CURSOR)
      )
      val profile = profileCollection.findOne().get.as[MongoDBObject]("command")
      database.command(MongoDBObject("profile" -> profileLevel))
      profile.contains("allowDiskUse") must beTrue
    }

    "test explainAggregate" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.6")
      val aggregationOptions = AggregationOptions(AggregationOptions.CURSOR)
      val explaination = collection.explainAggregate(
        List(
          MongoDBObject("$match" -> ("score" $gte 7)),
          MongoDBObject("$project" -> MongoDBObject("score" -> 1))
        ),
        aggregationOptions
      )
      explaination("ok") must beEqualTo(1.0)
      explaination.keys must contain("stages")
    }

    "return a cursor when options are supplied even if inline" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")
      val aggregationOptions = AggregationOptions(AggregationOptions.INLINE)

      val cursor: CommandCursor = collection.aggregate(
        List(
          MongoDBObject("$match" -> ("score" $gte 7)),
          MongoDBObject("$project" -> MongoDBObject("score" -> 1))
        ),
        aggregationOptions
      )

      cursor.size must beEqualTo(30)
    }

    "handle $out in multiple ops" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      val outCollection = database("outCollection")
      outCollection.drop()

      val cursor: AggregationOutput = collection.aggregate(
        MongoDBObject("$match" -> ("score" $gte 7)),
        MongoDBObject("$project" -> MongoDBObject("score" -> 1)),
        MongoDBObject("$out" -> outCollection.name)
      )
      cursor.results.iterator.hasNext must beFalse
      outCollection.count() must beEqualTo(30)
    }

    "handle $out in list of ops" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      val outCollection = database("outCollection")
      outCollection.drop()

      val cursor: AggregationOutput = collection.aggregate(List(
        MongoDBObject("$match" -> ("score" $gte 7)),
        MongoDBObject("$project" -> MongoDBObject("score" -> 1)),
        MongoDBObject("$out" -> outCollection.name)
      ))
      cursor.results.iterator.hasNext must beFalse
      outCollection.count() must beEqualTo(30)
    }

    "handle $out with options INLINE" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      val outCollection = database("outCollection")
      outCollection.drop()

      val aggregationOptions = AggregationOptions(AggregationOptions.INLINE)
      val cursor: CommandCursor = collection.aggregate(
        List(
          MongoDBObject("$match" -> ("score" $gte 7)),
          MongoDBObject("$project" -> MongoDBObject("score" -> 1)),
          MongoDBObject("$out" -> outCollection.name)
        ),
        aggregationOptions
      )
      cursor.size must beEqualTo(30)
      outCollection.count() must beEqualTo(30)
    }

    "handle $out with options CURSOR" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      lazy val outCollection = database("outCollection")
      outCollection.drop()

      val aggregationOptions = AggregationOptions(AggregationOptions.CURSOR)
      val cursor: CommandCursor = collection.aggregate(
        List(
          MongoDBObject("$match" -> ("score" $gte 7)),
          MongoDBObject("$project" -> MongoDBObject("score" -> 1)),
          MongoDBObject("$out" -> outCollection.name)
        ),
        aggregationOptions
      )
      cursor.size must beEqualTo(30)
      outCollection.count() must beEqualTo(30)
    }
  }

  "Collection" should {

    "support parallel scan" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")
      isSharded must beFalse.orSkip("Currently doesn't work with mongos")

      collection.drop()

      val ids = (1 to 2000 by 1).toSet
      for (i <- ids) collection += MongoDBObject("_id" -> i)

      val numCursors = 10
      val cursors: mutable.Buffer[Cursor] = collection.parallelScan(ParallelScanOptions(numCursors, 1000))
      cursors.size must beLessThanOrEqualTo(numCursors)

      var cursorIds = Set[Int]()
      for (cursor <- cursors) {
        while (cursor.hasNext) {
          cursorIds += cursor.next().get("_id").asInstanceOf[Int]
        }
      }
      cursorIds must beEqualTo(ids)
    }

    "support bypass document validation" in {
      serverIsAtLeastVersion(3, 2) must beTrue.orSkip("Needs server >= 3.2")

      // given
      collection.drop()
      val createCollectionOptions = MongoDBObject("""{
          validator: {x: {$lte: 100}},
          validationLevel: "strict",
          validationAction: "error"}""")
      database.createCollection(collection.name, createCollectionOptions)

      val ids = (1 to 99 by 1).toSet
      for (i <- ids) { collection += MongoDBObject("x" -> i) }

      // when
      val findAndModify = Try(collection.findAndModify(MongoDBObject("{x: 10}"), MongoDBObject("{$inc: {x: 1000}}")))

      // then
      findAndModify should beAFailedTry

      // when
      val findAndModifyWithBypass = Try(collection.findAndModify(MongoDBObject("{x: 10}"), MongoDBObject("{}"),
        MongoDBObject("{}"), false, MongoDBObject("{$inc: {x: 100}}"), true, false, true, Duration(10, "seconds")))

      // then
      findAndModifyWithBypass should beASuccessfulTry

      // when
      val insert = Try(collection.insert(MongoDBObject("{x: 101}")))

      // then
      insert should beAFailedTry

      // when
      val insertWithBypass = Try(collection.insert(new InsertOptions().bypassDocumentValidation(true), MongoDBObject("{x: 101}")))

      // then
      insertWithBypass should beASuccessfulTry

      // when
      val update = Try(collection.update(MongoDBObject("{x: 1}"), MongoDBObject("{$set: {x : 101}}")))

      // then
      update should beAFailedTry

      // when
      val updateWithBypass = Try(collection.update(MongoDBObject("{x: 1}"), MongoDBObject("{$set: {x : 101}}"),
        bypassDocumentValidation = Some(true)))

      // then
      updateWithBypass should beASuccessfulTry
    }
  }

}
