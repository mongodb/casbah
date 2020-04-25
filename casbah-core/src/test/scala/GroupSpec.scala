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

import java.io.IOException
import scala.sys.process._
import scala.jdk.CollectionConverters._
import org.specs2.specification.Scope

import com.mongodb.util.JSON
import com.mongodb.casbah.Imports._

class GroupSpec extends CasbahDBTestSpecification {

  "Casbah's Group Interfaces" should {

    "Work with a normal non-finalized Group statement" in new testData {
      val cond = MongoDBObject()
      val key = MongoDBObject("publicationYear" -> 1)
      val initial = MongoDBObject("count" -> 0)
      val reduce = "function(obj, prev) { prev.count++ }"

      val result = collection.group(key, cond, initial, reduce)
      result.size must beEqualTo(31)
    }

    // Test for CASBAH-37
    "Work with a trivial finalized Group statement" in new testData {
      val cond = MongoDBObject()
      val key = MongoDBObject("publicationYear" -> 1)
      val initial = MongoDBObject("count" -> 0)
      val reduce = "function(obj, prev) { prev.count++ }"

      val result = collection.group(key, cond, initial, reduce, "")
      result.size must beEqualTo(31)
    }

    "Work with a less-trivial finalized Group statement" in new testData {
      val cond = MongoDBObject()
      val key = MongoDBObject("publicationYear" -> 1)
      val initial = MongoDBObject("count" -> 0)
      val reduce = "function(obj, prev) { prev.count++; }"
      val finalise = "function(out) { out.avg_count = 3; }"

      val result = collection.group(key, cond, initial, reduce, finalise)
      result.forall(_.getOrElse("avg_count", 2) == 3)
    }
  }

  trait testData extends Scope {
    val jsonFile = "./casbah-core/src/test/resources/bookstore.json"

    database.dropDatabase()
    try {
      Seq("mongoimport", "-d", database.name, "-c", collection.name, "--drop", "--jsonArray", jsonFile).!!
    } catch {
      case ex: IOException => {
        val source = scala.io.Source.fromFile(jsonFile)
        val lines = source.mkString
        source.close()

        val rawDoc = JSON.parse(lines).asInstanceOf[BasicDBList]
        val docs = (for (doc <- rawDoc) yield doc.asInstanceOf[DBObject]).asJava
        collection.underlying.insert(docs)
      }
    }

    // Verify the treasury data is loaded or skip the test for now
    collection.count() must beGreaterThan(0)
  }

}

