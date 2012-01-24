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

package com.mongodb.casbah.test.commons

import com.mongodb.casbah.commons._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification

class MongoDBObjectSpec extends CasbahMutableSpecification {

  "MongoDBObject expand operations" should {

    val x: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> "C"))
    val y: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> MongoDBObject("C" -> 5)))
    val z: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> MongoDBObject("C" -> List(5, 4, 3, 2, 1))))

    "Expanding a simple layering should work" in {
      val b = x.expand[String]("A.B")
      b must beSome("C")

      "While overexpanding should probably fail" in {
        lazy val bFail = x.expand[String]("A.B.C")
        bFail must throwA[ClassCastException]
      }
    }

    "Expanding a further layering should work" in {
      // TODO - This is way broken as far as casting to int
      val c = y.expand[Int]("A.B.C")
      c must beSome(5)
      "While overexpanding should probably fail" in {
        lazy val cFail = y.expand[String]("A.B.C.D")
        cFail must throwA[ClassCastException]
      }
    }

    "And you can go further and even get a list" in {
      // TODO - This is way broken as far as casting to int
      val c = z.expand[List[_]]("A.B.C")
      c must beSome(List(5, 4, 3, 2, 1))
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

    /*"SCALA-42, storing Java Arrays in a DBObject shouldn't break .equals and .hashcode" in {
      val one = MongoDBObject("anArray" -> Array(MongoDBObject("one" -> "oneVal"), MongoDBObject("two" -> "twoVal")))
      val two = MongoDBObject("anArray" -> Array(MongoDBObject("one" -> "oneVal"), MongoDBObject("two" -> "twoVal")))
      one must beEqualTo(two)
    }*/
  }

  "MongoDBObject Factory & Builder" should {
    "Support 'empty', returning a DBObject" in {
      val dbObj = MongoDBObject.empty

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
  }

  "MongoDBObject" >> {
    "Support additivity of Tuple Entrys" >> {
      "A single Tuple Entry with + " in {
        // TODO - you currently have to explicitly cast this or get back a map. ugh.
        val newObj: DBObject = MongoDBObject("x" -> "y", "a" -> "b") + ("foo" -> "bar")
        newObj must haveEntries("x" -> "y", "a" -> "b", "foo" -> "bar")
      }
      "A list of Tuple Entrys with ++ " in {
        val newObj = MongoDBObject("x" -> "y", "a" -> "b") ++ ("foo" -> "bar", "n" -> 5)
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
      val dbObj = MongoDBObject("x" -> 5.2,
        "y" -> 9,
        "foo" -> MongoDBList("a", "b", "c"),
        "bar" -> MongoDBObject("baz" -> "foo"))
      dbObj must beDBObject

      dbObj.as[Double]("x") must beEqualTo(5.2)
      dbObj.as[Int]("y") must beEqualTo(9)
      dbObj.as[MongoDBList]("foo") must haveTheSameElementsAs(List("a", "b", "c"))
      dbObj.as[DBObject]("bar") must haveEntry("baz" -> "foo")
      dbObj.as[String]("nullValue") must throwA[NoSuchElementException]

//    DOES NOT COMPILE ANYMORE      
//    (dbObj.as("x"):Any) must throwA[IllegalArgumentException]
    }

    "Support 'as' methods for casting by type" in {
      "getAs functions as expected" in {
        val dbObj = MongoDBObject("x" -> 5, "y" -> 212.8, "spam" -> "eggs",
          "embedded" -> MongoDBObject("foo" -> "bar"))
        dbObj.getAs[Int]("x") must beSome[Int].which(_ == 5)
        dbObj.getAs[Double]("y") must beSome[Double].which(_ == 212.8)
        dbObj.getAs[DBObject]("embedded") must beSome[DBObject] and haveSomeEntry("foo" -> "bar")
        dbObj.getAs[Float]("omgponies") must beNone
        dbObj.getAs[Double]("x").get must throwA[ClassCastException]

//      DOES NOT COMPILE ANYMORE
//      (dbObj.getAs("x"):Any) must throwA[IllegalArgumentException]
      }

      "as functions as expected" in {
        val dbObj = MongoDBObject("x" -> 5, "y" -> 212.8, "spam" -> "eggs",
          "embedded" -> MongoDBObject("foo" -> "bar"))
        dbObj.as[Int]("x") must beEqualTo(5)
        dbObj.as[Double]("y") must beEqualTo(212.8)
        dbObj.as[DBObject]("embedded") must haveEntry("foo" -> "bar")
        dbObj.as[Float]("omgponies") must throwA[NoSuchElementException]
        dbObj.as[Double]("x") must throwA[ClassCastException]

        "the result should be assignable to the type specified" in {
          val y: Double = dbObj.as[Double]("y")
          y must beEqualTo(212.8)
        }
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
  }

}

// vim: set ts=2 sw=2 sts=2 et:
