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

class MongoDBListSpec extends Specification with PendingUntilFixed {
  "MongoDBList Factory & Builder" should {
    val x = Seq(5, 9, 212, "x", "y", 22.98)
    val y = Seq("spam", "eggs", "foo", "bar")

    "Support 'empty', returning a BasicDBList" in {
      val dbObj = MongoDBList.empty

      dbObj must haveSuperClass[BasicDBList]
      dbObj must haveSize(0)
    }

    "support a 2.8 factory interface which returns a BasicDBList" in {
      val dbLst = MongoDBList("x", "y", 5, 123.82, 84, "spam", "eggs")
      // A Java version to compare with
      val jLst = new com.mongodb.BasicDBList
      jLst.add("x")
      jLst.add("y")
      jLst.add(5.asInstanceOf[AnyRef])
      jLst.add(123.82.asInstanceOf[AnyRef]) 
      jLst.add(84.asInstanceOf[AnyRef])
      jLst.add("spam") 
      jLst.add("eggs")
      val jObj = jLst.result

      dbLst must haveSuperClass[BasicDBList]
      jLst must haveSuperClass[BasicDBList]
      dbLst must beEqualTo(jLst)
    }
    "Support a 2.8 builder interface which returns a BasicDBList" in {
      val builder = MongoDBList.newBuilder

      builder += "foo"
      builder += "bar"
      builder += "x" 
      builder += "y"
      builder ++= List(5, 212.8, "spam", "eggs", "type erasure" -> "sucks", "omg" -> "ponies!")

      val dbLst = builder.result

      dbLst must haveSuperClass[BasicDBList]
      dbLst must haveSize(10)
    }

    "Support a mix of other lists and flat items and create a single BasicDBList" in {
      val dbLst = MongoDBList(x, y, "omg" -> "ponies", 5, 212.8)
      dbLst must haveSuperClass[BasicDBList]
      dbLst must haveSize(5)
      dbLst must haveTheSameElementsAs(Seq(x, y, MongoDBObject("omg" -> "ponies"), 5, 212.8))
    }
    "Support A list/tuple of dbobject declarations and convert them to a dbobject cleanly" in {
      val dbLst = MongoDBList(x, y, "omg" -> "ponies", 5, 
                              MongoDBObject("x" -> "y", "foo" -> "bar", "bar" -> "baz"),
                              212.8)
      dbLst must haveSuperClass[BasicDBList]
      dbLst must haveSize(6)
      dbLst must haveTheSameElementsAs(Seq(x, y, MongoDBObject("omg" -> "ponies"), 5, 
                                      MongoDBObject("x" -> "y", "foo" -> "bar", "bar" -> "baz"), 212.8))
    }

    "Convert tuple pairs correctly" in {
      val dbList = MongoDBList("omg" -> "ponies")
      dbList must haveSuperClass[BasicDBList]
      dbList must haveSize(1)
      /*dbList must beEqualTo(List(MongoDBObject("omg" -> "ponies")))*/
    }
  }

}


// vim: set ts=2 sw=2 sts=2 et:
