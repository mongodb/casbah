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

import scala.collection.mutable
import scala.jdk.CollectionConverters._

import org.bson.codecs.configuration.CodecConfigurationException
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._

import com.github.nscala_time.time.Imports._
import org.specs2.specification._

class ConversionsSpec extends CasbahDBTestSpecification with BeforeEach {
  sequential
  type JDKDate = java.util.Date

  def before = {
    DeregisterConversionHelpers()
    DeregisterJodaTimeConversionHelpers()
    DeregisterJodaLocalDateTimeConversionHelpers()
  }

  "Casbah's Conversion Helpers" should {

    "Properly save Option[_] to MongoDB" in {
      collection.dropCollection()

      collection += MongoDBObject("some" -> Some("foo"), "none" -> None)

      val optDoc = collection.findOne().getOrElse(
        throw new IllegalArgumentException("No document")
      )

      optDoc.getAs[String]("some") must beSome("foo")
      optDoc.getAs[String]("none") must beNone
    }

    val jodaDate: DateTime = DateTime.now
    val localJodaDate: LocalDateTime = jodaDate.toLocalDateTime
    val jdkDate: JDKDate = new JDKDate(jodaDate.getMillis)

    jodaDate.getMillis must beEqualTo(jdkDate.getTime)

    "Fail to serialize Joda DateTime Objects unless explicitly loaded." in {
      collection.dropCollection()

      lazy val saveDate = {
        collection += MongoDBObject("date" -> jodaDate, "type" -> "joda")
      }

      saveDate must throwA[CodecConfigurationException]

      collection += MongoDBObject("date" -> jdkDate, "type" -> "jdk")

      val jdkEntry = collection.findOne(
        MongoDBObject("type" -> "jdk"),
        MongoDBObject("date" -> 1)
      )

      jdkEntry.get.getAs[JDKDate]("date") must beSome(jdkDate)
    }

    "Successfully serialize & deserialize Joda DateTime Objects when DateTime convertors are loaded." in {
      collection.dropCollection()
      RegisterConversionHelpers()
      RegisterJodaTimeConversionHelpers()
      // TODO - Matcher verification of these being loaded

      collection += MongoDBObject("date" -> jodaDate, "type" -> "joda")

      val jodaEntry = collection.findOne(
        MongoDBObject("type" -> "joda"),
        MongoDBObject("date" -> 1)
      )

      jodaEntry.get.getAs[DateTime]("date") must beSome(jodaDate)
      // Casting it as something it isn't will fail
      jodaEntry.get.getAs[JDKDate]("date") must beNone

    }

    "Successfully serialize & deserialize Joda Local/DateTime Objects when DateTime convertors are loaded." in {
      collection.dropCollection()
      RegisterConversionHelpers()
      RegisterJodaTimeConversionHelpers()
      // TODO - Matcher verification of these being loaded

      collection += MongoDBObject("date" -> localJodaDate, "type" -> "joda")

      val jodaEntry = collection.findOne(
        MongoDBObject("type" -> "joda"),
        MongoDBObject("date" -> 1)
      )

      jodaEntry.get.getAs[DateTime]("date") must beSome(jodaDate)
      // Casting it as something it isn't will fail
      jodaEntry.get.getAs[JDKDate]("date") must beNone
    }

    "Successfully serialize & deserialize Joda LocalDateTime Objects when LocalDateTime convertors are loaded." in {
      collection.dropCollection()
      RegisterConversionHelpers()
      RegisterJodaLocalDateTimeConversionHelpers()
      // TODO - Matcher verification of these being loaded

      collection += MongoDBObject("date" -> localJodaDate, "type" -> "joda")

      val jodaEntry = collection.findOne(
        MongoDBObject("type" -> "joda"),
        MongoDBObject("date" -> 1)
      )

      jodaEntry.get.getAs[LocalDateTime]("date") must beSome(localJodaDate)
      // Casting it as something it isn't will fail
      jodaEntry.get.getAs[JDKDate]("date") must beNone
    }

    "Successfully serialize & deserialize Joda Local/DateTime Objects when LocalDateTime convertors are loaded." in {
      collection.dropCollection()
      RegisterConversionHelpers()
      RegisterJodaLocalDateTimeConversionHelpers()
      // TODO - Matcher verification of these being loaded

      collection += MongoDBObject("date" -> jodaDate, "type" -> "joda")

      val jodaEntry = collection.findOne(
        MongoDBObject("type" -> "joda"),
        MongoDBObject("date" -> 1)
      )

      jodaEntry.get.getAs[LocalDateTime]("date") must beSome(localJodaDate)
      // Casting it as something it isn't will fail
      jodaEntry.get.getAs[JDKDate]("date") must beNone
    }

    "Be successfully deregistered." in {
      collection.dropCollection()

      RegisterConversionHelpers()
      RegisterJodaTimeConversionHelpers()
      RegisterJodaLocalDateTimeConversionHelpers()
      DeregisterConversionHelpers()
      DeregisterJodaTimeConversionHelpers()
      DeregisterJodaLocalDateTimeConversionHelpers()

      lazy val testJodaInsert = {
        collection += MongoDBObject("date" -> jodaDate, "type" -> "joda")
      }

      testJodaInsert must throwA[CodecConfigurationException]

      // Normal JDK Date should work
      collection += MongoDBObject("date" -> jdkDate, "type" -> "jdk")

      val jdkEntry = collection.findOne(
        MongoDBObject("type" -> "jdk"),
        MongoDBObject("date" -> 1)
      )

      jdkEntry.get.getAs[JDKDate]("date") must beSome(jdkDate)
      // Casting it as something it isn't will fail
      jdkEntry.get.getAs[DateTime]("date") must beNone
    }

    "Inserting a JDKDate should still allow retrieval as JodaTime after Conversions load" in {
      collection.dropCollection()
      RegisterConversionHelpers()

      collection += MongoDBObject("date" -> jdkDate, "type" -> "jdk")

      val jdkEntry = collection.findOne(
        MongoDBObject("type" -> "jdk"),
        MongoDBObject("date" -> 1)
      )

      jdkEntry must beSome
      jdkEntry.get.getAs[JDKDate]("date") must beSome(jdkDate)
      // Casting it as something it isn't will fail
      jdkEntry.get.getAs[DateTime]("date") must beNone

      RegisterJodaTimeConversionHelpers()

      val jodaEntry = collection.findOne(
        MongoDBObject("type" -> "jdk"),
        MongoDBObject("date" -> 1)
      )

      jodaEntry must beSome
      jodaEntry.get.getAs[DateTime]("date") must beSome(jodaDate)
      // Casting it as something it isn't will fail
      jodaEntry.get.getAs[JDKDate]("date") must beNone
    }

    "toString-ing a JODA Date with JODA Conversions loaded doesn't choke horribly." in {
      RegisterConversionHelpers()
      val jodaEntry: DBObject = MongoDBObject(
        "type" -> "jdk",
        "date" -> jdkDate
      )

      RegisterJodaTimeConversionHelpers()

      val json = jodaEntry.toString
      json must not beNull
    }

    "Handle scala collection types correctly" in {

      "Allow Maps to convert correctly" in {
        collection.dropCollection()
        collection += Map("a" -> "b", "c" -> "d")
        collection.findOne().get must haveEntry("a" -> "b")
      }
      "Allow mutable Maps to convert correctly" in {
        collection.dropCollection()
        collection += mutable.Map("a" -> "b", "c" -> "d")
        collection.findOne().get must haveEntry("a" -> "b")
      }
      "Allow Sets to convert correctly" in {
        collection.dropCollection()
        collection += MongoDBObject("a" -> Set("a", "b", "c"))
        collection.findOne().get.as[MongoDBList]("a") must containTheSameElementsAs(List("a", "b", "c"))

      }
      "Allow mutable Sets to convert correctly" in {
        collection.dropCollection()
        collection += MongoDBObject("a" -> mutable.Set("a", "b", "c"))
        collection.findOne().get.as[MongoDBList]("a") must containTheSameElementsAs(List("a", "b", "c"))

      }
      "Allow Seqs to convert correctly" in {
        collection.dropCollection()
        collection += MongoDBObject("a" -> mutable.Seq("a", "b", "c"))
        collection.findOne().get.as[MongoDBList]("a") must containTheSameElementsAs(List("a", "b", "c"))
      }
      "Allow mutable Seqs to convert correctly" in {
        collection.dropCollection()
        collection += MongoDBObject("a" -> mutable.Seq("a", "b", "c"))
        collection.findOne().get.as[MongoDBList]("a") must containTheSameElementsAs(List("a", "b", "c"))
      }
      "Allow mutable Buffers to convert correctly" in {
        collection.dropCollection()
        collection += MongoDBObject("a" -> mutable.Buffer("a", "b", "c"))
        collection.findOne().get.as[MongoDBList]("a") must containTheSameElementsAs(List("a", "b", "c"))
      }
      "Allow Tuples (Products) to convert correctly" in {
        collection.dropCollection()
        collection += MongoDBObject("a" -> ("a", "b", "c"))
        collection.findOne().get.as[MongoDBList]("a") must containTheSameElementsAs(List("a", "b", "c"))
      }
      "Allow Lists to convert correctly" in {
        collection.dropCollection()
        collection += MongoDBObject("a" -> List("a", "b", "c"))
        collection.findOne().get.as[MongoDBList]("a") must containTheSameElementsAs(List("a", "b", "c"))
      }
      "Allow converted Lists to convert correctly" in {
        collection.dropCollection()
        collection += MongoDBObject("a" -> List("a", "b", "c").asJava)
        collection.findOne().get.as[MongoDBList]("a") must containTheSameElementsAs(List("a", "b", "c"))
      }
    }
  }

  "Casbah and Java Driver custom type encoding" should {
    val encoder = new com.mongodb.DefaultDBEncoder()
    def encode(doc: DBObject): Long = {
      val start = System.currentTimeMillis()
      val encoded = encoder.encode(doc)
      val end = System.currentTimeMillis()
      end - start
    }
    DeregisterConversionHelpers()
    DeregisterJodaTimeConversionHelpers()

    "Produce viable performance numbers to test off of " >> {
      "Encoding DateTimes without any custom encoders registered " in {
        var total = 0.0
        val x = 10000
        for (n <- 1 to x) {
          val doc = MongoDBObject("date1" -> new JDKDate, "date2" -> new JDKDate, "foo" -> "bar", "x" -> 5.2)
          total += encode(doc)
        }

        val avg = total / x

        log.error("[Basic] Average encoding time over %s tries: %f [%s]", x, avg, total)
        avg must beGreaterThan(0.0)
      }

      "Encoding Joda DateTimes with custom encoders registered " in {
        RegisterJodaTimeConversionHelpers()
        var total = 0.0
        val x = 10000
        for (n <- 1 to x) {
          val doc = MongoDBObject("date1" -> DateTime.now, "date2" -> DateTime.now, "foo" -> "bar", "x" -> 5.2)
          total += encode(doc)
        }

        val avg = total / x

        log.error("[Custom Types] Average encoding time over %s tries: %f [%s]", x, avg, total)
        avg must beGreaterThan(0.0)
      }
    }
  }
}
