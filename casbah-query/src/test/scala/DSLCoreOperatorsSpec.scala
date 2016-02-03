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

package com.mongodb.casbah.test.query

import scala.language.reflectiveCalls
import com.mongodb.casbah.commons.conversions.scala._
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification

@SuppressWarnings(Array("deprecation"))
class DSLCoreOperatorsSpec extends CasbahMutableSpecification {

  sequential

  def nonDSL(key: String, oper: String, value: Any) = MongoDBObject(key -> MongoDBObject(oper -> value))

  "Casbah's DSL $eq operator" should {
    DeregisterJodaTimeConversionHelpers()

    val testDate = new java.util.Date(109, 1, 2, 0, 0, 0)

    "Accept a right hand value of String" in {
      val eqStr = "foo" $eq "ISBN-123456789"
      eqStr must beEqualTo(MongoDBObject("foo" -> "ISBN-123456789"))
    }

    "Accept a right hand value of BSONTimestamp" in {
      val timestamp = new BSONTimestamp(10101, 0)
      val timeQuery = "foo" $eq timestamp
      timeQuery must beEqualTo(MongoDBObject("foo" -> timestamp))
    }

    "Accept a right hand value of None" in {
      val neQuery = "foo" $eq None
      neQuery must haveEntry("foo" -> None)
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val eqObj = "foo" $eq new BasicDBObject("bar", "baz")
        eqObj must beEqualTo(MongoDBObject("foo" -> new BasicDBObject("bar", "baz")))
      }
      "A MongoDBObject created value" in {
        val eqObj = "foo" $eq MongoDBObject("bar" -> "baz")
        eqObj must beEqualTo(MongoDBObject("foo" -> MongoDBObject("bar" -> "baz")))
      }
      "A DBList should work also" in {
        val eqLst = "foo" $eq MongoDBList("x", "y", "z")
        eqLst must beEqualTo(MongoDBObject("foo" -> MongoDBList("x", "y", "z")))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val eqLst = "foo" $eq List("x", "y", 5)
        eqLst must beEqualTo(MongoDBObject("foo" -> List("x", "y", 5)))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val eqSeq = "foo" $eq Seq("x", "y", 5)
        eqSeq must beEqualTo(MongoDBObject("foo" -> Seq("x", "y", 5)))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val eqSet = "foo" $eq Set("x", "y", 5)
        eqSet must haveListEntry("foo", Set("x", "y", 5))
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val eqHashSet = "foo" $eq HashSet("x", "y", 5)
        eqHashSet must haveListEntry("foo", HashSet("x", "y", 5))
      }

      "Also, Arrays function" in {
        val eqArray = "foo" $eq Array("x", "y", 5)
        eqArray must haveListEntry("foo", Array("x", "y", 5))
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val eqInt = "foo" $eq 10
        eqInt must beEqualTo(MongoDBObject("foo" -> 10))
      }
      "with Byte" in {
        val eqByte = "foo" $eq java.lang.Byte.parseByte("51")
        eqByte must beEqualTo(MongoDBObject("foo" -> java.lang.Byte.parseByte("51")))
      }
      "with Double" in {
        val eqDouble = "foo" $eq 5.232352
        eqDouble must beEqualTo(MongoDBObject("foo" -> 5.232352))
      }
      "with Float" in {
        val eqFloat = "foo" $eq java.lang.Float.parseFloat("5.232352")
        eqFloat must beEqualTo(MongoDBObject("foo" -> java.lang.Float.parseFloat("5.232352")))
      }
      "with Long" in {
        val eqLong = "foo" $eq 10L
        eqLong must beEqualTo(MongoDBObject("foo" -> 10L))
      }
      "with Short" in {
        val eqShort = "foo" $eq java.lang.Short.parseShort("10")
        eqShort must beEqualTo(MongoDBObject("foo" -> java.lang.Short.parseShort("10")))
      }
      "with JDKDate" in {
        val eqJDKDate = "foo" $eq testDate
        eqJDKDate must beEqualTo(MongoDBObject("foo" -> testDate))
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val eqJodaDT = "foo" $eq new org.joda.time.DateTime(testDate.getTime)
        eqJodaDT must beEqualTo(MongoDBObject("foo" -> new org.joda.time.DateTime(testDate.getTime)))
      }
    }
  }

  "Casbah's DSL $ne operator" should {
    DeregisterJodaTimeConversionHelpers()

    val testDate = new java.util.Date(109, 1, 2, 0, 0, 0)

    "Accept a right hand value of String" in {
      val neStr = "foo" $ne "ISBN-123456789"
      neStr must haveEntry("foo.$ne" -> "ISBN-123456789")
    }

    "Accept a right hand value of BSONTimestamp" in {
      val timestamp = new BSONTimestamp(10101, 0)
      val timeQuery = "foo" $ne timestamp
      timeQuery must haveEntry("foo.$ne" -> timestamp)
    }

    "Accept a right hand value of None" in {
      val neQuery = "foo" $ne None
      neQuery must haveEntry("foo.$ne" -> None)
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $ne new BasicDBObject("bar", "baz")
        neObj must haveEntry("foo.$ne" -> new BasicDBObject("bar", "baz"))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $ne MongoDBObject("bar" -> "baz")
        neObj must haveEntry("foo.$ne" -> MongoDBObject("bar" -> "baz"))
      }
      "A DBList should work also" in {
        val neLst = "foo" $ne MongoDBList("x", "y", "z")
        neLst must haveEntry("foo.$ne" -> MongoDBList("x", "y", "z"))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $ne List("x", "y", 5)
        neLst must haveEntry("foo.$ne" -> List("x", "y", 5))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $ne Seq("x", "y", 5)
        neSeq must haveEntry("foo.$ne" -> Seq("x", "y", 5))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $ne Set("x", "y", 5)
        neSet must haveListEntry("foo.$ne", Set("x", "y", 5))
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $ne HashSet("x", "y", 5)
        neHashSet must haveListEntry("foo.$ne", HashSet("x", "y", 5))
      }

      "Also, Arrays function" in {
        val neArray = "foo" $ne Array("x", "y", 5)
        neArray must haveListEntry("foo.$ne", Array("x", "y", 5))
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $ne 10
        neInt must haveEntry("foo.$ne" -> 10)
      }
      "with Byte" in {
        val neByte = "foo" $ne java.lang.Byte.parseByte("51")
        neByte must haveEntry("foo.$ne" -> java.lang.Byte.parseByte("51"))
      }
      "with Double" in {
        val neDouble = "foo" $ne 5.232352
        neDouble must haveEntry("foo.$ne" -> 5.232352)
      }
      "with Float" in {
        val neFloat = "foo" $ne java.lang.Float.parseFloat("5.232352")
        neFloat must haveEntry("foo.$ne" -> java.lang.Float.parseFloat("5.232352"))
      }
      "with Long" in {
        val neLong = "foo" $ne 10L
        neLong must haveEntry("foo.$ne" -> 10L)
      }
      "with Short" in {
        val neShort = "foo" $ne java.lang.Short.parseShort("10")
        neShort must haveEntry("foo.$ne" -> java.lang.Short.parseShort("10"))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $ne testDate
        neJDKDate must haveEntry("foo.$ne", testDate)
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $ne new org.joda.time.DateTime(testDate.getTime)
        neJodaDT must haveEntry("foo.$ne" -> new org.joda.time.DateTime(testDate.getTime))
      }
    }
  }

  "Casbah's DSL $lt operator" should {
    DeregisterJodaTimeConversionHelpers()

    val testDate = new java.util.Date(109, 1, 2, 0, 0, 0)

    "Accept a right hand value of String" in {
      val neStr = "foo" $lt "ISBN-123456789"
      neStr must haveEntry("foo.$lt" -> "ISBN-123456789")
    }

    "Accept a right hand value of BSONTimestamp" in {
      val timestamp = new BSONTimestamp(10101, 0)
      val timeQuery = "foo" $lt timestamp
      timeQuery must haveEntry("foo.$lt" -> timestamp)
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $lt new BasicDBObject("bar", "baz")
        neObj must haveEntry("foo.$lt" -> new BasicDBObject("bar", "baz"))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $lt MongoDBObject("bar" -> "baz")
        neObj must haveEntry("foo.$lt" -> MongoDBObject("bar" -> "baz"))
      }
      "A DBList should work also" in {
        val neLst = "foo" $lt MongoDBList("x", "y", "z")
        neLst must haveEntry("foo.$lt" -> MongoDBList("x", "y", "z"))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $lt List("x", "y", 5)
        neLst must haveEntry("foo.$lt" -> List("x", "y", 5))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $lt Seq("x", "y", 5)
        neSeq must haveEntry("foo.$lt" -> Seq("x", "y", 5))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $lt Set("x", "y", 5)
        neSet must haveListEntry("foo.$lt", List("x", "y", 5))
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $lt HashSet("x", "y", 5)
        neHashSet must haveListEntry("foo.$lt", HashSet("x", "y", 5)) // TODO - This *MUST* Be able to match regardless of inner type!!!!
      }

      "Also, Arrays function" in {
        val neArray = "foo" $lt Array("x", "y", 5)
        neArray must haveListEntry("foo.$lt", Array("x", "y", 5))
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $lt 10
        neInt must haveEntry("foo.$lt" -> 10)
      }
      "with Byte" in {
        val neByte = "foo" $lt java.lang.Byte.parseByte("51")
        neByte must haveEntry("foo.$lt" -> java.lang.Byte.parseByte("51"))
      }
      "with Double" in {
        val neDouble = "foo" $lt 5.232352
        neDouble must haveEntry("foo.$lt" -> 5.232352)
      }
      "with Float" in {
        val neFloat = "foo" $lt java.lang.Float.parseFloat("5.232352")
        neFloat must haveEntry("foo.$lt" -> java.lang.Float.parseFloat("5.232352"))
      }
      "with Long" in {
        val neLong = "foo" $lt 10L
        neLong must haveEntry("foo.$lt" -> 10L)
      }
      "with Short" in {
        val neShort = "foo" $lt java.lang.Short.parseShort("10")
        neShort must haveEntry("foo.$lt" -> java.lang.Short.parseShort("10"))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $lt testDate
        neJDKDate must haveEntry("foo.$lt" -> testDate)
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $lt new org.joda.time.DateTime(testDate.getTime)
        neJodaDT must haveEntry("foo.$lt" -> new org.joda.time.DateTime(testDate.getTime))
      }

    }
  }

  "Casbah's DSL $lte operator" should {
    DeregisterJodaTimeConversionHelpers()

    val testDate = new java.util.Date(109, 1, 2, 0, 0, 0)

    "Accept a right hand value of String" in {
      val neStr = "foo" $lte "ISBN-123456789"
      neStr must haveEntry("foo.$lte" -> "ISBN-123456789")
    }

    "Accept a right hand value of BSONTimestamp" in {
      val timestamp = new BSONTimestamp(10101, 0)
      val timeQuery = "foo" $lte timestamp
      timeQuery must haveEntry("foo.$lte" -> timestamp)
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $lte new BasicDBObject("bar", "baz")
        neObj must haveEntry("foo.$lte" -> new BasicDBObject("bar", "baz"))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $lte MongoDBObject("bar" -> "baz")
        neObj must haveEntry("foo.$lte" -> MongoDBObject("bar" -> "baz"))
      }
      "A DBList should work also" in {
        val neLst = "foo" $lte MongoDBList("x", "y", "z")
        neLst must haveEntry("foo.$lte" -> MongoDBList("x", "y", "z"))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $lte List("x", "y", 5)
        neLst must haveEntry("foo.$lte" -> List("x", "y", 5))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $lte Seq("x", "y", 5)
        neSeq must haveEntry("foo.$lte" -> Seq("x", "y", 5))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $lte Set("x", "y", 5)
        neSet must haveListEntry("foo.$lte", List("x", "y", 5))
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $lte HashSet("x", "y", 5)
        neHashSet must haveListEntry("foo.$lte", HashSet("x", "y", 5))
      }

      "Also, Arrays function" in {
        val neArray = "foo" $lte Array("x", "y", 5)
        neArray must haveListEntry("foo.$lte", Array("x", "y", 5))
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $lte 10
        neInt must haveEntry("foo.$lte" -> 10)
      }
      "with Byte" in {
        val neByte = "foo" $lte java.lang.Byte.parseByte("51")
        neByte must haveEntry("foo.$lte" -> java.lang.Byte.parseByte("51"))
      }
      "with Double" in {
        val neDouble = "foo" $lte 5.232352
        neDouble must haveEntry("foo.$lte" -> 5.232352)
      }
      "with Float" in {
        val neFloat = "foo" $lte java.lang.Float.parseFloat("5.232352")
        neFloat must haveEntry("foo.$lte" -> java.lang.Float.parseFloat("5.232352"))
      }
      "with Long" in {
        val neLong = "foo" $lte 10L
        neLong must haveEntry("foo.$lte" -> 10L)
      }
      "with Short" in {
        val neShort = "foo" $lte java.lang.Short.parseShort("10")
        neShort must haveEntry("foo.$lte" -> java.lang.Short.parseShort("10"))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $lte testDate
        neJDKDate must haveEntry("foo.$lte" -> testDate)
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $lte new org.joda.time.DateTime(testDate.getTime)
        neJodaDT must haveEntry("foo.$lte" -> new org.joda.time.DateTime(testDate.getTime))
      }

    }
  }

  "Casbah's DSL $gt operator" should {
    DeregisterJodaTimeConversionHelpers()

    val testDate = new java.util.Date(109, 1, 2, 0, 0, 0)

    "Accept a right hand value of String" in {
      val neStr = "foo" $gt "ISBN-123456789"
      neStr must haveEntry("foo.$gt" -> "ISBN-123456789")
    }

    "Accept a right hand value of BSONTimestamp" in {
      val timestamp = new BSONTimestamp(10101, 0)
      val timeQuery = "foo" $gt timestamp
      timeQuery must haveEntry("foo.$gt" -> timestamp)
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $gt new BasicDBObject("bar", "baz")
        neObj must haveEntry("foo.$gt" -> new BasicDBObject("bar", "baz"))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $gt MongoDBObject("bar" -> "baz")
        neObj must haveEntry("foo.$gt" -> MongoDBObject("bar" -> "baz"))
      }
      "A DBList should work also" in {
        val neLst = "foo" $gt MongoDBList("x", "y", "z")
        neLst must haveListEntry("foo.$gt", MongoDBList("x", "y", "z"))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $gt List("x", "y", 5)
        neLst must haveListEntry("foo.$gt", List("x", "y", 5))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $gt Seq("x", "y", 5)
        neSeq must haveListEntry("foo.$gt", Seq("x", "y", 5))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $gt Set("x", "y", 5)
        neSet must haveListEntry("foo.$gt", List("x", "y", 5))
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $gt HashSet("x", "y", 5)
        neHashSet must haveListEntry("foo.$gt", HashSet("x", "y", 5))
      }

      "Also, Arrays function" in {
        val neArray = "foo" $gt Array("x", "y", 5)
        neArray must haveListEntry("foo.$gt", Array("x", "y", 5))
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $gt 10
        neInt must haveEntry("foo.$gt" -> 10)
      }
      "with Byte" in {
        val neByte = "foo" $gt java.lang.Byte.parseByte("51")
        neByte must haveEntry("foo.$gt" -> java.lang.Byte.parseByte("51"))
      }
      "with Double" in {
        val neDouble = "foo" $gt 5.232352
        neDouble must haveEntry("foo.$gt" -> 5.232352)
      }
      "with Float" in {
        val neFloat = "foo" $gt java.lang.Float.parseFloat("5.232352")
        neFloat must haveEntry("foo.$gt" -> java.lang.Float.parseFloat("5.232352"))
      }
      "with Long" in {
        val neLong = "foo" $gt 10L
        neLong must haveEntry("foo.$gt" -> 10L)
      }
      "with Short" in {
        val neShort = "foo" $gt java.lang.Short.parseShort("10")
        neShort must haveEntry("foo.$gt" -> java.lang.Short.parseShort("10"))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $gt testDate
        neJDKDate must haveEntry("foo.$gt" -> testDate)
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $gt new org.joda.time.DateTime(testDate.getTime)
        neJodaDT must haveEntry("foo.$gt" -> new org.joda.time.DateTime(testDate.getTime))
      }

    }
  }

  "Casbah's DSL $gte operator" should {
    DeregisterJodaTimeConversionHelpers()

    val testDate = new java.util.Date(109, 1, 2, 0, 0, 0)

    "Accept a right hand value of String" in {
      val neStr = "foo" $gte "ISBN-123456789"
      neStr must haveEntry("foo.$gte" -> "ISBN-123456789")
    }

    "Accept a right hand value of BSONTimestamp" in {
      val timestamp = new BSONTimestamp(10101, 0)
      val timeQuery = "foo" $gte timestamp
      timeQuery must haveEntry("foo.$gte" -> timestamp)
    }

    "Accept a right hand value of DBObject" in {
      "A BasicDBObject created value" in {
        val neObj = "foo" $gte new BasicDBObject("bar", "baz")
        neObj must haveEntry("foo.$gte" -> MongoDBObject("bar" -> "baz"))
      }
      "A MongoDBObject created value" in {
        val neObj = "foo" $gte MongoDBObject("bar" -> "baz")
        neObj must haveEntry("foo.$gte" -> MongoDBObject("bar" -> "baz"))
      }
      "A DBList should work also" in {
        val neLst = "foo" $gte MongoDBList("x", "y", "z")
        neLst must haveEntry("foo.$gte", MongoDBList("x", "y", "z"))

      }
    }

    "Accept List-like values descended from Iterable" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val neLst = "foo" $gte List("x", "y", 5)
        neLst must haveEntry("foo.$gte", List("x", "y", 5))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val neSeq = "foo" $gte Seq("x", "y", 5)
        neSeq must haveEntry("foo.$gte", Seq("x", "y", 5))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val neSet = "foo" $gte Set("x", "y", 5)
        neSet must haveListEntry("foo.$gte", List("x", "y", 5))
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val neHashSet = "foo" $gte HashSet("x", "y", 5)
        neHashSet must haveListEntry("foo.$gte", HashSet("x", "y", 5))
      }

      "Also, Arrays function" in {
        val neArray = "foo" $gte Array("x", "y", 5)
        neArray must haveListEntry("foo.$gte", Array("x", "y", 5))
      }
    }

    "Accept a right hand value of ValidDateOrNumericType" in {
      "with Int" in {
        val neInt = "foo" $gte 10
        neInt must haveEntry("foo.$gte" -> 10)
      }
      "with Byte" in {
        val neByte = "foo" $gte java.lang.Byte.parseByte("51")
        neByte must haveEntry("foo.$gte" -> java.lang.Byte.parseByte("51"))
      }
      "with Double" in {
        val neDouble = "foo" $gte 5.232352
        neDouble must haveEntry("foo.$gte" -> 5.232352)
      }
      "with Float" in {
        val neFloat = "foo" $gte java.lang.Float.parseFloat("5.232352")
        neFloat must haveEntry("foo.$gte" -> java.lang.Float.parseFloat("5.232352"))
      }
      "with Long" in {
        val neLong = "foo" $gte 10L
        neLong must haveEntry("foo.$gte" -> 10L)
      }
      "with Short" in {
        val neShort = "foo" $gte java.lang.Short.parseShort("10")
        neShort must haveEntry("foo.$gte" -> java.lang.Short.parseShort("10"))
      }
      "with JDKDate" in {
        val neJDKDate = "foo" $gte testDate
        neJDKDate must haveEntry("foo.$gte" -> testDate)
      }
      "with JodaDT" in {
        RegisterJodaTimeConversionHelpers()
        val neJodaDT = "foo" $gte new org.joda.time.DateTime(testDate.getTime)
        neJodaDT must haveEntry("foo.$gte" -> new org.joda.time.DateTime(testDate.getTime))
      }

    }
  }

  "Casbah's DSL $in operator" should {
    DeregisterJodaTimeConversionHelpers()

    "Accept Arrays as values" in {
      val in = "foo" $in Array(1, 8, 12, "x")
      in must haveListEntry("foo.$in", Array(1, 8, 12, "x"))
    }

    "Accept Tuples of varying arity for values" in {
      "A Tuple4 works" in {
        val in = "foo" $in (1, 8, 12, "x")
        in must haveListEntry("foo.$in", Array(1, 8, 12, "x"))
      }
      "A Tuple9 works" in {
        val in = "foo" $in (1, 8, 12, "x", "a", "b", "x", 25.2332, "15.32542352")
        in must haveListEntry("foo.$in", Array(1, 8, 12, "x", "a", "b", "x", 25.2332, "15.32542352"))
      }
    }
    "Accept Iterables as values" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val in = "foo" $in List(1, 8, 12, "x")
        in must haveListEntry("foo.$in", Array(1, 8, 12, "x"))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val in = "foo" $in Seq(1, 8, 12, "x")
        in must haveListEntry("foo.$in", Array(1, 8, 12, "x"))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val in = "foo" $in Set(1, 8, 12, "x")
        in must haveListEntry("foo.$in", Array(1, 8, 12, "x"))
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val in = "foo" $in HashSet(1, 8, 12, "x")
        in must haveListEntry("foo.$in", HashSet(1, 8, 12, "x"))
      }
    }
  }

  "Casbah's DSL $nin operator" should {

    "Accept Arrays as values" in {
      val in = "foo" $nin Array(1, 8, 12, "x")
      in must haveListEntry("foo.$nin", Array(1, 8, 12, "x"))
    }

    "Accept Tuples of varying arity for values" in {
      "A Tuple4 works" in {
        val in = "foo" $nin (1, 8, 12, "x")
        in must haveListEntry("foo.$nin", List(1, 8, 12, "x"))
      }
      "A Tuple9 works" in {
        val nin = "foo" $nin (1, 8, 12, "x", "a", "b", "x", 25.2332, "15.32542352")
        nin must haveListEntry("foo.$nin", List(1, 8, 12, "x", "a", "b", "x", 25.2332, "15.32542352"))
      }
    }
    "Accept Iterables as values" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val in = "foo" $nin List(1, 8, 12, "x")
        in must haveListEntry("foo.$nin", List(1, 8, 12, "x"))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val in = "foo" $nin Seq(1, 8, 12, "x")
        in must haveListEntry("foo.$nin", List(1, 8, 12, "x"))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val in = "foo" $nin Set(1, 8, 12, "x")
        in must haveListEntry("foo.$nin", List(1, 8, 12, "x"))
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val in = "foo" $nin HashSet(1, 8, 12, "x")
        in must haveListEntry("foo.$nin", HashSet(1, 8, 12, "x"))
      }
    }
  }

  "Casbah's DSL $all operator" should {

    "Accept Arrays as values" in {
      val in = "foo" $all Array(1, 8, 12, "x")
      in must haveListEntry("foo.$all", Array(1, 8, 12, "x"))
    }

    "Accept Tuples of varying arity for values" in {
      "A Tuple4 works" in {
        val in = "foo" $all (1, 8, 12, "x")
        in must haveListEntry("foo.$all", Array(1, 8, 12, "x"))
      }
      "A Tuple9 works" in {
        val all = "foo" $all (1, 8, 12, "x", "a", "b", "x", 25.2332, "15.32542352")
        all must haveListEntry("foo.$all", Array(1, 8, 12, "x", "a", "b", "x", 25.2332, "15.32542352"))
      }
    }
    "Accept Iterables as values" in {
      "An immutable List works" in {
        import scala.collection.immutable.List
        val in = "foo" $all List(1, 8, 12, "x")
        in must haveListEntry("foo.$all", Array(1, 8, 12, "x"))
      }

      "An immutable Seq works" in {
        import scala.collection.immutable.Seq
        val in = "foo" $all Seq(1, 8, 12, "x")
        in must haveListEntry("foo.$all", Array(1, 8, 12, "x"))
      }

      "An immutable Set works" in {
        import scala.collection.immutable.Set
        val in = "foo" $all Set(1, 8, 12, "x")
        in must haveListEntry("foo.$all", Array(1, 8, 12, "x"))
      }

      "An mutable HashSet works" in {
        import scala.collection.mutable.HashSet
        val in = "foo" $all HashSet(1, 8, 12, "x")
        in must haveListEntry("foo.$all", HashSet(1, 8, 12, "x"))
      }
    }
  }

  "Casbah's $mod modulo operator" should {
    "Function as expected" in {
      val mod = "x" $mod (5, 2)
      mod.toString must beEqualTo("""{ "x" : { "$mod" : [ 5 , 2]}}""")
    }

    "Take non-integer valid numeric values" in {
      val mod = "x" $mod (5.12, 2.9)
      mod.toString must beEqualTo("""{ "x" : { "$mod" : [ 5.12 , 2.9]}}""")
    }

    "Accept differing numeric types for left v. right" in {
      val mod = "x" $mod (5, 2.8300000000000125)
      mod.toString must beEqualTo("""{ "x" : { "$mod" : [ 5 , 2.8300000000000125]}}""")
    }

  }

  "Casbah's $size operator" should {
    "Function as expected" in {
      val size = "x" $size 12
      size.toString must beEqualTo("""{ "x" : { "$size" : 12}}""")
    }
  }

  "Casbah's $exists operator" should {
    "Function as expected" in {
      val exists = "x" $exists true
      exists.toString must beEqualTo("""{ "x" : { "$exists" : true}}""")
    }
  }

  "Casbah's $not operator" should {
    "Function in a normal passing" in {
      val not = "foo" $not {
        _ $lt 5.1
      }
      not must haveEntry("foo.$not.$lt" -> 5.1)
    }

    "Function with anchoring and subobjects" in {
      val not = "foo" $not {
        _ $mod (5, 10)
      }
      not must haveEntry("foo.$not" -> MongoDBObject("$mod" -> MongoDBList(5, 10)))
    }

    "Function with a regular expression" in {
      val not = "foo" $not "^foo.*bar".r
      not.toString must beEqualTo("""{ "foo" : { "$not" : { "$regex" : "^foo.*bar"}}}""")
    }
  }

  "Casbah's $regex operator" should {
    "Function in a normal passing" in {
      val regex = "foo" $regex "^bar$"
      regex must haveEntry("foo.$regex" -> "^bar$")
    }
    "Work when passing in a scala regex" in {
      val regex = "foo" $eq "^bar$".r
      regex.get("foo").toString() must beEqualTo("^bar$")
    }
  }

  "Casbah's $slice operator" should {
    "Function with a single value" in {
      val slice = "foo" $slice 5
      slice must haveEntry("foo.$slice" -> 5)
    }
    "Function with a slice and limit " in {
      val slice = "foo" $slice (5, -1)
      slice must haveEntry("foo.$slice" -> MongoDBList(5, -1))
    }
  }

  "Casbah's $elemMatch operator" should {
    "Function as expected" in {
      val elemMatch = "foo" $elemMatch (MongoDBObject("a" -> 1) ++ ("b" $gt 1))
      elemMatch must beEqualTo(
        MongoDBObject(
          "foo" -> MongoDBObject(
            "$elemMatch" -> MongoDBObject(
              "a" -> 1,
              "b" -> MongoDBObject("$gt" -> 1)
            )
          )
        )
      )
    }
  }

  "Casbah's $type operator" should {
    "Accept raw Byte indicators (e.g. from org.bson.BSON)" in {
      // Don't need to test every value here since it's just a byte
      val typeOper = "foo" $type org.bson.BSON.NUMBER_LONG
      typeOper must haveEntry("foo.$type" -> org.bson.BSON.NUMBER_LONG)
    }

    "Accept manifested Type arguments" in {
      "Doubles" in {
        val typeOper = "foo".$type[Double]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.NUMBER)
      }
      "Strings" in {
        val typeOper = "foo".$type[String]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.STRING)
      }
      "Object" in {
        "via BSONObject" in {
          val typeOper = "foo".$type[org.bson.BSONObject]
          typeOper must haveEntry("foo.$type" -> org.bson.BSON.OBJECT)
        }
        "via DBObject" in {
          val typeOper = "foo".$type[DBObject]
          typeOper must haveEntry("foo.$type" -> org.bson.BSON.OBJECT)
        }
      }
      "Array" in {
        "via BasicDBList" in {
          val typeOper = "foo".$type[BasicDBList]
          typeOper must haveEntry("foo.$type" -> org.bson.BSON.ARRAY)
        }
        "via BasicBSONList" in {
          val typeOper = "foo".$type[org.bson.types.BasicBSONList]
          typeOper must haveEntry("foo.$type" -> org.bson.BSON.ARRAY)
        }
      }
      "OID" in {
        val typeOper = "foo".$type[ObjectId]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.OID)
      }
      "Boolean" in {
        val typeOper = "foo".$type[Boolean]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.BOOLEAN)
      }
      "Date" in {
        "via JDKDate" in {
          val typeOper = "foo".$type[java.util.Date]
          typeOper must haveEntry("foo.$type" -> org.bson.BSON.DATE)
        }
        "via Joda DateTime" in {
          val typeOper = "foo".$type[org.joda.time.DateTime]
          typeOper must haveEntry("foo.$type" -> org.bson.BSON.DATE)
        }
      }
      "None (null)" in {
        // For some reason you can't use NONE
        val typeOper = "foo".$type[Option[Nothing]]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.NULL)
      }
      "Regex" in {
        "Scala Regex" in {
          val typeOper = "foo".$type[scala.util.matching.Regex]
          typeOper must haveEntry("foo.$type" -> org.bson.BSON.REGEX)
        }
      }
      "Symbol" in {
        val typeOper = "foo".$type[Symbol]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.SYMBOL)
      }
      "Number (integer)" in {
        val typeOper = "foo".$type[Int]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.NUMBER_INT)
      }
      "Number (Long)" in {
        val typeOper = "foo".$type[Long]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.NUMBER_LONG)
      }
      "Timestamp" in {
        val typeOper = "foo".$type[java.sql.Timestamp]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.TIMESTAMP)
      }
      "Binary" in {
        val typeOper = "foo".$type[Array[Byte]]
        typeOper must haveEntry("foo.$type" -> org.bson.BSON.BINARY)
      }

    }

  }

  "Casbah's $meta operator" should {
    "Function as expected" in {
      val meta = ("foo" $meta)
      meta must beEqualTo(MongoDBObject("foo" -> MongoDBObject("$meta" -> "textScore")))
    }
  }

  "Casbah's GeoSpatial Operators" should {
    "Allow construction of GeoCoords" in {
      "With two different numeric types" in {
        val geo = GeoCoords(5.23, -123)
        geo must not beNull
      }
      "Be convertible from a tuple" in {
        val geo = tupleToGeoCoords((5.23, -123))
        geo must not beNull
      }
    }

    "Support $near" in {
      "With an explicit GeoCoords instance" in {
        val near = "foo" $near GeoCoords(74.2332, -75.23452)
        near must beEqualTo(
          MongoDBObject(
            "foo" -> MongoDBObject(
              "$near" -> MongoDBList(74.2332, -75.23452)
            )
          )
        )
      }
      "With a tuple converted coordinate set" in {
        val near = "foo" $near (74.2332, -75.23452)
        near must beEqualTo(
          MongoDBObject(
            "foo" -> MongoDBObject(
              "$near" -> MongoDBList(74.2332, -75.23452)
            )
          )
        )
      }
      "With a $maxDistance specification" in {
        val near = "foo" $near (74.2332, -75.23452) $maxDistance 5
        near must beEqualTo(
          MongoDBObject(
            "foo" -> MongoDBObject(
              "$near" -> MongoDBList(74.2332, -75.23452),
              "$maxDistance" -> 5
            )
          )
        )
      }
    }

    "Support $nearSphere" in {
      "With an explicit GeoCoords instance" in {
        val near = "foo" $nearSphere GeoCoords(74.2332, -75.23452)
        near must beEqualTo(
          MongoDBObject(
            "foo" -> MongoDBObject(
              "$nearSphere" -> MongoDBList(74.2332, -75.23452)
            )
          )
        )
      }
      "With a tuple converted coordinate set" in {
        val near = "foo" $nearSphere (74.2332, -75.23452)
        near must beEqualTo(
          MongoDBObject(
            "foo" -> MongoDBObject(
              "$nearSphere" -> MongoDBList(74.2332, -75.23452)
            )
          )
        )
      }
    }
    "Support.$geoWithin ..." in {

      "... geometries" in {
        var geo = MongoDBObject("$geometry" ->
          MongoDBObject(
            "$type" -> "polygon",
            "coordinates" -> (((
              GeoCoords(74.2332, -75.23452),
              GeoCoords(123, 456),
              GeoCoords(74.2332, -75.23452)
            )))
          ))
        val near = "foo".$geoWithin(geo)
        near must beEqualTo(
          MongoDBObject(
            "foo" -> MongoDBObject(
              "$geoWithin" -> geo
            )
          )
        )
      }
      "... $box" in {
        "With an explicit GeoCoords instance" in {
          val near = "foo".$geoWithin $box (GeoCoords(74.2332, -75.23452), GeoCoords(123, 456))
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$geoWithin" -> MongoDBObject(
                  "$box" -> MongoDBList(
                    MongoDBList(74.2332, -75.23452),
                    MongoDBList(123, 456)
                  )
                )
              )
            )
          )
        }
        "With a tuple converted coordinate set" in {
          val near = "foo".$geoWithin $box ((74.2332, -75.23452), (123, 456))
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$geoWithin" -> MongoDBObject(
                  "$box" -> MongoDBList(
                    MongoDBList(74.2332, -75.23452),
                    MongoDBList(123, 456)
                  )
                )
              )
            )
          )
        }
      }
      "... $center" in {
        "With an explicit GeoCoords instance" in {
          val near = "foo".$geoWithin $center (GeoCoords(50, 50), 10)
          log.error("Near: %s", near.getClass)
          val x = MongoDBObject(
            "foo" -> MongoDBObject(
              "$geoWithin" -> MongoDBObject(
                "$center" -> MongoDBList(
                  MongoDBList(50, 50),
                  10
                )
              )
            )
          )
          log.error("Match X: %s", x.getClass)
          near must beEqualTo(x)
        }
        "With a tuple converted coordinate set" in {
          val near = "foo".$geoWithin $center ((50, 50), 10)
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$geoWithin" -> MongoDBObject(
                  "$center" -> MongoDBList(
                    MongoDBList(50, 50),
                    10
                  )
                )
              )
            )
          )
        }
      }
      "... $centerSphere" in {
        "With an explicit GeoCoords instance" in {
          val near = "foo".$geoWithin $centerSphere (GeoCoords(50, 50), 10)
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$geoWithin" -> MongoDBObject(
                  "$centerSphere" -> MongoDBList(
                    MongoDBList(50, 50),
                    10
                  )
                )
              )
            )
          )
        }
        "With a tuple converted coordinate set" in {
          val near = "foo".$geoWithin $centerSphere ((50, 50), 10)
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$geoWithin" -> MongoDBObject(
                  "$centerSphere" -> MongoDBList(
                    MongoDBList(50, 50),
                    10
                  )
                )
              )
            )
          )
        }
      }
    }
    "Support.$within ..." in {
      "... $box" in {
        "With an explicit GeoCoords instance" in {
          val near = "foo".$within $box (GeoCoords(74.2332, -75.23452), GeoCoords(123, 456))
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$within" -> MongoDBObject(
                  "$box" -> MongoDBList(
                    MongoDBList(74.2332, -75.23452),
                    MongoDBList(123, 456)
                  )
                )
              )
            )
          )
        }
        "With a tuple converted coordinate set" in {
          val near = "foo".$within $box ((74.2332, -75.23452), (123, 456))
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$within" -> MongoDBObject(
                  "$box" -> MongoDBList(
                    MongoDBList(74.2332, -75.23452),
                    MongoDBList(123, 456)
                  )
                )
              )
            )
          )
        }
      }
      "... $center" in {
        "With an explicit GeoCoords instance" in {
          val near = "foo".$within $center (GeoCoords(50, 50), 10)
          log.error("Near: %s", near.getClass)
          val x = MongoDBObject(
            "foo" -> MongoDBObject(
              "$within" -> MongoDBObject(
                "$center" -> MongoDBList(
                  MongoDBList(50, 50),
                  10
                )
              )
            )
          )
          log.error("Match X: %s", x.getClass)
          near must beEqualTo(x)
        }
        "With a tuple converted coordinate set" in {
          val near = "foo".$within $center ((50, 50), 10)
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$within" -> MongoDBObject(
                  "$center" -> MongoDBList(
                    MongoDBList(50, 50),
                    10
                  )
                )
              )
            )
          )
        }
      }
      "... $centerSphere" in {
        "With an explicit GeoCoords instance" in {
          val near = "foo".$within $centerSphere (GeoCoords(50, 50), 10)
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$within" -> MongoDBObject(
                  "$centerSphere" -> MongoDBList(
                    MongoDBList(50, 50),
                    10
                  )
                )
              )
            )
          )
        }
        "With a tuple converted coordinate set" in {
          val near = "foo".$within $centerSphere ((50, 50), 10)
          near must beEqualTo(
            MongoDBObject(
              "foo" -> MongoDBObject(
                "$within" -> MongoDBObject(
                  "$centerSphere" -> MongoDBList(
                    MongoDBList(50, 50),
                    10
                  )
                )
              )
            )
          )
        }
      }
    }
  }

  "Chained core operators" should {
    "Function correctly" in {
      val ltGt = "foo" $gte 15 $lt 35.2 $ne 16
      log.debug("LTGT: %s", ltGt)
      ltGt must beEqualTo(MongoDBObject("foo" -> MongoDBObject("$gte" -> 15, "$lt" -> 35.2, "$ne" -> 16)))
    }
    "Function correctly with deeper nesting e.g. $not" in {
      val ltGt = "foo" $not {
        _ $gte 15 $lt 35.2 $ne 16
      }
      log.debug("LTGT: %s", ltGt)
      ltGt must beEqualTo(MongoDBObject("foo" -> MongoDBObject("$not" -> MongoDBObject("$gte" -> 15, "$lt" -> 35.2, "$ne" -> 16))))
    }
  }
}

