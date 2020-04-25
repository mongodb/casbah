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

package com.mongodb.casbah.test.commons

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification

class MongoDBListSpec extends CasbahMutableSpecification {

  val x: Seq[Any] = Seq(5, 9, 212, "x", "y", 22.98)
  val y = Seq("spam", "eggs", "foo", "bar")

  "MongoDBList Factory & Builder" should {
    "Support 'empty', returning a BasicDBList" in {
      val dbObj = MongoDBList.empty

      dbObj must haveSize(0)
    }

    "support a 2.8 factory interface which returns a Seq" in {
      val dbLst = MongoDBList("x", "y", 5, 123.82, 84, "spam", "eggs")
      // A Java version to compare with
      val jLst = new com.mongodb.BasicDBList
      jLst.add("x")
      jLst.add("y")
      jLst.add(5.asInstanceOf[AnyRef])
      jLst.add(123.82.asInstanceOf[AnyRef])
      jLst.add(84.asInstanceOf[AnyRef])
      jLst.add("spam")
      jLst.add("eggs")
      jLst must not beEmpty

      dbLst must containTheSameElementsAs(jLst.toSeq)
    }
    "Support a 2.8 builder interface which returns a BasicDBList" in {
      val builder = MongoDBList.newBuilder

      builder += "foo"
      builder += "bar"
      builder += "x"
      builder += "y"
      builder ++= List(5, 212.8, "spam", "eggs", "type erasure" -> "sucks", "omg" -> "ponies!")

      val dbLst = builder.result

      dbLst must haveSize(10)
      // Note we flattened that list above when we added it
      dbLst must containTheSameElementsAs(List("foo", "bar", "x", "y", 5, 212.8, "spam", "eggs", "type erasure" -> "sucks", "omg" -> "ponies!"))
    }

    "Support a mix of other lists and flat items and create a single BasicDBList" in {
      val dbLst = MongoDBList(x, y, "omg" -> "ponies", 5, 212.8)
      dbLst must haveSize(5)
      dbLst must containTheSameElementsAs(Seq(x, y, MongoDBObject("omg" -> "ponies"), 5, 212.8))
    }
    "Support A list/tuple of dbobject declarations" in {
      val dbLst = MongoDBList(x, y, "omg" -> "ponies", 5,
        MongoDBObject("x" -> "y", "foo" -> "bar", "bar" -> "baz"),
        212.8)
      dbLst must haveSize(6)
      dbLst must containTheSameElementsAs(Seq(x, y, MongoDBObject("omg" -> "ponies"), 5,
        MongoDBObject("x" -> "y", "foo" -> "bar", "bar" -> "baz"), 212.8))
    }

    "Convert tuple pairs correctly" in {
      val dbList = MongoDBList("omg" -> "ponies")
      dbList must haveSize(1)
      dbList must containTheSameElementsAs(List(MongoDBObject("omg" -> "ponies")))
    }

    "Concat immutable traversable" in {
      val dbList = MongoDBList.concat(List("ponies"))
      dbList must haveSize(1)
      dbList must beEqualTo(List("ponies"))
    }

    "Concat mutable traversable" in {
      val dbList = MongoDBList.concat(collection.mutable.Buffer("ponies"))
      dbList must haveSize(1)
      dbList must beEqualTo(List("ponies"))
    }

    "Use underlying Object methods" in {
      val seq = MongoDBList(x, y, "omg" -> "ponies", 5,
        MongoDBObject("x" -> "y", "foo" -> "bar", "bar" -> "baz"),
        212.8)

      val raw = new BasicDBList()
      raw += seq

      val mongo: MongoDBList = raw
      mongo must beMongoDBList

      raw.toString must beEqualTo(mongo.toString())
      raw.hashCode must beEqualTo(mongo.hashCode())
      raw.equals(raw) must beEqualTo(mongo.equals(mongo))
    }
  }

  "Support 'as' methods for casting by type" should {
    "getAs functions as expected" in {
      val dbList = MongoDBList(5, 212.8, "eggs", MongoDBObject("foo" -> "bar"), None)
      dbList.getAs[Int](0) must beSome[Int].which(_ == 5)
      dbList.getAs[Double](1) must beSome[Double].which(_ == 212.8)
      dbList.getAs[String](2) must beSome[String].which(_ == "eggs")
      dbList.getAs[DBObject](3) must beSome[DBObject] and haveSomeEntry("foo" -> "bar")

      "Should return None for None, failed casts and missing items" in {
        dbList.getAs[Double](2) must beNone
        dbList.getAs[String](4) must beNone
        dbList.getAs[Float](70) must beNone
      }
    }
  }
  "as functions " should {
    val dbList = MongoDBList(5, 212.8, "eggs", MongoDBObject("foo" -> "bar"), None)

    "function as expected" in {
      dbList.as[Int](0) must beEqualTo(5)
      dbList.as[Double](1) must beEqualTo(212.8)
      dbList.as[String](2) must beEqualTo("eggs")
      dbList.as[DBObject](3) must haveEntry("foo" -> "bar")
    }

    "Should return None for failed casts and missing items" in {
      dbList.as[Float](3) must throwA[ClassCastException]
      dbList.as[Double](50) must throwA[IndexOutOfBoundsException]
    }

    "the result should be assignable to the type specified" in {
      val y: Double = dbList.as[Double](1)
      y must beEqualTo(212.8)
    }
  }

}

