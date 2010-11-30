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
package map_reduce

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.conversions.scala._

import org.scala_tools.time.Imports._

import org.specs._
import org.specs.specification.PendingUntilFixed

class MapReduceSpec extends Specification with PendingUntilFixed with Logging {

  "Casbah's Map/Reduce Engine" should {
    shareVariables

    implicit val mongoDB = MongoConnection()("casbahTest")
    mongoDB.dropDatabase()

    "Handle error conditions such as non-existent collections gracefully" in {
      mongoDB must notBeNull

      val seed = DateTime.now.getMillis
      implicit val mongo = mongoDB("mapReduce.nonexistant.foo.bar.baz.%s".format(seed))
      mongo.dropCollection()
      
      val keySet = distinctKeySet("Foo", "bar", "Baz")

      for (x <- keySet) {
        log.trace("noop.")
      }

      keySet must notBeNull
      keySet must beEmpty 

    }
  }

  def distinctKeySet(keys: String*)(implicit mongo: MongoCollection): MapReduceResult = {
    val keySet = keys.flatMap(x => "'%s': this.%s, ".format(x, x)).mkString

    val map = "function () { emit({%s}, 1); }".format(keySet)

    val reduce = "function(k, v) { return 1; }"

    val mr = MapReduceCommand(mongo.getName, map, reduce, None, None, None, None, None)

    val result = mongo.mapReduce(mr)

    result
  }
}

// vim: set ts=2 sw=2 sts=2 et:
