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

@SuppressWarnings(Array("deprecation"))
class DSLCoreOperatorsSpec extends Specification with PendingUntilFixed with Logging {

  def nonDSL(key: String, oper: String, value: Any) = MongoDBObject(key -> MongoDBObject(oper -> value))

  "Casbah's DSL $ne operator" should {
    shareVariables

    val testDate = new java.util.Date(109, 01, 02, 0, 0, 0)

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

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $ne 10
        neInt.toString must notBeNull // Test that JSON Serialization works
        neInt must notBeNull
        neInt must haveSuperClass[DBObject]
        neInt must beEqualTo(nonDSL("foo", "$ne", 10))
      }
      "with BigDecimal" in {
        val neBD = "foo" $ne BigDecimal("5.8233232")
        neBD.toString must notBeNull // Test that JSON Serialization works
        neBD must notBeNull
        neBD must haveSuperClass[DBObject]
        neBD must beEqualTo(nonDSL("foo", "$ne", BigDecimal("5.8233232")))
      }
      "with BigInt" in {
        val neBI = "foo" $ne BigInt("1000000000000000000425425245252")
        neBI.toString must notBeNull // Test that JSON Serialization works
        neBI must notBeNull
        neBI must haveSuperClass[DBObject]
        neBI must beEqualTo(nonDSL("foo", "$ne",  BigInt("1000000000000000000425425245252")))
      }
      "with Byte" in {
        val neByte = "foo" $ne java.lang.Byte.parseByte("51")
        neByte.toString must notBeNull // Test that JSON Serialization works
        neByte must notBeNull
        neByte must haveSuperClass[DBObject]
        neByte must beEqualTo(nonDSL("foo", "$ne", java.lang.Byte.parseByte("51")))
      }
      "with Double" in {
        val neDouble = "foo" $ne 5.232352
        neDouble.toString must notBeNull // Test that JSON Serialization works
        neDouble must notBeNull
        neDouble must haveSuperClass[DBObject]
        neDouble must beEqualTo(nonDSL("foo", "$ne", 5.232352))
      }
      "with Float" in {
        val neFloat = "foo" $ne java.lang.Float.parseFloat("5.232352")
        neFloat.toString must notBeNull // Test that JSON Serialization works
        neFloat must notBeNull
        neFloat must haveSuperClass[DBObject]
        neFloat must beEqualTo(nonDSL("foo", "$ne", java.lang.Float.parseFloat("5.232352")))
      }
      "with Long" in {
        val neLong = "foo" $ne 10L
        neLong.toString must notBeNull // Test that JSON Serialization works
        neLong must notBeNull
        neLong must haveSuperClass[DBObject]
        neLong must beEqualTo(nonDSL("foo", "$ne", 10L))
      }
      "with Short" in {
        val neShort = "foo" $ne java.lang.Short.parseShort("10")
        neShort.toString must notBeNull // Test that JSON Serialization works
        neShort must notBeNull
        neShort must haveSuperClass[DBObject]
        neShort must beEqualTo(nonDSL("foo", "$ne", java.lang.Short.parseShort("10")))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $ne testDate
        neJDKDate.toString must notBeNull // Test that JSON Serialization works
        neJDKDate must notBeNull
        neJDKDate must haveSuperClass[DBObject]
        neJDKDate must beEqualTo(nonDSL("foo", "$ne", testDate))
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $ne new org.joda.time.DateTime(testDate.getTime)
        neJodaDT.toString must notBeNull // Test that JSON Serialization works
        neJodaDT must notBeNull
        neJodaDT must haveSuperClass[DBObject]
        neJodaDT must beEqualTo(nonDSL("foo", "$ne", new org.joda.time.DateTime(testDate.getTime)))
        DeregisterJodaTimeConversionHelpers()
      }

    }
  }

  "Casbah's DSL $lt operator" should {
    shareVariables

    val testDate = new java.util.Date(109, 01, 02, 0, 0, 0)

    "Accept a right hand value of String" in {
      val neStr = "foo" $lt "ISBN-123456789"
      neStr must notBeNull
      neStr must haveSuperClass[DBObject]
      neStr must beEqualTo(nonDSL("foo", "$lt", "ISBN-123456789"))
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $lt new BasicDBObject("bar", "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$lt", new BasicDBObject("bar", "baz")))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $lt MongoDBObject("bar" -> "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$lt", MongoDBObject("bar" -> "baz")))
      }
      "A DBList should work also" in {
        val neLst = "foo" $lt MongoDBList("x", "y", "z")
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$lt", MongoDBList("x", "y", "z")))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $lt List("x", "y", 5)
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$lt", List("x", "y", 5)))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $lt Seq("x", "y", 5)
        neSeq must notBeNull
        neSeq must haveSuperClass[DBObject]
        neSeq must beEqualTo(nonDSL("foo", "$lt", Seq("x", "y", 5)))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $lt Set("x", "y", 5)
        neSet must notBeNull
        neSet must haveSuperClass[DBObject]
        /*neSet must beEqualTo(nonDSL("foo", "$lt", Set("x", "y", 5)))*/
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $lt HashSet("x", "y", 5)
        neHashSet must notBeNull
        neHashSet must haveSuperClass[DBObject]
        /*neHashSet must beEqualTo(nonDSL("foo", "$lt", HashSet("x", "y", 5)))*/
      }

      "Also, Arrays function" in {
        val neArray = "foo" $lt Array("x", "y", 5)
        neArray must notBeNull
        neArray must haveSuperClass[DBObject]
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $lt 10
        neInt.toString must notBeNull // Test that JSON Serialization works
        neInt must notBeNull
        neInt must haveSuperClass[DBObject]
        neInt must beEqualTo(nonDSL("foo", "$lt", 10))
      }
      "with BigDecimal" in {
        val neBD = "foo" $lt BigDecimal("5.8233232")
        neBD.toString must notBeNull // Test that JSON Serialization works
        neBD must notBeNull
        neBD must haveSuperClass[DBObject]
        neBD must beEqualTo(nonDSL("foo", "$lt", BigDecimal("5.8233232")))
      }
      "with BigInt" in {
        val neBI = "foo" $lt BigInt("1000000000000000000425425245252")
        neBI.toString must notBeNull // Test that JSON Serialization works
        neBI must notBeNull
        neBI must haveSuperClass[DBObject]
        neBI must beEqualTo(nonDSL("foo", "$lt",  BigInt("1000000000000000000425425245252")))
      }
      "with Byte" in {
        val neByte = "foo" $lt java.lang.Byte.parseByte("51")
        neByte.toString must notBeNull // Test that JSON Serialization works
        neByte must notBeNull
        neByte must haveSuperClass[DBObject]
        neByte must beEqualTo(nonDSL("foo", "$lt", java.lang.Byte.parseByte("51")))
      }
      "with Double" in {
        val neDouble = "foo" $lt 5.232352
        neDouble.toString must notBeNull // Test that JSON Serialization works
        neDouble must notBeNull
        neDouble must haveSuperClass[DBObject]
        neDouble must beEqualTo(nonDSL("foo", "$lt", 5.232352))
      }
      "with Float" in {
        val neFloat = "foo" $lt java.lang.Float.parseFloat("5.232352")
        neFloat.toString must notBeNull // Test that JSON Serialization works
        neFloat must notBeNull
        neFloat must haveSuperClass[DBObject]
        neFloat must beEqualTo(nonDSL("foo", "$lt", java.lang.Float.parseFloat("5.232352")))
      }
      "with Long" in {
        val neLong = "foo" $lt 10L
        neLong.toString must notBeNull // Test that JSON Serialization works
        neLong must notBeNull
        neLong must haveSuperClass[DBObject]
        neLong must beEqualTo(nonDSL("foo", "$lt", 10L))
      }
      "with Short" in {
        val neShort = "foo" $lt java.lang.Short.parseShort("10")
        neShort.toString must notBeNull // Test that JSON Serialization works
        neShort must notBeNull
        neShort must haveSuperClass[DBObject]
        neShort must beEqualTo(nonDSL("foo", "$lt", java.lang.Short.parseShort("10")))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $lt testDate
        neJDKDate.toString must notBeNull // Test that JSON Serialization works
        neJDKDate must notBeNull
        neJDKDate must haveSuperClass[DBObject]
        neJDKDate must beEqualTo(nonDSL("foo", "$lt", testDate))
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $lt new org.joda.time.DateTime(testDate.getTime)
        neJodaDT.toString must notBeNull // Test that JSON Serialization works
        neJodaDT must notBeNull
        neJodaDT must haveSuperClass[DBObject]
        neJodaDT must beEqualTo(nonDSL("foo", "$lt", new org.joda.time.DateTime(testDate.getTime)))
        DeregisterJodaTimeConversionHelpers()
      }

    }
  }

  "Casbah's DSL $lte operator" should {
    shareVariables

    val testDate = new java.util.Date(109, 01, 02, 0, 0, 0)

    "Accept a right hand value of String" in {
      val neStr = "foo" $lte "ISBN-123456789"
      neStr must notBeNull
      neStr must haveSuperClass[DBObject]
      neStr must beEqualTo(nonDSL("foo", "$lte", "ISBN-123456789"))
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $lte new BasicDBObject("bar", "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$lte", new BasicDBObject("bar", "baz")))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $lte MongoDBObject("bar" -> "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$lte", MongoDBObject("bar" -> "baz")))
      }
      "A DBList should work also" in {
        val neLst = "foo" $lte MongoDBList("x", "y", "z")
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$lte", MongoDBList("x", "y", "z")))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $lte List("x", "y", 5)
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$lte", List("x", "y", 5)))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $lte Seq("x", "y", 5)
        neSeq must notBeNull
        neSeq must haveSuperClass[DBObject]
        neSeq must beEqualTo(nonDSL("foo", "$lte", Seq("x", "y", 5)))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $lte Set("x", "y", 5)
        neSet must notBeNull
        neSet must haveSuperClass[DBObject]
        /*neSet must beEqualTo(nonDSL("foo", "$lte", Set("x", "y", 5)))*/
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $lte HashSet("x", "y", 5)
        neHashSet must notBeNull
        neHashSet must haveSuperClass[DBObject]
        /*neHashSet must beEqualTo(nonDSL("foo", "$lte", HashSet("x", "y", 5)))*/
      }

      "Also, Arrays function" in {
        val neArray = "foo" $lte Array("x", "y", 5)
        neArray must notBeNull
        neArray must haveSuperClass[DBObject]
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $lte 10
        neInt.toString must notBeNull // Test that JSON Serialization works
        neInt must notBeNull
        neInt must haveSuperClass[DBObject]
        neInt must beEqualTo(nonDSL("foo", "$lte", 10))
      }
      "with BigDecimal" in {
        val neBD = "foo" $lte BigDecimal("5.8233232")
        neBD.toString must notBeNull // Test that JSON Serialization works
        neBD must notBeNull
        neBD must haveSuperClass[DBObject]
        neBD must beEqualTo(nonDSL("foo", "$lte", BigDecimal("5.8233232")))
      }
      "with BigInt" in {
        val neBI = "foo" $lte BigInt("1000000000000000000425425245252")
        neBI.toString must notBeNull // Test that JSON Serialization works
        neBI must notBeNull
        neBI must haveSuperClass[DBObject]
        neBI must beEqualTo(nonDSL("foo", "$lte",  BigInt("1000000000000000000425425245252")))
      }
      "with Byte" in {
        val neByte = "foo" $lte java.lang.Byte.parseByte("51")
        neByte.toString must notBeNull // Test that JSON Serialization works
        neByte must notBeNull
        neByte must haveSuperClass[DBObject]
        neByte must beEqualTo(nonDSL("foo", "$lte", java.lang.Byte.parseByte("51")))
      }
      "with Double" in {
        val neDouble = "foo" $lte 5.232352
        neDouble.toString must notBeNull // Test that JSON Serialization works
        neDouble must notBeNull
        neDouble must haveSuperClass[DBObject]
        neDouble must beEqualTo(nonDSL("foo", "$lte", 5.232352))
      }
      "with Float" in {
        val neFloat = "foo" $lte java.lang.Float.parseFloat("5.232352")
        neFloat.toString must notBeNull // Test that JSON Serialization works
        neFloat must notBeNull
        neFloat must haveSuperClass[DBObject]
        neFloat must beEqualTo(nonDSL("foo", "$lte", java.lang.Float.parseFloat("5.232352")))
      }
      "with Long" in {
        val neLong = "foo" $lte 10L
        neLong.toString must notBeNull // Test that JSON Serialization works
        neLong must notBeNull
        neLong must haveSuperClass[DBObject]
        neLong must beEqualTo(nonDSL("foo", "$lte", 10L))
      }
      "with Short" in {
        val neShort = "foo" $lte java.lang.Short.parseShort("10")
        neShort.toString must notBeNull // Test that JSON Serialization works
        neShort must notBeNull
        neShort must haveSuperClass[DBObject]
        neShort must beEqualTo(nonDSL("foo", "$lte", java.lang.Short.parseShort("10")))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $lte testDate
        neJDKDate.toString must notBeNull // Test that JSON Serialization works
        neJDKDate must notBeNull
        neJDKDate must haveSuperClass[DBObject]
        neJDKDate must beEqualTo(nonDSL("foo", "$lte", testDate))
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $lte new org.joda.time.DateTime(testDate.getTime)
        neJodaDT.toString must notBeNull // Test that JSON Serialization works
        neJodaDT must notBeNull
        neJodaDT must haveSuperClass[DBObject]
        neJodaDT must beEqualTo(nonDSL("foo", "$lte", new org.joda.time.DateTime(testDate.getTime)))
        DeregisterJodaTimeConversionHelpers()
      }

    }
  }

  "Casbah's DSL $gt operator" should {
    shareVariables

    val testDate = new java.util.Date(109, 01, 02, 0, 0, 0)

    "Accept a right hand value of String" in {
      val neStr = "foo" $gt "ISBN-123456789"
      neStr must notBeNull
      neStr must haveSuperClass[DBObject]
      neStr must beEqualTo(nonDSL("foo", "$gt", "ISBN-123456789"))
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $gt new BasicDBObject("bar", "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$gt", new BasicDBObject("bar", "baz")))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $gt MongoDBObject("bar" -> "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$gt", MongoDBObject("bar" -> "baz")))
      }
      "A DBList should work also" in {
        val neLst = "foo" $gt MongoDBList("x", "y", "z")
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$gt", MongoDBList("x", "y", "z")))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $gt List("x", "y", 5)
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$gt", List("x", "y", 5)))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $gt Seq("x", "y", 5)
        neSeq must notBeNull
        neSeq must haveSuperClass[DBObject]
        neSeq must beEqualTo(nonDSL("foo", "$gt", Seq("x", "y", 5)))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $gt Set("x", "y", 5)
        neSet must notBeNull
        neSet must haveSuperClass[DBObject]
        /*neSet must beEqualTo(nonDSL("foo", "$gt", Set("x", "y", 5)))*/
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $gt HashSet("x", "y", 5)
        neHashSet must notBeNull
        neHashSet must haveSuperClass[DBObject]
        /*neHashSet must beEqualTo(nonDSL("foo", "$gt", HashSet("x", "y", 5)))*/
      }

      "Also, Arrays function" in {
        val neArray = "foo" $gt Array("x", "y", 5)
        neArray must notBeNull
        neArray must haveSuperClass[DBObject]
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $gt 10
        neInt.toString must notBeNull // Test that JSON Serialization works
        neInt must notBeNull
        neInt must haveSuperClass[DBObject]
        neInt must beEqualTo(nonDSL("foo", "$gt", 10))
      }
      "with BigDecimal" in {
        val neBD = "foo" $gt BigDecimal("5.8233232")
        neBD.toString must notBeNull // Test that JSON Serialization works
        neBD must notBeNull
        neBD must haveSuperClass[DBObject]
        neBD must beEqualTo(nonDSL("foo", "$gt", BigDecimal("5.8233232")))
      }
      "with BigInt" in {
        val neBI = "foo" $gt BigInt("1000000000000000000425425245252")
        neBI.toString must notBeNull // Test that JSON Serialization works
        neBI must notBeNull
        neBI must haveSuperClass[DBObject]
        neBI must beEqualTo(nonDSL("foo", "$gt",  BigInt("1000000000000000000425425245252")))
      }
      "with Byte" in {
        val neByte = "foo" $gt java.lang.Byte.parseByte("51")
        neByte.toString must notBeNull // Test that JSON Serialization works
        neByte must notBeNull
        neByte must haveSuperClass[DBObject]
        neByte must beEqualTo(nonDSL("foo", "$gt", java.lang.Byte.parseByte("51")))
      }
      "with Double" in {
        val neDouble = "foo" $gt 5.232352
        neDouble.toString must notBeNull // Test that JSON Serialization works
        neDouble must notBeNull
        neDouble must haveSuperClass[DBObject]
        neDouble must beEqualTo(nonDSL("foo", "$gt", 5.232352))
      }
      "with Float" in {
        val neFloat = "foo" $gt java.lang.Float.parseFloat("5.232352")
        neFloat.toString must notBeNull // Test that JSON Serialization works
        neFloat must notBeNull
        neFloat must haveSuperClass[DBObject]
        neFloat must beEqualTo(nonDSL("foo", "$gt", java.lang.Float.parseFloat("5.232352")))
      }
      "with Long" in {
        val neLong = "foo" $gt 10L
        neLong.toString must notBeNull // Test that JSON Serialization works
        neLong must notBeNull
        neLong must haveSuperClass[DBObject]
        neLong must beEqualTo(nonDSL("foo", "$gt", 10L))
      }
      "with Short" in {
        val neShort = "foo" $gt java.lang.Short.parseShort("10")
        neShort.toString must notBeNull // Test that JSON Serialization works
        neShort must notBeNull
        neShort must haveSuperClass[DBObject]
        neShort must beEqualTo(nonDSL("foo", "$gt", java.lang.Short.parseShort("10")))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $gt testDate
        neJDKDate.toString must notBeNull // Test that JSON Serialization works
        neJDKDate must notBeNull
        neJDKDate must haveSuperClass[DBObject]
        neJDKDate must beEqualTo(nonDSL("foo", "$gt", testDate))
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $gt new org.joda.time.DateTime(testDate.getTime)
        neJodaDT.toString must notBeNull // Test that JSON Serialization works
        neJodaDT must notBeNull
        neJodaDT must haveSuperClass[DBObject]
        neJodaDT must beEqualTo(nonDSL("foo", "$gt", new org.joda.time.DateTime(testDate.getTime)))
        DeregisterJodaTimeConversionHelpers()
      }

    }
  }


  "Casbah's DSL $gte operator" should {
    shareVariables

    val testDate = new java.util.Date(109, 01, 02, 0, 0, 0)

    "Accept a right hand value of String" in {
      val neStr = "foo" $gte "ISBN-123456789"
      neStr must notBeNull
      neStr must haveSuperClass[DBObject]
      neStr must beEqualTo(nonDSL("foo", "$gte", "ISBN-123456789"))
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $gte new BasicDBObject("bar", "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$gte", new BasicDBObject("bar", "baz")))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $gte MongoDBObject("bar" -> "baz")
        neObj must notBeNull
        neObj must haveSuperClass[DBObject]
        neObj must beEqualTo(nonDSL("foo", "$gte", MongoDBObject("bar" -> "baz")))
      }
      "A DBList should work also" in {
        val neLst = "foo" $gte MongoDBList("x", "y", "z")
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$gte", MongoDBList("x", "y", "z")))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $gte List("x", "y", 5)
        neLst must notBeNull
        neLst must haveSuperClass[DBObject]
        neLst must beEqualTo(nonDSL("foo", "$gte", List("x", "y", 5)))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $gte Seq("x", "y", 5)
        neSeq must notBeNull
        neSeq must haveSuperClass[DBObject]
        neSeq must beEqualTo(nonDSL("foo", "$gte", Seq("x", "y", 5)))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $gte Set("x", "y", 5)
        neSet must notBeNull
        neSet must haveSuperClass[DBObject]
        /*neSet must beEqualTo(nonDSL("foo", "$gte", Set("x", "y", 5)))*/
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $gte HashSet("x", "y", 5)
        neHashSet must notBeNull
        neHashSet must haveSuperClass[DBObject]
        /*neHashSet must beEqualTo(nonDSL("foo", "$gte", HashSet("x", "y", 5)))*/
      }

      "Also, Arrays function" in {
        val neArray = "foo" $gte Array("x", "y", 5)
        neArray must notBeNull
        neArray must haveSuperClass[DBObject]
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $gte 10
        neInt.toString must notBeNull // Test that JSON Serialization works
        neInt must notBeNull
        neInt must haveSuperClass[DBObject]
        neInt must beEqualTo(nonDSL("foo", "$gte", 10))
      }
      "with BigDecimal" in {
        val neBD = "foo" $gte BigDecimal("5.8233232")
        neBD.toString must notBeNull // Test that JSON Serialization works
        neBD must notBeNull
        neBD must haveSuperClass[DBObject]
        neBD must beEqualTo(nonDSL("foo", "$gte", BigDecimal("5.8233232")))
      }
      "with BigInt" in {
        val neBI = "foo" $gte BigInt("1000000000000000000425425245252")
        neBI.toString must notBeNull // Test that JSON Serialization works
        neBI must notBeNull
        neBI must haveSuperClass[DBObject]
        neBI must beEqualTo(nonDSL("foo", "$gte",  BigInt("1000000000000000000425425245252")))
      }
      "with Byte" in {
        val neByte = "foo" $gte java.lang.Byte.parseByte("51")
        neByte.toString must notBeNull // Test that JSON Serialization works
        neByte must notBeNull
        neByte must haveSuperClass[DBObject]
        neByte must beEqualTo(nonDSL("foo", "$gte", java.lang.Byte.parseByte("51")))
      }
      "with Double" in {
        val neDouble = "foo" $gte 5.232352
        neDouble.toString must notBeNull // Test that JSON Serialization works
        neDouble must notBeNull
        neDouble must haveSuperClass[DBObject]
        neDouble must beEqualTo(nonDSL("foo", "$gte", 5.232352))
      }
      "with Float" in {
        val neFloat = "foo" $gte java.lang.Float.parseFloat("5.232352")
        neFloat.toString must notBeNull // Test that JSON Serialization works
        neFloat must notBeNull
        neFloat must haveSuperClass[DBObject]
        neFloat must beEqualTo(nonDSL("foo", "$gte", java.lang.Float.parseFloat("5.232352")))
      }
      "with Long" in {
        val neLong = "foo" $gte 10L
        neLong.toString must notBeNull // Test that JSON Serialization works
        neLong must notBeNull
        neLong must haveSuperClass[DBObject]
        neLong must beEqualTo(nonDSL("foo", "$gte", 10L))
      }
      "with Short" in {
        val neShort = "foo" $gte java.lang.Short.parseShort("10")
        neShort.toString must notBeNull // Test that JSON Serialization works
        neShort must notBeNull
        neShort must haveSuperClass[DBObject]
        neShort must beEqualTo(nonDSL("foo", "$gte", java.lang.Short.parseShort("10")))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $gte testDate
        neJDKDate.toString must notBeNull // Test that JSON Serialization works
        neJDKDate must notBeNull
        neJDKDate must haveSuperClass[DBObject]
        neJDKDate must beEqualTo(nonDSL("foo", "$gte", testDate))
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $gte new org.joda.time.DateTime(testDate.getTime)
        neJodaDT.toString must notBeNull // Test that JSON Serialization works
        neJodaDT must notBeNull
        neJodaDT must haveSuperClass[DBObject]
        neJodaDT must beEqualTo(nonDSL("foo", "$gte", new org.joda.time.DateTime(testDate.getTime)))
        DeregisterJodaTimeConversionHelpers()
      }

    }
  }

  "Casbah's DSL $in operator" should {
    val baseline = """{ "foo" : { "$in" : [ 1 , 8 , 12 , "x"]}}"""

    "Accept Arrays as values" in {
      val in = "foo" $in Array(1, 8, 12, "x")
      in must notBeNull
      in.toString must notBeNull // Test That JSON Serialization works
      in must haveSuperClass[DBObject]
      in.toString must beEqualTo(baseline)
    }

    "Accept Tuples of varying arity for values" in {
      "A Tuple4 works" in {
        val in = "foo" $in (1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }
      "A Tuple9 works" in {
        val in = "foo" $in (1, 8, 12, "x", "a", "b", "x", 25.2332, BigDecimal("15.32542352"))
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        /*in.toString must beEqualTo(baseline)*/
      }
    }
    "Accept Iterables as values" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val in = "foo" $in List(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val in = "foo" $in Seq(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val in = "foo" $in Set(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val in = "foo" $in HashSet(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo("""{ "foo" : { "$in" : [ 12 , "x" , 1 , 8]}}""")
      }
    }
  }

  "Casbah's DSL $nin operator" should {
    val baseline = """{ "foo" : { "$nin" : [ 1 , 8 , 12 , "x"]}}"""

    "Accept Arrays as values" in {
      val in = "foo" $nin Array(1, 8, 12, "x")
      in must notBeNull
      in.toString must notBeNull // Test That JSON Serialization works
      in must haveSuperClass[DBObject]
      in.toString must beEqualTo(baseline)
    }

    "Accept Tuples of varying arity for values" in {
      "A Tuple4 works" in {
        val in = "foo" $nin (1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }
      "A Tuple9 works" in {
        val in = "foo" $nin (1, 8, 12, "x", "a", "b", "x", 25.2332, BigDecimal("15.32542352"))
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        /*in.toString must beEqualTo(baseline)*/
      }
    }
    "Accept Iterables as values" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val in = "foo" $nin List(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val in = "foo" $nin Seq(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val in = "foo" $nin Set(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val in = "foo" $nin HashSet(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo("""{ "foo" : { "$nin" : [ 12 , "x" , 1 , 8]}}""")
      }
    }
  }

  "Casbah's DSL $all operator" should {
    val baseline = """{ "foo" : { "$all" : [ 1 , 8 , 12 , "x"]}}"""

    "Accept Arrays as values" in {
      val in = "foo" $all Array(1, 8, 12, "x")
      in must notBeNull
      in.toString must notBeNull // Test That JSON Serialization works
      in must haveSuperClass[DBObject]
      in.toString must beEqualTo(baseline)
    }

    "Accept Tuples of varying arity for values" in {
      "A Tuple4 works" in {
        val in = "foo" $all (1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }
      "A Tuple9 works" in {
        val in = "foo" $all (1, 8, 12, "x", "a", "b", "x", 25.2332, BigDecimal("15.32542352"))
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        /*in.toString must beEqualTo(baseline)*/
      }
    }
    "Accept Iterables as values" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val in = "foo" $all List(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val in = "foo" $all Seq(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val in = "foo" $all Set(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo(baseline)
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val in = "foo" $all HashSet(1, 8, 12, "x")
        in must notBeNull
        in.toString must notBeNull // Test That JSON Serialization works
        in must haveSuperClass[DBObject]
        in.toString must beEqualTo("""{ "foo" : { "$all" : [ 12 , "x" , 1 , 8]}}""")
      }
    }
  }

  "Casbah's $mod modulo operator" should {
    "Function as expected" in {
      val mod = "x" $mod (5, 2)
      mod must notBeNull
      mod.toString must notBeNull
      mod must haveSuperClass[DBObject]
      mod.toString must beEqualTo("""{ "x" : { "$mod" : [ 5 , 2]}}""")
    }

    "Take non-integer valid numeric values" in {
      val mod = "x" $mod (5.12, 2.9)
      mod must notBeNull
      mod.toString must notBeNull
      mod must haveSuperClass[DBObject]
      mod.toString must beEqualTo("""{ "x" : { "$mod" : [ 5.12 , 2.9]}}""")
    }

    "Accept differing numeric types for left v. right" in {
      val mod = "x" $mod (5, 2.8300000000000125)
      mod must notBeNull
      mod.toString must notBeNull
      mod must haveSuperClass[DBObject]
      mod.toString must beEqualTo("""{ "x" : { "$mod" : [ 5 , 2.8300000000000125]}}""")
    }
  
  }
  
  "Casbah's $size operator" should {
    "Function as expected" in {
      val size = "x" $size 12
      size must notBeNull
      size.toString must notBeNull
      size must haveSuperClass[DBObject]
      size.toString must beEqualTo("""{ "x" : { "$size" : 12}}""")
    }
    "Accept a BigInt" in {
      val size = "x" $size BigInt("555565363612")
      size must notBeNull
      size.toString must notBeNull
      size must haveSuperClass[DBObject]
      size.toString must beEqualTo("""{ "x" : { "$size" : 555565363612}}""")
    }
  }

  "Casbah's $exists operator" should {
    "Function as expected" in {
      val exists = "x" $exists true
      exists must notBeNull
      exists.toString must notBeNull
      exists must haveSuperClass[DBObject]
      exists.toString must beEqualTo("""{ "x" : { "$exists" : true}}""")
    }
  }

  "Casbah's $where operator" should {
    "Function as expected" in {
      val where = "x" $where "function () { this.foo }"
      where must notBeNull
      where.toString must notBeNull
      where must haveSuperClass[DBObject]
    }
  }

  "Casbah's $not operator" should {
    "Function in a normal passing" in {
      val not = "foo" $not { _ $lt 5.1 }
      not must notBeNull
      not.toString must notBeNull
      not must haveSuperClass[DBObject]
    }


    "Function with anchoring and subobjects" in {
      val not = "foo" $not { _ $mod (5, 10) }
      not must notBeNull
      not.toString must notBeNull
      not must haveSuperClass[DBObject]
      not must beEqualTo(nonDSL("foo", "$not", MongoDBObject("$mod" -> MongoDBList(5, 10))))
    }

    "Function with a regular expression" in {
      val not = "foo" $not new scala.util.matching.Regex("^foo.*bar")
      not must notBeNull
      not.toString must notBeNull
      not must haveSuperClass[DBObject]
    }
  }

  "Casbah's $slice operator" should {
    "Function with a single value" in {
      val slice = "foo" $slice 5
      slice must notBeNull
      slice.toString must notBeNull
      slice must haveSuperClass[DBObject]
      slice must beEqualTo(nonDSL("foo", "$slice", 5))
    }
    "Function with a slice and limit " in {
      val slice = "foo" $slice (5, -1)
      slice must notBeNull
      slice.toString must notBeNull
      slice must haveSuperClass[DBObject]
      slice must beEqualTo(nonDSL("foo", "$slice", MongoDBList(5, -1)))
    }
  }


  "Casbah's $elemMatch operator" should {
    "Function as expected" in {
      val elemMatch = "foo" $elemMatch MongoDBObject("x" -> 5, "y" -> MongoDBList("x", "y", "z", "abc", 123, 12.8))
      elemMatch must notBeNull
      elemMatch.toString must notBeNull
      elemMatch must haveSuperClass[DBObject]
      //elemMatch must beEqualTo(nonDSL("foo", "$elemMatch", MongoDBObject("x" -> 5, "y" -> MongoDBList("x", "y", "z", "abc", 123, 12.8))))
    }
  }

  "Casbah's $type operator" should {
    "Accept raw Byte indicators (e.g. from org.bson.BSON)" in {
      // Don't need to test every value here since it's just a byte
      val typeOper = "foo" $type org.bson.BSON.NUMBER_LONG
      typeOper must notBeNull
      typeOper.toString must notBeNull
      typeOper must haveSuperClass[DBObject]
      typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NUMBER_LONG))
    }

    "Accept manifested Type arguments" in {
      "Doubles" in {
        val typeOper = "foo".$type[Double]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NUMBER))
      }
      "Strings" in {
        val typeOper = "foo".$type[String]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.STRING))
      }
      "Object" in {
        "via BSONObject" in {
          val typeOper = "foo".$type[org.bson.BSONObject]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.OBJECT))
        }
        "via DBObject" in {
          val typeOper = "foo".$type[DBObject]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.OBJECT))
        }
      }
      "Array" in {
        "via BasicDBList" in {
          val typeOper = "foo".$type[BasicDBList]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.ARRAY))
        }
        "via BasicBSONList" in {
          val typeOper = "foo".$type[org.bson.types.BasicBSONList]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.ARRAY))
        }
      }
      "OID" in {
        val typeOper = "foo".$type[ObjectId]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.OID))
      }
      "Boolean" in {
        val typeOper = "foo".$type[Boolean]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.BOOLEAN))
      }
      "Date" in {
        "via JDKDate" in {
          val typeOper = "foo".$type[java.util.Date]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.DATE))
        }
        "via Joda DateTime" in {
          val typeOper = "foo".$type[org.joda.time.DateTime]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.DATE))
        }
      }
      "None (null)" in {
        // For some reason you can't use NONE 
        val typeOper = "foo".$type[Option[Nothing]]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NULL))
      }
      "Regex" in {
        "Scala Regex" in {
          val typeOper = "foo".$type[scala.util.matching.Regex]
          typeOper must notBeNull
          typeOper.toString must notBeNull
          typeOper must haveSuperClass[DBObject]
          typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.REGEX))
        }
      }
      "Symbol" in {
        val typeOper = "foo".$type[Symbol]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.SYMBOL))
      }
      "Number (integer)" in {
        val typeOper = "foo".$type[Int]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NUMBER_INT))
      }
      "Number (Long)" in {
        val typeOper = "foo".$type[Long]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.NUMBER_LONG))
      }
      "Timestamp" in {
        val typeOper = "foo".$type[java.sql.Timestamp]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.TIMESTAMP))
      }
      "Binary" in {
        val typeOper = "foo".$type[Array[Byte]]
        typeOper must notBeNull
        typeOper.toString must notBeNull
        typeOper must haveSuperClass[DBObject]
        typeOper must beEqualTo(nonDSL("foo", "$type", org.bson.BSON.BINARY))
      }

    }

  }


  "Casbah's GeoSpatial Operators" should {
    "Allow construction of GeoCoords" in {
      "With two different numeric types" in {
        val geo = GeoCoords(5.23, -123)
        geo must notBeNull
      }
      "Be convertible from a tuple" in {
        val geo = tupleToGeoCoords((5.23, -123))
        geo must notBeNull
        geo must haveClass[GeoCoords[_,_]]
      }
    }

    "Support $near" in {
      "With an explicit GeoCoords instance" in {
        val near = "foo" $near GeoCoords(74.2332, -75.23452)
        near must notBeNull
        near must haveSuperClass[DBObject]
        /*near must beEqualTo(nonDSL("foo", "$near", MongoDBList(74.2332, -75.23542)))*/
      }
      "With a tuple converted coordinate set" in {
        val near = "foo" $near (74.2332, -75.23452)
        near must notBeNull
        near must haveSuperClass[DBObject]
        /*near must beEqualTo(nonDSL("foo", "$near", MongoDBList(74.2332, -75.23542)))*/
      }
      "With a $maxDistance specification" in {
        val near = "foo" $near (74.2332, -75.23452) $maxDistance 5
        near must notBeNull
        near must haveSuperClass[DBObject]
        /*near.asInstanceOf[DBObject] must beEqualTo(MongoDBObject("foo" -> MongoDBObject("$near" -> (74.2332, -75.23452), "$maxDistance" -> 5)).asInstanceOf[DBObject])*/
      }
    }

    "Support $nearSphere" in {
      "With an explicit GeoCoords instance" in {
        val near = "foo" $nearSphere GeoCoords(74.2332, -75.23452)
        near must notBeNull
        near must haveSuperClass[DBObject]
        /*near must beEqualTo(nonDSL("foo", "$nearSphere", MongoDBList(74.2332, -75.23542)))*/
      }
      "With a tuple converted coordinate set" in {
        val near = "foo" $nearSphere (74.2332, -75.23452)
        near must notBeNull
        near must haveSuperClass[DBObject]
        /*near must beEqualTo(nonDSL("foo", "$nearSphere", MongoDBList(74.2332, -75.23542)))*/
      }
    }
    "Support.$within ..." in {
      "... $box" in {
        "With an explicit GeoCoords instance" in {
          val near = "foo".$within $box (GeoCoords(74.2332, -75.23452), GeoCoords(123, 456))
          near must notBeNull
          near must haveSuperClass[DBObject]
          /*near must beEqualTo(nonDSL("foo", "$box", MongoDBList(74.2332, -75.23542)))*/
        }
        "With a tuple converted coordinate set" in {
          val near = "foo".$within $box ((74.2332, -75.23452), (123, 456))
          near must notBeNull
          near must haveSuperClass[DBObject]
          /*near must beEqualTo(nonDSL("foo", "$box", MongoDBList(74.2332, -75.23542)))*/
        }
      }
      "... $center" in {
        "With an explicit GeoCoords instance" in {
          val near = "foo".$within $center (GeoCoords(74.2332, -75.23452), 5)
          near must notBeNull
          near must haveSuperClass[DBObject]
          /*near must beEqualTo(nonDSL("foo", "$center", MongoDBList(74.2332, -75.23542)))*/
        }
        "With a tuple converted coordinate set" in {
          val near = "foo".$within $center ((74.2332, -75.23452), 5)
          near must notBeNull
          near must haveSuperClass[DBObject]
          /*near must beEqualTo(nonDSL("foo", "$center", MongoDBList(74.2332, -75.23542)))*/
        }
      }
      "... $centerSphere" in {
        "With an explicit GeoCoords instance" in {
          val near = "foo".$within $centerSphere (GeoCoords(74.2332, -75.23452), 5)
          near must notBeNull
          near must haveSuperClass[DBObject]
          /*near must beEqualTo(nonDSL("foo", "$centerSphere", MongoDBList(74.2332, -75.23542)))*/
        }
        "With a tuple converted coordinate set" in {
          val near = "foo".$within $centerSphere ((74.2332, -75.23452), 5)
          near must notBeNull
          near must haveSuperClass[DBObject]
          /*near must beEqualTo(nonDSL("foo", "$centerSphere", MongoDBList(74.2332, -75.23542)))*/
        }
      }
    }
  }


  "Chained core operators" should {
    "Function correctly" in {
      val ltGt = "foo" $gte 15 $lt 35.2 $ne 16
      log.debug("LTGT: %s", ltGt)
      ltGt must notBeNull
      ltGt must haveSuperClass[DBObject]
      ltGt must beEqualTo(MongoDBObject("foo" ->  MongoDBObject("$gte" -> 15, "$lt" -> 35.2, "$ne" -> 16)))
    } 
    "Function correctly with deeper nesting e.g. $not" in {
      val ltGt = "foo" $not { _ $gte 15 $lt 35.2 $ne 16 }
      log.debug("LTGT: %s", ltGt)
      ltGt must notBeNull
      ltGt must haveSuperClass[DBObject]
      ltGt must beEqualTo(MongoDBObject("foo" -> MongoDBObject("$not" -> MongoDBObject("$gte" -> 15, "$lt" -> 35.2, "$ne" -> 16))))
    } 
  }
}
// vim: set ts=2 sw=2 sts=2 et:
