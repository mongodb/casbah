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
package query

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.conversions.scala._

import org.scala_tools.time.Imports._

import org.specs._
import org.specs.specification.PendingUntilFixed

class DSLCoreOperatorsSpec extends Specification with PendingUntilFixed with Logging {

  def nonDSL(key: String, oper: String, value: Any) = MongoDBObject(key -> MongoDBObject(oper -> value))

  "Casbah's DSL $ne operator" should {
    shareVariables

    "Accept a right hand value of String" in {
      val neStr = "foo" $ne "ISBN-123456789"
      neStr must notBeNull
      neStr must haveSuperClass[DBObject]
      neStr must beEqualTo(nonDSL("foo", "$ne", "ISBN-123456789"))
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $ne new BasicDBObject("bar", "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$ne", new BasicDBObject("bar", "baz")))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $ne MongoDBObject("bar" -> "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$ne", MongoDBObject("bar" -> "baz")))
      }
      "A DBList should work also" in {
        val neLst = "foo" $ne MongoDBList("x", "y", "z")
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$ne", MongoDBList("x", "y", "z")))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $ne List("x", "y", 5)
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$ne", List("x", "y", 5)))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $ne Seq("x", "y", 5)
        neSeq must notBeNull
        neSeq must haveSuperClass[DBObject]
        neSeq must beEqualTo(nonDSL("foo", "$ne", Seq("x", "y", 5)))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $ne Set("x", "y", 5)
        neSet must notBeNull
        neSet must haveSuperClass[DBObject]
        /*neSet must beEqualTo(nonDSL("foo", "$ne", Set("x", "y", 5)))*/
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $ne HashSet("x", "y", 5)
        neHashSet must notBeNull
        neHashSet must haveSuperClass[DBObject]
        /*neHashSet must beEqualTo(nonDSL("foo", "$ne", HashSet("x", "y", 5)))*/
      }

      "Also, Arrays function" in {
        val neArray = "foo" $ne Array("x", "y", 5)
        neArray must notBeNull
        neArray must haveSuperClass[DBObject]
      }
    }

    "Accept a right hand value of Numeric" in {
      "with Int" in {
        val neInt = "foo" $ne 10
        neInt.toString // Test that JSON Serialization works
        neInt must notBeNull
        neInt must haveSuperClass[DBObject]
        neInt must beEqualTo(nonDSL("foo", "$ne", 10))
      }
      "with BigDecimal" in {
        val neBD = "foo" $ne BigDecimal("5.8233232")
        neBD.toString // Test that JSON Serialization works
        neBD must notBeNull
        neBD must haveSuperClass[DBObject]
        neBD must beEqualTo(nonDSL("foo", "$ne", BigDecimal("5.8233232")))
      }
      "with BigInt" in {
        val neBI = "foo" $ne BigInt("1000000000000000000425425245252")
        neBI.toString // Test that JSON Serialization works
        neBI must notBeNull
        neBI must haveSuperClass[DBObject]
        neBI must beEqualTo(nonDSL("foo", "$ne",  BigInt("1000000000000000000425425245252")))
      }
      "with Byte" in {
        val neByte = "foo" $ne java.lang.Byte.parseByte("51")
        neByte.toString // Test that JSON Serialization works
        neByte must notBeNull
        neByte must haveSuperClass[DBObject]
        neByte must beEqualTo(nonDSL("foo", "$ne", java.lang.Byte.parseByte("51")))
      }
      "with Double" in {
        val neDouble = "foo" $ne 5.232352
        neDouble.toString // Test that JSON Serialization works
        neDouble must notBeNull
        neDouble must haveSuperClass[DBObject]
        neDouble must beEqualTo(nonDSL("foo", "$ne", 5.232352))
      }
      "with Float" in {
        val neFloat = "foo" $ne java.lang.Float.parseFloat("5.232352")
        neFloat.toString // Test that JSON Serialization works
        neFloat must notBeNull
        neFloat must haveSuperClass[DBObject]
        neFloat must beEqualTo(nonDSL("foo", "$ne", java.lang.Float.parseFloat("5.232352")))
      }
      "with Long" in {
        val neLong = "foo" $ne 10L
        neLong.toString // Test that JSON Serialization works
        neLong must notBeNull
        neLong must haveSuperClass[DBObject]
        neLong must beEqualTo(nonDSL("foo", "$ne", 10L))
      }
      "with Short" in {
        val neShort = "foo" $ne java.lang.Short.parseShort("10")
        neShort.toString // Test that JSON Serialization works
        neShort must notBeNull
        neShort must haveSuperClass[DBObject]
        neShort must beEqualTo(nonDSL("foo", "$ne", java.lang.Short.parseShort("10")))
      }
      // [info]       json can't serialize type : class java.lang.Character (JSON.java:203)
      /*"with Char" in {
        val neChar = "foo" $ne 'x'
        neChar must notBeNull
        neChar must haveSuperClass[DBObject]
        neChar must beEqualTo(nonDSL("foo", "$ne", 'x'))
      }*/

    }
  

  }

}
// vim: set ts=2 sw=2 sts=2 et:
