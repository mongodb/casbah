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
package test 

import com.mongodb.casbah.commons.Imports._


import org.specs._
import org.specs.specification.PendingUntilFixed

class MongoDBObjectSpec extends Specification with PendingUntilFixed {
  "MongoDBObject expand operations" should {
    shareVariables()

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
      q must haveSuperClass[DBObject]
      fields must haveSuperClass[DBObject]
    }

  }

  "MongoDBObject Factory & Builder" should {
    "Support 'empty', returning a DBObject" in {
      val dbObj = MongoDBObject.empty

      dbObj must haveSuperClass[DBObject]
      dbObj must haveSize(0)
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

      dbObj must haveSuperClass[DBObject]
      jObj must haveSuperClass[DBObject]
      dbObj must beEqualTo(jObj)
    }
    "Support a 2.8 builder interface which returns a DBObject" in {
      val builder = MongoDBObject.newBuilder

      builder += "foo" -> "bar"
      builder += "x" -> 5
      builder += "y" -> 212.8

      builder ++= List("spam" -> "eggs", "type erasure" -> "sucks", "omg" -> "ponies!")

      val dbObj = builder.result

      dbObj must haveSuperClass[DBObject]
      dbObj must haveSize(6)
    }
  }

  "MongoDBObject type conversions" should {
    "Support converting Maps of [String, Any] to DBObjects" in {
      val control: DBObject = MongoDBObject("foo" -> "bar", "n" -> 2)
      control must haveSuperClass[DBObject]

      val map = Map("foo" -> "bar", "n" -> 2)

      val cast: DBObject = map

      cast must haveSuperClass[DBObject]
      cast must beEqualTo(control)

      val explicit = map.asDBObject

      explicit must haveSuperClass[DBObject]
      explicit must beEqualTo(control)
    }
  }

  "MongoDBObject" should {
    "Support additivity of Tuple Pairs" in {
      "A single Tuple pair with + " in {
        // Note - you currently have to explicitly cast this or get back a map. ugh.
        val newObj: DBObject = MongoDBObject("x" -> "y", "a" -> "b") + ("foo" -> "bar")
        newObj must notBeNull
        newObj must haveSuperClass[DBObject]

        newObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b", "foo" -> "bar"))
      }
      "A list of Tuple pairs with ++ " in {
        // Note - you currently have to explicitly cast this or get back a map. ugh.
        val newObj: DBObject = MongoDBObject("x" -> "y", "a" -> "b") ++ ("foo" -> "bar", "n" -> 5)
        newObj must notBeNull
        newObj must haveSuperClass[DBObject]

        newObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b", "foo" -> "bar", "n" -> 5))
      }
      "Merging a single tuple via += " in {
        val dbObj = MongoDBObject("x" -> "y", "a" -> "b") 
        dbObj must notBeNull
        dbObj must haveSuperClass[DBObject]
        dbObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b"))

        dbObj += ("foo" -> "bar")

        dbObj must notBeNull
        dbObj must haveSuperClass[DBObject]

        dbObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b", "foo" -> "bar"))

      }
      "Merging a set of tuples via ++= " in {
        val dbObj = MongoDBObject("x" -> "y", "a" -> "b") 
        dbObj must notBeNull
        dbObj must haveSuperClass[DBObject]
        dbObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b"))

        dbObj += ("foo" -> "bar", "n" -> 5.asInstanceOf[AnyRef], "fbc" -> 542542.2.asInstanceOf[AnyRef])

        dbObj must notBeNull
        dbObj must haveSuperClass[DBObject]

        dbObj must beEqualTo(MongoDBObject("x" -> "y", "a" -> "b", "foo" -> "bar", "n" -> 5, "fbc" -> 542542.2))

      }
    }

    "Support the as[<type>] method" in {
      val dbObj = MongoDBObject("x" -> 5.2, 
                                "y" -> 9, 
                                "foo" -> ("a", "b", "c"), 
                                "bar" -> MongoDBObject("baz" -> "foo"))
      dbObj must notBeNull
      dbObj must haveSuperClass[DBObject]

      dbObj.as[Double]("x") must notBeNull
      dbObj.as[Int]("y") must notBeNull
      dbObj.as[Seq[_]]("foo") must notBeNull
      dbObj.as[DBObject]("bar") must notBeNull
      dbObj.as[String]("nullValue") must throwA[NoSuchElementException]
    }
  }

}


// vim: set ts=2 sw=2 sts=2 et:
