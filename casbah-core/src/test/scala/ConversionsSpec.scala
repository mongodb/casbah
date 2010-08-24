/**
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
 *     http://github.com/novus/casbah
 * 
 */

package com.novus.casbah
package conversions

import com.novus.casbah.Imports._
import com.novus.casbah.conversions.scala._

import org.scala_tools.time.Imports._

import org.specs._
import org.specs.specification.PendingUntilFixed

class ConversionsSpec extends Specification with PendingUntilFixed {
  
  "Casbah's Conversion Helpers" should {
    shareVariables
    
    implicit val mongoDB = MongoConnection()("casbahTest")
    mongoDB.dropDatabase()
    val jodaDate = DateTime.now
    val jdkDate = new java.util.Date

    "Fail to serialize Joda DateTime Objects unless explicitly loaded." in {
      mongoDB must notBeNull
      val mongo = mongoDB("dateFail")
      mongo.dropCollection()

      lazy val saveDate = { mongo += MongoDBObject("date" -> jodaDate, "type" -> "joda") } 

      saveDate must throwA[IllegalArgumentException]

      mongo += MongoDBObject("date" -> jdkDate, "type" -> "jdk")
      
      val jdkEntry = mongo.findOne(MongoDBObject("type" -> "jdk"), 
                                   MongoDBObject("date" -> 1))

      jdkEntry must beSomething

      jdkEntry.get must notBeNull
      jdkEntry.get.getAs[java.util.Date]("date") must beSome(jdkDate)
    }

    "Successfully serialize & deserialize Joda DateTime Objects when convertors are loaded." in {
      mongoDB must notBeNull
      val mongo = mongoDB("jodaSerDeser")
      mongo.dropCollection()
      RegisterConversionHelpers()
      RegisterJodaTimeConversionHelpers()
      // TODO - Matcher verification of these being loaded
      
      mongo += MongoDBObject("date" -> jodaDate, "type" -> "joda")

      val jodaEntry = mongo.findOne(MongoDBObject("type" -> "joda"), 
                                    MongoDBObject("date" -> 1))

      jodaEntry must beSomething
      jodaEntry.get must notBeNull
      //jodaEntry.get.get("date") must beSome[DateTime]
      jodaEntry.get.getAs[DateTime]("date") must beSome(jodaDate)
      lazy val getDate = { jodaEntry.get.getAs[java.util.Date]("date") } 
      
      getDate must throwA[ClassCastException]
    }
  }
}
// vim: set ts=2 sw=2 sts=2 et:
