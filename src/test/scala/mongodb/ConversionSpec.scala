/**
 * Copyright (c) 2010, Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: Portions of this work are derived from the Apache License 2.0 "mongo-scala-driver" work
 * by Alexander Azarov <azarov@osinka.ru>, available from http://github.com/alaz/mongo-scala-driver
 */

package com.novus.casbah
package mongodb
package test

import util.Logging
import gridfs._

import java.security.MessageDigest 
import java.io._

import Implicits._
import com.mongodb._
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers

import org.scala_tools.time.Imports._

import conversions.scala._

class ConversionSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers with Logging {
  DeregisterConversionHelpers()
  feature("The conversions do not work unless explicitly brought into scope.") {
    val conn = new Mongo().asScala
    implicit val mongo = conn("conversions")
    mongo.dropDatabase()
    val now = DateTime.now
    val jDate = new java.util.Date()
    scenario("Unless conversions is scoped, Joda DateTime Objects cannot serialize") {
      given("A Mongo object connected to the default [localhost]")
      assert(conn != null)
      log.info("Date: %s", now)
      evaluating { mongo("dateFail").insert(Map("date" -> now).asDBObject) } should produce [IllegalArgumentException]
      and("A normal java.util.Date should work")
      mongo("dateFail").insert(Map("date" -> jDate).asDBObject) 
    }
    scenario("However, conversion scoping should allow the same DateTime to be saved") {
      given("Imported Conversions") 
      RegisterConversionHelpers()
      RegisterJodaTimeConversionHelpers()
      when("the same date is inserted")
      mongo("dateTest").insert(Map("date" -> now).asDBObject) 
      and("Deserialization works correctly (In that we get back a Joda DateTime")
      val testRow = mongo("dateTest").findOne()
      log.info("Test Row: %s", testRow)
      testRow.get("date").isInstanceOf[DateTime]
      log.info("JodaTime: %s", testRow.get("date").asInstanceOf[DateTime])
      evaluating { testRow.get("date").asInstanceOf[java.util.Date] } should produce [ClassCastException]
    }
    scenario("And Conversions can be deregistered....") {
      given("A Mongo object connected to the default [localhost]")
      DeregisterConversionHelpers()
      assert(conn != null)
      log.info("Date: %s", now)
      evaluating { mongo("dateDeRegedFail").insert(Map("date" -> now).asDBObject) } should produce [IllegalArgumentException]
      and("A normal java.util.Date should work")
      mongo("dateDeRegedFail").insert(Map("date" -> jDate).asDBObject) 
      and("It should not come back as a Joda DateTime")
      val testRow = mongo("dateDeRegedFail").findOne()
      log.info("Test Row: %s", testRow)
      testRow.get("date").isInstanceOf[java.util.Date]
      log.info("JDK Date: %s", testRow.get("date").asInstanceOf[java.util.Date])
      evaluating { testRow.get("date").asInstanceOf[DateTime] } should produce [ClassCastException]
    }
    scenario("Inserting a java.util.Date should still allow return as a Joda DateTime") {
      given("A java.util.Date inserted to MongoDB") 
      mongo("datePostInsertTest").insert(Map("date" -> jDate).asDBObject) 
      and("Should be fetchable back as a JDK Date")
      val testRow = mongo("datePostInsertTest").findOne()
      log.info("Test Row: %s", testRow)
      testRow.get("date").isInstanceOf[java.util.Date]
      log.info("JDK Date: %s", testRow.get("date").asInstanceOf[java.util.Date])
      evaluating { testRow.get("date").asInstanceOf[DateTime] } should produce [ClassCastException]
      then("It should come back as a Joda DateTime once registrations are setup.")
      RegisterConversionHelpers()
      RegisterJodaTimeConversionHelpers()
      val testRow2 = mongo("datePostInsertTest").findOne()
      log.info("Test Row: %s", testRow2)
      testRow2.get("date").isInstanceOf[DateTime]
      log.info("JodaTime: %s", testRow2.get("date").asInstanceOf[DateTime])
      evaluating { testRow2.get("date").asInstanceOf[java.util.Date] } should produce [ClassCastException]

    }
  }
}

// vim: set ts=2 sw=2 sts=2 et:
