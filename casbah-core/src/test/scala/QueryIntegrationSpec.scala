/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
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

import scala.collection.JavaConverters._

import com.mongodb.WriteConcernException
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification


@SuppressWarnings(Array("deprecation"))
class QueryIntegrationSpec extends CasbahMutableSpecification {
  sequential

  skipAllUnless(MongoDBOnline)

  implicit lazy val coll = MongoClient()("casbahIntegration")("bareword")

  "Casbah's collection string representation" should {
    "not read the collection" in {
      coll.toString must beEqualTo("bareword")
    }
  }

  "$set" should {
    "Work with a single pair" in {
      coll.drop()
      coll += MongoDBObject("foo" -> "baz")
      coll.update(MongoDBObject("foo" -> "baz"), $set("foo" -> "bar"))
      coll.find(MongoDBObject("foo" -> "bar")).count must beEqualTo(1)
    }
    "Work with multiple pairs" in {
      val set = $set("foo" -> "baz", "x" -> 5.2,
        "y" -> 9, "a" ->("b", "c", "d", "e"))
      coll.drop()
      coll += MongoDBObject("foo" -> "bar")
      coll.update(MongoDBObject("foo" -> "baz"), set)
      coll.find(MongoDBObject("foo" -> "bar")).count must beEqualTo(1)
    }
  }

  "$setOnInsert" should {
    "Work with a single pair" in {
      coll.drop()
      try {
        coll.update(MongoDBObject(), $setOnInsert("foo" -> "baz"), true)
        coll.find(MongoDBObject("foo" -> "baz")).count must beEqualTo(1)
      } catch {
        case ex: WriteConcernException => {
          if (ex.getCommandResult.get("err") != "Invalid modifier specified $setOnInsert")
            throw ex
        }
      }
      success
    }

    "Work with multiple pairs" in {
      val set = $setOnInsert("foo" -> "baz", "x" -> 5.2,
        "y" -> 9, "a" ->("b", "c", "d", "e"))
      coll.drop()
      try {
        coll.update(MongoDBObject(), set, true)
        coll.find(MongoDBObject("foo" -> "baz")).count must beEqualTo(1)
      } catch {
        case ex: WriteConcernException => {
          if (ex.getCommandResult.get("err") != "Invalid modifier specified $setOnInsert")
            throw ex
        }
      }
      success
    }

    "work combined with $set" in {
      coll.drop()
      try {
        coll.update(MongoDBObject(), $setOnInsert("x" -> 1) ++ $set("a" -> "b"), true)
        coll.find(MongoDBObject("x" -> 1)).count must beEqualTo(1)
        coll.find(MongoDBObject("a" -> "b")).count must beEqualTo(1)
      } catch {
        case ex: WriteConcernException => {
          if (ex.getCommandResult.get("err") != "Invalid modifier specified $setOnInsert")
            throw ex
        }
      }
      success
    }
  }

  "$unset" should {
    "work as a single item" in {
      coll.drop()
      coll += MongoDBObject("foo" -> "baz", "hello" -> "world")
      coll.update(MongoDBObject("foo" -> "baz"), $unset("foo"))
      coll.findOne().get.keySet.asScala must beEqualTo(Set("_id", "hello"))
    }

    "work with multiple items" in {
      coll.drop()
      coll += MongoDBObject("foo" -> "One",
        "bar" -> 1,
        "x" -> "X",
        "y" -> "Y",
        "hello" -> "world")
      val unset = $unset("foo", "bar", "x", "y")
      coll.update(MongoDBObject("hello" -> "world"), unset)
      coll.findOne().get.keySet.asScala must beEqualTo(Set("_id", "hello"))
    }
  }

  "$where" should {
    "work as expected" in {
      coll.drop()
      coll += MongoDBObject("foo" -> "baz")
      coll.find($where("this.foo == 'baz'")).count must beEqualTo(1)
    }
  }

  "$inc" should {
    "Work with a single pair" in {
      coll.drop()
      coll += MongoDBObject("hello" -> "world")
      val inc = $inc("foo" -> 5.0)
      coll.update(MongoDBObject("hello" -> "world"), $inc("foo" -> 5.0))
      coll.findOne().get("foo") must beEqualTo(5)
    }
    "Work with multiple pairs" in {
      coll.drop()
      coll += MongoDBObject("hello" -> "world")
      val inc = $inc("foo" -> 5.0, "bar" -> -1.2)
      coll.update(MongoDBObject("hello" -> "world"), inc)
      val doc = coll.findOne()
      doc.get("foo") must beEqualTo(5)
      doc.get("bar") must beEqualTo(-1.2)
    }
  }

  "$or" should {
    "load some test data first" in {
      coll.drop()
      coll += MongoDBObject("foo" -> "bar")
      coll += MongoDBObject("foo" -> 6)
      coll += MongoDBObject("foo" -> 5)
      coll += MongoDBObject("x" -> 11)
      success
    }

    "Accept multiple values" in {
      val or = $or("foo" -> "bar", "foo" -> 6)
      coll.find(or).count must beEqualTo(2)
    }
    "Accept a mix" in {
      val or = $or {
        "foo" -> "bar" :: ("foo" $gt 5 $lt 10)
      }
      coll.find(or).count must beEqualTo(2)
    }

    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val or = $or("foo" $lt 6 $gt 1, "x" $gte 10 $lte 152)
        coll.find(or).count must beEqualTo(2)
      }
      "As a cons (::  constructed) cell" in {
        val or = $or(("foo" $lt 6 $gt 1) :: ("x" $gte 10 $lte 152))
        coll.find(or).count must beEqualTo(2)
      }
    }
  }

  "$nor" should {

    "load some test data first" in {
      coll.drop()
      coll += MongoDBObject("foo" -> "bar", "x" -> "y")
      coll += MongoDBObject("foo" -> "baz", "x" -> "y")
      coll += MongoDBObject("foo" -> 6, "x" -> 5)
      coll += MongoDBObject("foo" -> 5, "x" -> 6)
      success
    }

    "Accept multiple values" in {
      val nor = $nor("foo" -> "bar", "x" -> "y")
      println(nor)
      coll.find(nor).count must beEqualTo(2)
    }
    "Accept a mix" in {
      val nor = $nor("foo" -> 5 :: ("foo" $gt 5 $lt 10))
      coll.find(nor).count must beEqualTo(2)
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val nor = $nor("foo" $lt 6 $gt 1, "x" $gte 6 $lte 10)
        coll.find(nor).count must beEqualTo(3)
      }
      "As a cons (::  constructed) cell" in {
        val nor = $nor(("foo" $lt 6 $gt 1) :: ("x" $gte 6 $lte 10))
        coll.find(nor).count must beEqualTo(3)
      }
    }
  }

  "$and" should {
    "load some test data first" in {
      coll.drop()
      coll += MongoDBObject("foo" -> "bar", "x" -> "y")
      coll += MongoDBObject("foo" -> "bar", "x" -> 5)
      coll += MongoDBObject("foo" -> "bar", "x" -> 11)
      coll += MongoDBObject("foo" -> 4, "x" -> 11)
      success
    }
    "Accept multiple values" in {
      val and = $and("foo" -> "bar", "x" -> "y")
      coll.find(and).count must beEqualTo(1)
    }
    "Accept a mix" in {
      val and = $and {
        "foo" -> "bar" :: ("x" $gte 5 $lt 10)
      }
      coll.find(and).count must beEqualTo(1)
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val and = $and("foo" $lt 5 $gt 1, "x" $gte 10 $lte 152)
        coll.find(and).count must beEqualTo(1)
      }
      "As a cons (::  constructed) cell" in {
        val and = $and(("foo" $lt 5 $gt 1) :: ("x" $gte 10 $lte 152))
        coll.find(and).count must beEqualTo(1)
      }
    }
  }

  "$rename" should {
    "Accept one or many sets of renames" in {
      "A single set" in {
        coll.drop()
        coll += MongoDBObject("foo" -> "bar", "x" -> "y")
        val rename = $rename("foo" -> "bar")
        coll.update(MongoDBObject("foo" -> "bar"), rename)
        coll.findOne.get.keySet.asScala must beEqualTo(Set("_id", "bar", "x"))
      }
      "Multiple sets" in {
        coll.drop()
        coll += MongoDBObject("foo" -> "bar", "x" -> "y")
        val rename = $rename("foo" -> "bar", "x" -> "y")
        coll.update(MongoDBObject("foo" -> "bar"), rename)
        coll.findOne.get.keySet.asScala must beEqualTo(Set("_id", "bar", "y"))
      }
    }
  }

  "Casbah's DSL Array operators" should {
    "$push" in {
      "Accept a single value" in {
        coll.drop()
        coll += MongoDBObject("foo" -> "bar")
        val push = $push("x" -> "y")
        coll.update(MongoDBObject("foo" -> "bar"), push)
        val doc = coll.findOne.get
        doc.keySet.asScala must beEqualTo(Set("_id", "foo", "x"))
        doc.as[MongoDBList]("x") must beEqualTo(MongoDBList("y"))
      }
      "Accept multiple values" in {
        coll.drop()
        coll += MongoDBObject("a" -> "b")
        val push = $push("foo" -> "bar", "x" -> 5.2)
        coll.update(MongoDBObject("a" -> "b"), push)
        val doc = coll.findOne.get
        doc.keySet.asScala must beEqualTo(Set("_id", "a", "foo", "x"))
        doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("bar"))
        doc.as[MongoDBList]("x") must beEqualTo(MongoDBList(5.2))
      }
    }
    "$pushAll" in {
      "Accept a single value list" in {
        coll.drop()
        coll += MongoDBObject("foo" -> "bar")
        val push = $pushAll("baz" ->("bar", "baz", "x", "y"))
        coll.update(MongoDBObject("foo" -> "bar"), push)
        val doc = coll.findOne.get
        doc.keySet.asScala must beEqualTo(Set("_id", "baz", "foo"))
        doc.as[MongoDBList]("baz") must beEqualTo(MongoDBList("bar", "baz", "x", "y"))
      }

      "Accept multiple value lists" in {
        coll.drop()
        coll += MongoDBObject("a" -> "b")
        val push = $pushAll("foo" ->("bar", "baz", "x", "y"), "x" ->(5, 10, 12, 238))
        coll.update(MongoDBObject("a" -> "b"), push)
        val doc = coll.findOne.get
        doc.keySet.asScala must beEqualTo(Set("_id", "a", "foo", "x"))
        doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("bar", "baz", "x", "y"))
        doc.as[MongoDBList]("x") must beEqualTo(MongoDBList(5, 10, 12, 238))
      }
    }

    "$pull" in {
      "Accept a single value" in {
        coll.drop()
        coll += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1))
        val pull = $pull("foo" -> 2)
        coll.update(MongoDBObject("a" -> "b"), pull)
        coll.findOne.get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 1))
      }
      "Allow Value Test Operators" in {
        "A simple $gt test" in {
          coll.drop()
          coll += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1))
          val pull = $pull("foo" $gt 2)
          coll.update(MongoDBObject("a" -> "b"), pull)
          coll.findOne.get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(2, 1))
        }
        "A deeper chain test" in {
          coll.drop()
          coll += MongoDBObject("a" -> "b", "foo" -> MongoDBList(30, 20, 10, 3, 2, 1))
          val pull = $pull("foo" $gt 5 $lte 52)
          coll.update(MongoDBObject("a" -> "b"), pull)
          coll.findOne.get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 2, 1))
        }
        "a sub document from a list" in {
          coll.drop()
          coll += MongoDBObject("_id" -> "xyz", "answers" -> MongoDBList(
            MongoDBObject("name" -> "Yes"),
            MongoDBObject("name" -> "No")
          ))
          val pull = $pull("answers" -> MongoDBObject("name" -> "Yes"))
          coll.update(MongoDBObject("_id" -> "xyz"), pull)
          coll.findOne.get.as[MongoDBList]("answers") must beEqualTo(MongoDBList(MongoDBObject("name" -> "No")))
        }
      }
      "Accept multiple values" in {
        coll.drop()
        coll += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1), "x" -> MongoDBList(5.1, 5.2, 5.3))
        val pull = $pull("foo" -> 2, "x" -> 5.2)
        coll.update(MongoDBObject("a" -> "b"), pull)
        coll.findOne.get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 1))
        coll.findOne.get.as[MongoDBList]("x") must beEqualTo(MongoDBList(5.1, 5.3))
      }
    }
    "$pullAll" in {
      "Accept a single value list" in {
        coll.drop()
        coll += MongoDBObject("a" -> "b", "foo" -> MongoDBList(30, 20, 10, 3, 2, 1))
        val pull = $pullAll("foo" ->(30, 20, 10))
        coll.update(MongoDBObject("a" -> "b"), pull)
        coll.findOne.get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 2, 1))
      }

      "Accept multiple value lists" in {
        coll.drop()
        coll += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1), "x" -> MongoDBList(5.1, 5.2, 5.3))
        val pull = $pullAll("foo" ->(3, 2), "x" ->(5.1, 5.2))
        coll.update(MongoDBObject("a" -> "b"), pull)
        coll.findOne.get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(1))
        coll.findOne.get.as[MongoDBList]("x") must beEqualTo(MongoDBList(5.3))
      }
    }
  }

  "$addToSet" in {
    "Accept a single value" in {
      coll.drop()
      coll += MongoDBObject("a" -> "b")
      val addToSet = $addToSet("foo" -> "bar")
      coll.update(MongoDBObject("a" -> "b"), addToSet)
      val doc = coll.findOne.get
      doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("bar"))
    }
    "Accept multiple values" in {
      coll.drop()
      coll += MongoDBObject("a" -> "b")
      val addToSet = $addToSet("foo" -> "bar", "x" -> 5.2)
      coll.update(MongoDBObject("a" -> "b"), addToSet)
      val doc = coll.findOne.get
      doc.keySet.asScala must beEqualTo(Set("_id", "a", "foo", "x"))
      doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("bar"))
      doc.as[MongoDBList]("x") must beEqualTo(MongoDBList(5.2))
    }
    "Function with the $each operator for multi-value updates" in {
      coll.drop()
      coll += MongoDBObject("a" -> "b")
      val addToSet = $addToSet("foo") $each("x", "y", "foo", "bar", "baz")
      coll.update(MongoDBObject("a" -> "b"), addToSet)
      val doc = coll.findOne.get
      doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList("x", "y", "foo", "bar", "baz"))
    }
  }

  "$pop" in {
    "Accept a single value" in {
      coll.drop()
      coll += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1))
      val pop = $pop("foo" -> 1)
      coll.update(MongoDBObject("a" -> "b"), pop)
      coll.findOne.get.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 2))
    }
    "Accept multiple values" in {
      coll.drop()
      coll += MongoDBObject("a" -> "b", "foo" -> MongoDBList(3, 2, 1), "x" -> MongoDBList(1, 2, 3))
      val pop = $pop("foo" -> 1, "x" -> -1)
      coll.update(MongoDBObject("a" -> "b"), pop)
      val doc = coll.findOne.get
      doc.as[MongoDBList]("foo") must beEqualTo(MongoDBList(3, 2))
      doc.as[MongoDBList]("x") must beEqualTo(MongoDBList(2, 3))
    }
  }

  "$bit" in {
    "Accept a single value For 'and'" in {
      coll.drop()
      coll += MongoDBObject("foo" -> 5)
      val bit = $bit("foo") and 1
      coll.update(MongoDBObject("foo" -> 5), bit)
      coll.findOne.get("foo") must beEqualTo(1)
    }
    "Accept a single value For 'or'" in {
      coll.drop()
      coll += MongoDBObject("foo" -> 5)
      val bit = $bit("foo") or 1
      coll.update(MongoDBObject("foo" -> 5), bit)
      coll.findOne.get("foo") must beEqualTo(5)
    }
  }

  "$text operator" should {

    "Setup and load some test data first" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      val enableTextCommand = MongoDBObject("setParameter" -> 1, "textSearchEnabled" -> true)
      coll.getDB.getSisterDB("admin").command(enableTextCommand)
      val textIndex = MongoDBObject("a" -> "text")
      coll.drop()
      coll.ensureIndex(textIndex)

      coll += MongoDBObject("_id" -> 0, "unindexedField" -> 0, "a" -> "textual content")
      coll += MongoDBObject("_id" -> 1, "unindexedField" -> 1, "a" -> "additional content")
      coll += MongoDBObject("_id" -> 2, "unindexedField" -> 2, "a" -> "irrelevant content")
      success
    }

    "Accept just $text" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      coll.find($text("textual content -irrelevant")).count should beEqualTo(2)
    }

    "Accept $text and other operators" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      coll.find(("unindexedField" $eq 0) ++ $text("textual content -irrelevant")).count should beEqualTo(1)
    }

    "Accept $text and $language" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      coll.find($text("textual content -irrelevant") $language "english").count should beEqualTo(2)
    }

    "Work with $meta projection" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      coll.find($text("textual content -irrelevant"), "score" $meta).count should beEqualTo(2)
      val result = coll.findOne($text("textual content -irrelevant"), "score" $meta)
      result must haveSomeField("score")
    }

    "Work with $meta in projection and sort" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")

      coll.find($text("textual content -irrelevant"), "score" $meta).sort("score" $meta).count should beEqualTo(2)
    }
  }

}


