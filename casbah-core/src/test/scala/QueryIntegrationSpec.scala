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

import scala.jdk.CollectionConverters._
import scala.language.reflectiveCalls

import com.mongodb.WriteConcernException
import com.mongodb.casbah.Imports._

@SuppressWarnings(Array("deprecation"))
class QueryIntegrationSpec extends CasbahDBTestSpecification {

  "Casbah's collection string representation" should {
    "not read the collection" in {
      collection.toString must beEqualTo(collection.name)
    }
  }

  "$set" should {
    "Work with a single pair" in {
      collection.drop()
      collection += MongoDBObject("foo" -> "baz")
      collection.update(MongoDBObject("foo" -> "baz"), $set("foo" -> "bar"))
      collection.find(MongoDBObject("foo" -> "bar")).count must beEqualTo(1)
    }
    "Work with multiple pairs" in {
      val set = $set("foo" -> "baz", "x" -> 5.2,
        "y" -> 9, "a" -> ("b", "c", "d", "e"))
      collection.drop()
      collection += MongoDBObject("foo" -> "bar")
      collection.update(MongoDBObject("foo" -> "baz"), set)
      collection.find(MongoDBObject("foo" -> "bar")).count must beEqualTo(1)
    }
  }

  "$setOnInsert" should {

    "Work with a single pair" in {
      serverIsAtLeastVersion(2, 4) must beTrue.orSkip("Needs server >= 2.4")
      collection.drop()
      try {
        collection.update(MongoDBObject(), $setOnInsert("foo" -> "baz"), upsert = true)
        collection.find(MongoDBObject("foo" -> "baz")).count must beEqualTo(1)
      } catch {
        case ex: WriteConcernException if ex.getErrorMessage != "Invalid modifier specified $setOnInsert" =>
          throw ex
      }
      success
    }

    "Work with multiple pairs" in {
      serverIsAtLeastVersion(2, 4) must beTrue.orSkip("Needs server >= 2.4")
      val set = $setOnInsert("foo" -> "baz", "x" -> 5.2,
        "y" -> 9, "a" -> ("b", "c", "d", "e"))
      collection.drop()
      try {
        collection.update(MongoDBObject(), set, upsert = true)
        collection.find(MongoDBObject("foo" -> "baz")).count must beEqualTo(1)
      } catch {
        case ex: WriteConcernException =>
          if (ex.getErrorMessage != "Invalid modifier specified $setOnInsert")
            throw ex
      }
      success
    }

    "work combined with $set" in {
      serverIsAtLeastVersion(2, 4) must beTrue.orSkip("Needs server >= 2.4")
      collection.drop()
      try {
        collection.update(MongoDBObject(), $setOnInsert("x" -> 1) ++ $set("a" -> "b"), true)
        collection.find(MongoDBObject("x" -> 1)).count must beEqualTo(1)
        collection.find(MongoDBObject("a" -> "b")).count must beEqualTo(1)
      } catch {
        case ex: WriteConcernException =>
          if (ex.getErrorMessage != "Invalid modifier specified $setOnInsert")
            throw ex
      }
      success
    }
  }

  "$unset" should {
    "work as a single item" in {
      collection.drop()
      collection += MongoDBObject("foo" -> "baz", "hello" -> "world")
      collection.update(MongoDBObject("foo" -> "baz"), $unset("foo"))
      collection.findOne().get.keySet.asScala must beEqualTo(Set("_id", "hello"))
    }

    "work with multiple items" in {
      collection.drop()
      collection += MongoDBObject(
        "foo" -> "One",
        "bar" -> 1,
        "x" -> "X",
        "y" -> "Y",
        "hello" -> "world"
      )
      val unset = $unset("foo", "bar", "x", "y")
      collection.update(MongoDBObject("hello" -> "world"), unset)
      collection.findOne().get.keySet.asScala must beEqualTo(Set("_id", "hello"))
    }
  }

  "$where" should {
    "work as expected" in {
      collection.drop()
      collection += MongoDBObject("foo" -> "baz")
      collection.find($where("this.foo == 'baz'")).count must beEqualTo(1)
    }
  }

  "$inc" should {
    "Work with a single pair" in {
      collection.drop()
      collection += MongoDBObject("hello" -> "world")

      collection.update(MongoDBObject("hello" -> "world"), $inc("foo" -> 5.0))
      collection.findOne().get("foo") must beEqualTo(5)
    }
    "Work with multiple pairs" in {
      collection.drop()
      collection += MongoDBObject("hello" -> "world")

      val inc = $inc("foo" -> 5.0, "bar" -> -1.2)
      collection.update(MongoDBObject("hello" -> "world"), inc)

      val doc = collection.findOne()
      doc.get("foo") must beEqualTo(5)
      doc.get("bar") must beEqualTo(-1.2)
    }
  }

  "$max" should {
    "Work with a single pair" in {
      serverIsAtLeastVersion(2, 6) must beTrue.orSkip("Needs server >= 2.6")

      collection.drop()
      collection += MongoDBObject("hello" -> "world", "foo" -> 5.0)

      collection.update(MongoDBObject("hello" -> "world"), $max("foo" -> 6.0))
      collection.findOne().get("foo") must beEqualTo(6)
    }
    "Work with multiple pairs" in {
      serverIsAtLeastVersion(2, 6) must beTrue.orSkip("Needs server >= 2.6")

      collection.drop()
      collection += MongoDBObject("hello" -> "world", "foo" -> 5.0, "bar" -> 5.0)

      val max = $max("foo" -> 3.0, "bar" -> 10.0)
      collection.update(MongoDBObject("hello" -> "world"), max)

      val doc = collection.findOne()
      doc.get("foo") must beEqualTo(5)
      doc.get("bar") must beEqualTo(10)
    }
  }

  "$or" should {
    "load some test data first" in {
      collection.drop()
      collection += MongoDBObject("foo" -> "bar")
      collection += MongoDBObject("foo" -> 6)
      collection += MongoDBObject("foo" -> 5)
      collection += MongoDBObject("x" -> 11)
      success
    }

    "Accept multiple values" in {
      val or = $or("foo" -> "bar", "foo" -> 6)
      collection.find(or).count must beEqualTo(2)
    }
    "Accept a mix" in {
      val or = $or {
        "foo" -> "bar" :: ("foo" $gt 5 $lt 10)
      }
      collection.find(or).count must beEqualTo(2)
    }

    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val or = $or("foo" $lt 6 $gt 1, "x" $gte 10 $lte 152)
        collection.find(or).count must beEqualTo(2)
      }
      "As a cons (::  constructed) cell" in {
        val or = $or(("foo" $lt 6 $gt 1) :: ("x" $gte 10 $lte 152))
        collection.find(or).count must beEqualTo(2)
      }
    }
  }

  "$nor" should {

    "load some test data first" in {
      collection.drop()
      collection += MongoDBObject("foo" -> "bar", "x" -> "y")
      collection += MongoDBObject("foo" -> "baz", "x" -> "y")
      collection += MongoDBObject("foo" -> 6, "x" -> 5)
      collection += MongoDBObject("foo" -> 5, "x" -> 6)
      success
    }

    "Accept multiple values" in {
      val nor = $nor("foo" -> "bar", "x" -> "y")
      collection.find(nor).count must beEqualTo(2)
    }
    "Accept a mix" in {
      val nor = $nor("foo" -> 5 :: ("foo" $gt 5 $lt 10))
      collection.find(nor).count must beEqualTo(2)
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val nor = $nor("foo" $lt 6 $gt 1, "x" $gte 6 $lte 10)
        collection.find(nor).count must beEqualTo(3)
      }
      "As a cons (::  constructed) cell" in {
        val nor = $nor(("foo" $lt 6 $gt 1) :: ("x" $gte 6 $lte 10))
        collection.find(nor).count must beEqualTo(3)
      }
    }
  }

  "$and" should {
    "load some test data first" in {
      collection.drop()
      collection += MongoDBObject("foo" -> "bar", "x" -> "y")
      collection += MongoDBObject("foo" -> "bar", "x" -> 5)
      collection += MongoDBObject("foo" -> "bar", "x" -> 11)
      collection += MongoDBObject("foo" -> 4, "x" -> 11)
      success
    }
    "Accept multiple values" in {
      val and = $and("foo" -> "bar", "x" -> "y")
      collection.find(and).count must beEqualTo(1)
    }
    "Accept a mix" in {
      val and = $and {
        "foo" -> "bar" :: ("x" $gte 5 $lt 10)
      }
      collection.find(and).count must beEqualTo(1)
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val and = $and("foo" $lt 5 $gt 1, "x" $gte 10 $lte 152)
        collection.find(and).count must beEqualTo(1)
      }
      "As a cons (::  constructed) cell" in {
        val and = $and(("foo" $lt 5 $gt 1) :: ("x" $gte 10 $lte 152))
        collection.find(and).count must beEqualTo(1)
      }
    }
  }

  "$rename" should {
    "Accept one or many sets of renames" in {
      "A single set" in {
        collection.drop()
        collection += MongoDBObject("foo" -> "bar", "x" -> "y")
        val rename = $rename("foo" -> "bar")
        collection.update(MongoDBObject("foo" -> "bar"), rename)
        collection.findOne().get.keySet.asScala must containAllOf(Seq("_id", "bar", "x"))
      }
      "Multiple sets" in {
        collection.drop()
        collection += MongoDBObject("foo" -> "bar", "x" -> "y")
        val rename = $rename("foo" -> "bar", "x" -> "y")
        collection.update(MongoDBObject("foo" -> "bar"), rename)
        collection.findOne().get.keySet.asScala must containAllOf(Seq("_id", "bar", "y"))
      }
    }
  }

  "Casbah's DSL Array operators" should {
    "$push" in {
      "Accept a single value" in {
        collection.drop()
        collection += MongoDBObject("foo" -> "bar")
        val push = $push("x" -> "y")
        collection.update(MongoDBObject("foo" -> "bar"), push)
        val doc = collection.findOne().get
        doc.keySet.asScala must beEqualTo(Set("_id", "foo", "x"))
        doc.as[MongoDBList]("x") must beEqualTo(MongoDBList("y"))
      }
      "Accept multiple values" in {
        collection.drop()
        collection += MongoDBObject("a" -> "b")
        val push = $push("foo" -> "bar", "x" -> 5.2)
        collection.update(MongoDBObject("a" -> "b"), push)
        val doc = collection.findOne().get
        doc.keySet.asScala must beEqualTo(Set("_id", "a", "foo", "x"))
        doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("bar"))
        doc.as[MongoDBList]("x") must beEqualTo(MongoDBList(5.2))
      }
    }
    "$pushAll" in {
      "Accept a single value list" in {
        collection.drop()
        collection += MongoDBObject("foo" -> "bar")
        val push = $pushAll("baz" -> ("bar", "baz", "x", "y"))
        collection.update(MongoDBObject("foo" -> "bar"), push)
        val doc = collection.findOne().get
        doc.keySet.asScala must containAllOf(Seq("_id", "baz", "foo"))
        doc.as[MongoDBList]("baz") must beEqualTo(MongoDBList("bar", "baz", "x", "y"))
      }

      "Accept multiple value lists" in {
        collection.drop()
        collection += MongoDBObject("a" -> "b")
        val push = $pushAll("foo" -> ("bar", "baz", "x", "y"), "x" -> (5, 10, 12, 238))
        collection.update(MongoDBObject("a" -> "b"), push)
        val doc = collection.findOne().get
        doc.keySet.asScala must containAllOf(Seq("_id", "a", "foo", "x"))
        doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("bar", "baz", "x", "y"))
        doc.as[MongoDBList]("x") must beEqualTo(MongoDBList(5, 10, 12, 238))
      }
    }

    "$pull" in {
      "Accept a single value" in {
        collection.drop()
        collection += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1))
        val pull = $pull("foo" -> 2)
        collection.update(MongoDBObject("a" -> "b"), pull)
        collection.findOne().get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 1))
      }
      "Allow Value Test Operators" in {
        "A simple $gt test" in {
          collection.drop()
          collection += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1))
          val pull = $pull("foo" $gt 2)
          collection.update(MongoDBObject("a" -> "b"), pull)
          collection.findOne().get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(2, 1))
        }
        "A deeper chain test" in {
          collection.drop()
          collection += MongoDBObject("a" -> "b", "foo" -> MongoDBList(30, 20, 10, 3, 2, 1))
          val pull = $pull("foo" $gt 5 $lte 52)
          collection.update(MongoDBObject("a" -> "b"), pull)
          collection.findOne().get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 2, 1))
        }
        "a sub document from a list" in {
          collection.drop()
          collection += MongoDBObject("_id" -> "xyz", "answers" -> MongoDBList(
            MongoDBObject("name" -> "Yes"),
            MongoDBObject("name" -> "No")
          ))
          val pull = $pull("answers" -> MongoDBObject("name" -> "Yes"))
          collection.update(MongoDBObject("_id" -> "xyz"), pull)
          collection.findOne().get.as[MongoDBList]("answers") must beEqualTo(MongoDBList(MongoDBObject("name" -> "No")))
        }
      }
      "Accept multiple values" in {
        collection.drop()
        collection += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1), "x" -> MongoDBList(5.1, 5.2, 5.3))
        val pull = $pull("foo" -> 2, "x" -> 5.2)
        collection.update(MongoDBObject("a" -> "b"), pull)
        collection.findOne().get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 1))
        collection.findOne().get.as[MongoDBList]("x") must beEqualTo(MongoDBList(5.1, 5.3))
      }
    }
    "$pullAll" in {
      "Accept a single value list" in {
        collection.drop()
        collection += MongoDBObject("a" -> "b", "foo" -> MongoDBList(30, 20, 10, 3, 2, 1))
        val pull = $pullAll("foo" -> (30, 20, 10))
        collection.update(MongoDBObject("a" -> "b"), pull)
        collection.findOne().get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 2, 1))
      }

      "Accept multiple value lists" in {
        collection.drop()
        collection += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1), "x" -> MongoDBList(5.1, 5.2, 5.3))
        val pull = $pullAll("foo" -> (3, 2), "x" -> (5.1, 5.2))
        collection.update(MongoDBObject("a" -> "b"), pull)
        collection.findOne().get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(1))
        collection.findOne().get.as[MongoDBList]("x") must beEqualTo(MongoDBList(5.3))
      }
    }
  }

  "$addToSet" in {
    "Accept a single value" in {
      collection.drop()
      collection += MongoDBObject("a" -> "b")
      val addToSet = $addToSet("foo" -> "bar")
      collection.update(MongoDBObject("a" -> "b"), addToSet)
      val doc = collection.findOne().get
      doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("bar"))
    }
    "Accept multiple values" in {
      collection.drop()
      collection += MongoDBObject("a" -> "b")
      val addToSet = $addToSet("foo" -> "bar", "x" -> 5.2)
      collection.update(MongoDBObject("a" -> "b"), addToSet)
      val doc = collection.findOne().get
      doc.keySet.asScala must beEqualTo(Set("_id", "a", "foo", "x"))
      doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("bar"))
      doc.as[MongoDBList]("x") must beEqualTo(MongoDBList(5.2))
    }
    "Function with the $each operator for multi-value updates" in {
      collection.drop()
      collection += MongoDBObject("a" -> "b")
      val addToSet = $addToSet("foo") $each ("x", "y", "foo", "bar", "baz")
      collection.update(MongoDBObject("a" -> "b"), addToSet)
      val doc = collection.findOne().get
      doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("x", "y", "foo", "bar", "baz"))
    }
  }

  "$pop" in {
    "Accept a single value" in {
      collection.drop()
      collection += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1))
      val pop = $pop("foo" -> 1)
      collection.update(MongoDBObject("a" -> "b"), pop)
      collection.findOne().get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 2))
    }
    "Accept multiple values" in {
      collection.drop()
      collection += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1), "x" -> MongoDBList(1, 2, 3))
      val pop = $pop("foo" -> 1, "x" -> -1)
      collection.update(MongoDBObject("a" -> "b"), pop)
      val doc = collection.findOne().get
      doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 2))
      doc.as[MongoDBList]("x") must beEqualTo(MongoDBList(2, 3))
    }
  }

  "$bit" in {
    "Accept a single value For 'and'" in {
      collection.drop()
      collection += MongoDBObject("foo" -> 5)
      val bit = $bit("foo") and 1
      collection.update(MongoDBObject("foo" -> 5), bit)
      collection.findOne().get("foo") must beEqualTo(1)
    }
    "Accept a single value For 'or'" in {
      collection.drop()
      collection += MongoDBObject("foo" -> 5)
      val bit = $bit("foo") or 1
      collection.update(MongoDBObject("foo" -> 5), bit)
      collection.findOne().get("foo") must beEqualTo(5)
    }
  }

  "$text operator" should {

    "Setup and load some test data first" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      val enableTextCommand = MongoDBObject("setParameter" -> 1, "textSearchEnabled" -> true)
      collection.getDB.getSisterDB("admin").command(enableTextCommand)
      val textIndex = MongoDBObject("a" -> "text")
      collection.drop()
      collection.createIndex(textIndex)

      collection += MongoDBObject("_id" -> 0, "unindexedField" -> 0, "a" -> "textual content")
      collection += MongoDBObject("_id" -> 1, "unindexedField" -> 1, "a" -> "additional content")
      collection += MongoDBObject("_id" -> 2, "unindexedField" -> 2, "a" -> "irrelevant content")
      success
    }

    "Accept just $text" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      collection.find($text("textual content -irrelevant")).count should beEqualTo(2)
    }

    "Accept $text and other operators" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      collection.find(("unindexedField" $eq 0) ++ $text("textual content -irrelevant")).count should beEqualTo(1)
    }

    "Accept $text and $language" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      collection.find($text("textual content -irrelevant") $language "english").count should beEqualTo(2)
    }

    "Work with $meta projection" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      collection.find($text("textual content -irrelevant"), "score" $meta).count should beEqualTo(2)
      val result = collection.findOne($text("textual content -irrelevant"), "score" $meta)
      result must haveSomeField("score")
    }

    "Work with $meta in projection and sort" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      collection.find($text("textual content -irrelevant"), "score" $meta).sort("score" $meta).count should beEqualTo(2)
    }
  }

}

