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

import scala.Some

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification

import com.github.nscala_time.time.Imports._

class MongoDBObjectSpec extends CasbahMutableSpecification {
  type JDKDate = java.util.Date

  "MongoDBObject expand operations" should {

    val x: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> "C"))
    val y: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> MongoDBObject("C" -> 5)))
    val z: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> MongoDBObject("C" -> List(5, 4, 3, 2, 1))))

    "Expanding a simple layering should work" in {
      val b = x.expand[String]("A.B")
      b must beSome("C")

      "While overexpanding returns None" in {
        x.expand[String]("A.B.C") must beNone
      }
    }

    "Expanding a further layering should work" in {
      // TODO - This is way broken as far as casting to int
      val c = y.expand[Int]("A.B.C")
      c must beSome(5)
      "While overexpanding should probably fail" in {
        val cFail = y.expand[String]("A.B.C.D")
        cFail must beNone
      }
    }

    "And you can go further and even get a list" in {
      val c = z.expand[List[Int]]("A.B.C")
      c must beEqualTo(Some(List(5, 4, 3, 2, 1)))
    }
  }

  "MongoDBObject issues and edge cases" should {
    "Not break like tommy chheng reported" in {
      val q = MongoDBObject.empty

      val fields = MongoDBObject("name" -> 1)

      // Simple test of Is it a DBObject?
      q must beDBObject
      fields must beDBObject
    }

    "If a BasicDBList is requested it quietly is recast to something which can be a Seq[] or List[]" in {
      val lst = new BasicDBList()
      lst.add("Brendan")
      lst.add("Mike")
      lst.add("Tyler")
      lst.add("Jeff")
      lst.add("Jesse")
      lst.add("Christian")
      val doc = MongoDBObject("driverAuthors" -> lst)

      doc.getAs[Seq[_]]("driverAuthors") must beSome[Seq[_]]
      doc.getAs[List[_]]("driverAuthors") must beSome[List[_]]
    }

    "Nones placed to DBObject should immediately convert to null to present proper getAs behavior" in {
      val obj = MongoDBObject("foo" -> None)
      obj.getAs[String]("foo") must beNone
    }

    "getAs should respect types" in {
      val obj = MongoDBObject("foo" -> 0)
      obj.getAs[String]("foo") must beNone
      obj.getAs[Int]("foo") must beSome[Int]
    }
  }

  /* "CASBAH-42, storing Java Arrays in a DBObject shouldn't break .equals and .hashcode" in {
    val one = MongoDBObject("anArray" -> Array(MongoDBObject("one" -> "oneVal"), MongoDBObject("two" -> "twoVal")))
    val two = MongoDBObject("anArray" -> Array(MongoDBObject("one" -> "oneVal"), MongoDBObject("two" -> "twoVal")))
    one must beEqualTo(two)
  } */

  "MongoDBObject Factory & Builder" should {
    "Support 'empty', returning a DBObject" in {
      val dbObj: DBObject = MongoDBObject.empty

      dbObj must beDBObject
      dbObj must have size (0)
    }

    "allow list as well as varargs construction" in {
      val dbObj = MongoDBObject(List("x" -> 1, "y" -> 2))

      // A Java version to compare with
      val jBldr = new com.mongodb.BasicDBObjectBuilder
      jBldr.add("x", 1)
      jBldr.add("y", 2)
      val jObj = jBldr.get

      dbObj must beDBObject
      jObj must beDBObject
      dbObj must beEqualTo(jObj)
    }

    "support a 2.8 factory interface which returns a DBObject" in {
      val dbObj = MongoDBObject("x" -> 5, "y" -> 212.8, "spam" -> "eggs",
        "embedded" -> MongoDBObject("foo" -> "bar"))
      // A Java version to compare with
      val jBldr = new com.mongodb.BasicDBObjectBuilder
      jBldr.add("x", 5)
      jBldr.add("y", 212.8)
      jBldr.add("spam", "eggs")
      jBldr.add("embedded", new com.mongodb.BasicDBObject("foo", "bar"))
      val jObj = jBldr.get

      dbObj must beDBObject
      jObj must beDBObject
      dbObj must beEqualTo(jObj)
    }
    "Support a 2.8 builder interface which returns a DBObject" in {
      val builder = MongoDBObject.newBuilder

      builder += "foo" -> "bar"
      builder += "x" -> 5
      builder += "y" -> 212.8

      builder ++= List("spam" -> "eggs", "type erasure" -> "sucks", "omg" -> "ponies!")

      val dbObj = builder.result

      dbObj must beDBObject
      dbObj must haveSize(6)
    }
  }

  "MongoDBObject type conversions" should {
    "Support converting Maps of [String, Any] to DBObjects" in {
      val control: DBObject = MongoDBObject("foo" -> "bar", "n" -> 2)
      control must beDBObject

      val map = Map("foo" -> "bar", "n" -> 2)

      val cast: DBObject = map

      cast must beDBObject
      cast must beEqualTo(control)

      val explicit = map.asDBObject

      explicit must beDBObject
      explicit must beEqualTo(control)
    }
    "Support complex lists into DBObjects" in {
      val control: DBObject = MongoDBObject(
        "a" -> 1, "b" -> 2,
        "c" -> MongoDBList(MongoDBObject("x" -> 1), MongoDBObject("y" -> 2))
      )

      control must beDBObject

      val map = Map("a" -> 1, "b" -> 2, "c" -> List(Map("x" -> 1), Map("y" -> 2)))

      val cast: DBObject = map

      cast must beDBObject
      cast.toString must beEqualTo(control.toString())
      cast.equals(cast) must beEqualTo(control.equals(control))

      val explicit = map.asDBObject

      explicit must beDBObject
      explicit.toString must beEqualTo(control.toString())
      explicit.equals(explicit) must beEqualTo(control.equals(control))

      // TODO FIX:
      // cast must beEqualTo(control)
      // explicit must beEqualTo(control)

    }
  }

  "MongoDBObject" >> {
    "Support additivity of Tuple Entrys" >> {
      "A single Tuple Entry with + " in {
        // TODO - you currently have to explicitly cast this or get back a map. ugh.
        val newObj: DBObject = MongoDBObject("x" -> "y", "a" -> "b") + ("foo" -> "bar")
        newObj must haveEntries("x" -> "y", "a" -> "b", "foo" -> "bar")
      }
      "A list of Tuple Entrys with ++ " in {
        val newObj = MongoDBObject("x" -> "y", "a" -> "b") ++~ ("foo" -> "bar", "n" -> 5)
        newObj must beDBObject

        newObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b", "foo" -> "bar", "n" -> 5))
      }
      "Merging a single tuple via += " in {
        val dbObj = MongoDBObject("x" -> "y", "a" -> "b")
        dbObj must beDBObject
        dbObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b"))

        dbObj += ("foo" -> "bar")

        dbObj must beDBObject

        dbObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b", "foo" -> "bar"))

      }
      "Merging a set of tuples via ++= " in {
        val dbObj = MongoDBObject("x" -> "y", "a" -> "b")
        dbObj must beDBObject
        dbObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b"))

        dbObj += ("foo" -> "bar", "n" -> 5.asInstanceOf[AnyRef], "fbc" -> 542542.2.asInstanceOf[AnyRef])

        dbObj must beDBObject

        dbObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b", "foo" -> "bar", "n" -> 5, "fbc" -> 542542.2))

      }
    }

    "Support additivity with another MongoDBObject" in {
      val newObj = MongoDBObject("x" -> "y", "a" -> "b") ++ MongoDBObject("foo" -> "bar", "n" -> 5)

      newObj must beDBObject

      newObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b", "foo" -> "bar", "n" -> 5))
    }

    "Support the as[<type>] method" in {
      val dbObj = MongoDBObject(
        "x" -> 5.2,
        "y" -> 9,
        "foo" -> MongoDBList("a", "b", "c"),
        "bar" -> MongoDBObject("baz" -> "foo")
      )
      dbObj must beDBObject

      dbObj.as[Double]("x") must beEqualTo(5.2)
      dbObj.as[Int]("y") must beEqualTo(9)
      dbObj.as[MongoDBList]("foo") must containTheSameElementsAs(List("a", "b", "c"))
      dbObj.as[DBObject]("bar") must haveEntry("baz" -> "foo")
      dbObj.as[String]("nullValue") must throwA[NoSuchElementException]

    }
  }

  "Support 'as' methods for casting by type" should {

    val jodaDate: DateTime = DateTime.now
    val localJodaDate: LocalDateTime = jodaDate.toLocalDateTime
    val jdkDate: JDKDate = new JDKDate(jodaDate.getMillis)

    val dbObj = MongoDBObject("x" -> 5, "y" -> 212.8, "spam" -> "eggs",
      "embedded" -> MongoDBObject("foo" -> "bar"), "none" -> None,
      "jodaDate" -> jodaDate, "localJodaDate" -> localJodaDate, "jdkDate" -> jdkDate,
      "null" -> null)

    "getAs functions as expected" in {
      dbObj.getAs[Int]("x") must beSome[Int](5)
      dbObj.getAs[Double]("y") must beSome[Double](212.8)
      dbObj.getAs[DBObject]("embedded") must beSome[DBObject] and haveSomeEntry("foo" -> "bar")
      dbObj.getAs[Map[String, Any]]("embedded") must beSome[Map[String, Any]]

      dbObj.getAs[DateTime]("jodaDate") must beSome[DateTime](jodaDate)
      dbObj.getAs[LocalDateTime]("localJodaDate") must beSome[LocalDateTime](localJodaDate)
      dbObj.getAs[JDKDate]("jdkDate") must beSome[JDKDate](jdkDate)
      dbObj.getAs[Any]("null") must beSome[Any](None)
      dbObj.getAs[Int]("null") must beNone
    }
    "Should return None for None, failed casts and missing items" in {
      dbObj.getAs[Double]("none") must beNone
      dbObj.getAs[Double]("spam") must beNone
      dbObj.getAs[Float]("omgponies") must beNone
    }
  }

  "as functions as expected" in {
    val dbObj = MongoDBObject("x" -> 5, "y" -> 212.8, "spam" -> "eggs",
      "embedded" -> MongoDBObject("foo" -> "bar"))
    dbObj.as[Int]("x") must beEqualTo(5)
    dbObj.as[Double]("y") must beEqualTo(212.8)
    dbObj.as[DBObject]("embedded") must haveEntry("foo" -> "bar")
    dbObj.as[Map[String, Any]]("embedded") must havePairs(("foo", "bar"))
    dbObj.as[Float]("omgponies") must throwA[NoSuchElementException]
    dbObj.as[Double]("x") must throwA[ClassCastException]

    "the result should be assignable to the type specified" in {
      val y: Double = dbObj.as[Double]("y")
      y must beEqualTo(212.8)
    }
  }

  "Support the nested as[<type>] and getAs[<type>] methods" should {

    val w: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> "C"))
    val x: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> MongoDBObject("C" -> 5)))
    val y: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> MongoDBObject("C" -> List(5, 4, 3, 2, 1))))
    val z: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> MongoDBObject("C" -> List(5, 4, 3, 2, 1)).underlying))

    "Expanding a simple layering should work" in {
      w.as[String]("A", "B") must beEqualTo("C")
      w.as[String]("A.B") must beEqualTo("C")
      w.getAs[String]("A", "B") must beSome[String]("C")
      w.getAs[String]("A.B") must beSome[String]("C")

      "While overexpanding should probably fail" in {
        w.as[String]("A", "B", "C") must throwA[NoSuchElementException]
        w.as[String]("A.B.C") must throwA[NoSuchElementException]
        w.getAs[String]("A", "B", "C") must beNone
        w.getAs[String]("A.B.C") must beNone
      }
    }

    "Expanding a further layering should work" in {
      x.as[Int]("A", "B", "C") must beEqualTo(5)
      x.as[Int]("A.B.C") must beEqualTo(5)
      x.getAs[Int]("A", "B", "C") must beSome[Int](5)
      x.getAs[Int]("A.B.C") must beSome[Int](5)

      "While overexpanding should fail" in {
        x.as[String]("A", "B", "C", "D") must throwA[NoSuchElementException]
        x.as[String]("A.B.C.D") must throwA[NoSuchElementException]
        x.getAs[Int]("A", "B", "C", "D") must beNone
        x.getAs[Int]("A.B.C.D") must beNone
      }
    }

    "And you can go further and even get a list" in {
      y.as[List[Int]]("A", "B", "C") must beEqualTo(List(5, 4, 3, 2, 1))
      y.as[List[Int]]("A.B.C") must beEqualTo(List(5, 4, 3, 2, 1))
      y.getAs[List[Int]]("A", "B", "C") must beSome[List[Int]](List(5, 4, 3, 2, 1))
      y.getAs[List[Int]]("A.B.C") must beSome[List[Int]](List(5, 4, 3, 2, 1))
    }

    "And you can go further and even convert a MongoDBList" in {
      y.as[MongoDBList]("A", "B", "C") must beEqualTo(List(5, 4, 3, 2, 1))
      y.as[MongoDBList]("A.B.C") must beEqualTo(List(5, 4, 3, 2, 1))
      y.getAs[MongoDBList]("A", "B", "C") must beSome[MongoDBList](MongoDBList(5, 4, 3, 2, 1))
      y.getAs[MongoDBList]("A.B.C") must beSome[MongoDBList](MongoDBList(5, 4, 3, 2, 1))
    }

    "And you can go further and even convert a BasicDBList" in {
      y.as[BasicDBList]("A", "B", "C") must beEqualTo(MongoDBList(5, 4, 3, 2, 1).underlying)
      y.as[BasicDBList]("A.B.C") must beEqualTo(MongoDBList(5, 4, 3, 2, 1).underlying)
      y.getAs[BasicDBList]("A", "B", "C") must beSome[BasicDBList]
      y.getAs[BasicDBList]("A.B.C") must beSome[BasicDBList]
    }

    "And MongoDBLists should behave the same as List[_]" in {
      z.as[List[Int]]("A", "B", "C") must beEqualTo(List(5, 4, 3, 2, 1))
      z.as[List[Int]]("A.B.C") must beEqualTo(List(5, 4, 3, 2, 1))
      z.getAs[List[Int]]("A", "B", "C") must beSome[List[Int]](List(5, 4, 3, 2, 1))
      z.getAs[List[Int]]("A.B.C") must beSome[List[Int]](List(5, 4, 3, 2, 1))
      z.as[MongoDBList]("A", "B", "C") must beEqualTo(List(5, 4, 3, 2, 1))
      z.as[MongoDBList]("A.B.C") must beEqualTo(List(5, 4, 3, 2, 1))
      z.getAs[MongoDBList]("A", "B", "C") must beSome[MongoDBList](MongoDBList(5, 4, 3, 2, 1))
      z.getAs[MongoDBList]("A.B.C") must beSome[MongoDBList](MongoDBList(5, 4, 3, 2, 1))
      z.as[BasicDBList]("A", "B", "C") must beEqualTo(MongoDBList(5, 4, 3, 2, 1).underlying)
      z.as[BasicDBList]("A.B.C") must beEqualTo(MongoDBList(5, 4, 3, 2, 1).underlying)
      z.getAs[BasicDBList]("A", "B", "C") must beSome[BasicDBList]
      z.getAs[BasicDBList]("A.B.C") must beSome[BasicDBList]
    }

    "Invalid missing elements should also fail" in {
      z.as[Float]("C", "X") must throwA[NoSuchElementException]
      z.as[Float]("C.X") must throwA[NoSuchElementException]
      z.getAs[Float]("C", "X") must beNone
      z.getAs[Float]("C.X") must beNone
    }
  }

  "Use underlying Object methods" in {
    val control: MongoDBObject = MongoDBObject("foo" -> "bar", "n" -> 2)
    control must beMongoDBObject

    val explicit = control.asDBObject
    explicit must beDBObject
    explicit.toString must beEqualTo(control.toString())
    explicit.hashCode must beEqualTo(control.hashCode())
    explicit.equals(explicit) must beEqualTo(control.equals(control))
  }

  "Support list creation operators" in {
    "Prepend to end of a new list" in {
      "With explicitly created Elements" in {
        val list = MongoDBObject("x" -> "y") :: MongoDBObject("foo" -> "bar")
        list must haveSize(2)
        list(0) must beDBObject
        (list(0): DBObject) must haveEntries("x" -> "y")
        list(1) must beDBObject
        (list(1): DBObject) must haveEntries("foo" -> "bar")
      }
      "With implicitly created Elements with an explicit" in {
        val list = ("x" -> "y") :: MongoDBObject("foo" -> "bar")
        list must haveSize(2)
        list(0) must beDBObject
        (list(0): DBObject) must haveEntries("x" -> "y")
        list(1) must beDBObject
        (list(1): DBObject) must haveEntries("foo" -> "bar")
      }
    }
  }

  "Map values saved as DBObject values" should {
    "convert from the MongoDBObject constructor" in {
      val dbObj = MongoDBObject("foo" -> "bar", "x" -> 5,
        "map" -> Map("spam" -> 8.2, "eggs" -> "bacon"))
      val map: Option[DBObject] = dbObj.getAs[DBObject]("map")
      map must beSome[DBObject]
    }
    "convert from the MongoDBObjectBuilder" in {
      val b = MongoDBObject.newBuilder
      b += "foo" -> "bar"
      b += "x" -> 5
      b += "map" -> Map("spam" -> 8.2, "eggs" -> "bacon")
      val dbObj = b.result()
      val map: Option[DBObject] = dbObj.getAs[DBObject]("map")
      map.orNull must beDBObject
    }
    "convert from the put method" in {
      val dbObj = MongoDBObject("foo" -> "bar", "x" -> 5)
      dbObj += ("map" -> Map("spam" -> 8.2, "eggs" -> "bacon"))
      val map: Option[DBObject] = dbObj.getAs[DBObject]("map")
      map.orNull must beDBObject
    }
  }
}

