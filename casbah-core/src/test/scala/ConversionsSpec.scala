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

package com.mongodb.casbah.commons
package conversions

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._

import org.scala_tools.time.Imports._

import org.specs._
import org.specs.specification.PendingUntilFixed

class ConversionsSpec extends Specification with PendingUntilFixed {

  type JDKDate = java.util.Date
  

  def clearConversions = beforeContext { 
    DeregisterConversionHelpers()
    DeregisterJodaTimeConversionHelpers()
  }
  
  "Casbah's Conversion Helpers"  ->-(clearConversions) should {
    shareVariables
    
    implicit val mongoDB = MongoConnection()("casbahTest")
    mongoDB.dropDatabase()
    val jodaDate: DateTime = DateTime.now
    val jdkDate: JDKDate = new JDKDate(jodaDate.getMillis)

    jodaDate.getMillis must beEqualTo(jdkDate.getTime)

    "Fail to serialize Joda DateTime Objects unless explicitly loaded." in {
      mongoDB must notBeNull
      val mongo = mongoDB("dateFail")
      mongo.dropCollection()

      lazy val saveDate = { mongo += MongoDBObject("date" -> jodaDate, "type" -> "joda") } 

      saveDate must throwA[IllegalArgumentException]

      mongo += MongoDBObject("date" -> jdkDate, "type" -> "jdk")
      
      val jdkEntry = mongo.findOne(MongoDBObject("type" -> "jdk"), 
                                   MongoDBObject("date" -> 1))

      jdkEntry.get
      jdkEntry must beSomething

      jdkEntry.get must notBeNull
      jdkEntry.get.getAs[JDKDate]("date") must beSome(jdkDate)
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
      // Casting it as something it isn't will fail
      lazy val getDate = { jodaEntry.get.getAs[JDKDate]("date") } 
      // Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      getDate.get must throwA[ClassCastException] 
    } 

    "Be successfully deregistered." in {
      mongoDB must notBeNull
      val mongo = mongoDB("conversionDeReg")
      mongo.dropCollection()
      RegisterConversionHelpers()
      RegisterJodaTimeConversionHelpers()
      DeregisterConversionHelpers()
      DeregisterJodaTimeConversionHelpers()
      lazy val testJodaInsert = { mongo += MongoDBObject("date" -> jodaDate, "type" -> "joda") } 

      testJodaInsert must throwA[IllegalArgumentException]

      // Normal JDK Date should work
      mongo += MongoDBObject("date" -> jdkDate, "type" -> "jdk")
      
      val jdkEntry = mongo.findOne(MongoDBObject("type" -> "jdk"), 
                                   MongoDBObject("date" -> 1))

      jdkEntry must beSomething

      jdkEntry.get must notBeNull
      jdkEntry.get.getAs[JDKDate]("date") must beSome(jdkDate)
      // Casting it as something it isn't will fail
      lazy val getDate = { jdkEntry.get.getAs[DateTime]("date") } 
      // Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      getDate.get must throwA[ClassCastException] 
    }
    "Inserting a JDKDate should still allow retrieval as JodaTime after Conversions load" in {
      mongoDB must notBeNull
      val mongo = mongoDB("conversionConversion")
      mongo.dropCollection()
      RegisterConversionHelpers()

      mongo += MongoDBObject("date" -> jdkDate, "type" -> "jdk")
      
      val jdkEntry = mongo.findOne(MongoDBObject("type" -> "jdk"), 
                                   MongoDBObject("date" -> 1))


      jdkEntry must beSomething

      jdkEntry.get must notBeNull
      jdkEntry.get.getAs[JDKDate]("date") must beSome(jdkDate)
      // Casting it as something it isn't will fail
      lazy val getDate = { jdkEntry.get.getAs[DateTime]("date") } 
      // Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      getDate.get must throwA[ClassCastException] 

      RegisterJodaTimeConversionHelpers()

      val jodaEntry = mongo.findOne(MongoDBObject("type" -> "jdk"), 
                                    MongoDBObject("date" -> 1))


      jodaEntry must beSomething

      jodaEntry.get must notBeNull
      jodaEntry.get.getAs[DateTime]("date") must beSome(jodaDate)
      // Casting it as something it isn't will fail
      lazy val getConvertedDate = { jodaEntry.get.getAs[JDKDate]("date") } 
      // Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      getConvertedDate.get must throwA[ClassCastException] 
    }
    "toString-ing a JODA Date with JODA Conversions loaded doesn't choke horribly." in {
      RegisterConversionHelpers()
      val jodaEntry: DBObject = MongoDBObject("type" -> "jdk",
                                              "date" -> jdkDate)

      jodaEntry must notBeNull
      /*jodaEntry.getAs[DateTime]("date") must beSome(jdkDate)
      // Casting it as something it isn't will fail
      lazy val getDate = { jodaEntry.getAs[JDKDate]("date") } 
      // Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      getDate.get must throwA[ClassCastException] */
      RegisterJodaTimeConversionHelpers()
      
      val json = jodaEntry.toString

      json must notBeNull
    }
  }
}
// vim: set ts=2 sw=2 sts=2 et:
