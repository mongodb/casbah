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

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.conversions.scala._

import org.scala_tools.time.Imports._
import com.mongodb.casbah.commons.test.CasbahSpecification

class GroupSpec extends CasbahSpecification {

  "Casbah's Group Interfaces" should {
    implicit val mongoDB = MongoConnection()("casbahIntegration")
  

    "Work with a normal non-finalized Group statement" in {
      val cond = MongoDBObject()
      val key = MongoDBObject("publicationYear" -> 1)
      val initial = MongoDBObject("count" -> 0)
      val reduce = "function(obj, prev) { prev.count++ }"
      val result = mongoDB("books").group(key, cond, initial, reduce)
      println(result)
      success
    }

    // Test for SCALA-37
    "Work with a trivial finalized Group statement" in {
      val cond = MongoDBObject()
      val key = MongoDBObject("publicationYear" -> 1)
      val initial = MongoDBObject("count" -> 0)
      val reduce = "function(obj, prev) { prev.count++ }"
      val result = mongoDB("books").group(key, cond, initial, reduce, "")
      println(result)
      success
    }

    "Work with a less-trivial finalized Group statement" in {
      val cond = MongoDBObject()
      val key = MongoDBObject("publicationYear" -> 1)
      val initial = MongoDBObject("count" -> 0)
      val reduce = "function(obj, prev) { prev.count++; }"
      val finalize = "function(out) { out.avg_count = 3; }"
      val result = mongoDB("books").group(key, cond, initial, reduce, finalize)
      result.forall(_.getOrElse("avg_count", 2) == 3)
    }

    "Use a default Group statement that changes nothing" in {
      val cond = MongoDBObject()
      val key = MongoDBObject("publicationYear" -> 1)
      val r1 = mongoDB("books").group(key, cond)
      val r2 = mongoDB("books").group(key, cond, "function(o,p) { }")
      r1 must haveTheSameElementsAs(r2)
    }

  }


}

// vim: set ts=2 sw=2 sts=2 et:
