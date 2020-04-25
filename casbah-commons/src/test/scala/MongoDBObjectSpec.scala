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

package com.mongodb.casbah.test.commons

import scala.Some

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification

import com.github.nscala_time.time.Imports._

class TodoSpec extends CasbahMutableSpecification {
  type JDKDate = java.util.Date

  "MongoDBObject issues and edge cases" should {

    "If a BasicDBList is requested it quietly is recast to something which can be a Seq[] or List[]" in {
      val lst = new BasicDBList()
      lst.add("Brendan")
      lst.add("Mike")
      lst.add("Tyler")
      lst.add("Jeff")
      lst.add("Jesse")
      lst.add("Christian")
      val doc = MongoDBObject("driverAuthors" -> lst)

      doc.getAs[Seq[_]]("driverAuthors") must beSome[Seq[_]]
      doc.getAs[List[_]]("driverAuthors") must beSome[List[_]]
    }
  }

  /* "CASBAH-42, storing Java Arrays in a DBObject shouldn't break .equals and .hashcode" in {
    val one = MongoDBObject("anArray" -> Array(MongoDBObject("one" -> "oneVal"), MongoDBObject("two" -> "twoVal")))
    val two = MongoDBObject("anArray" -> Array(MongoDBObject("one" -> "oneVal"), MongoDBObject("two" -> "twoVal")))
    one must beEqualTo(two)
  } */
}








