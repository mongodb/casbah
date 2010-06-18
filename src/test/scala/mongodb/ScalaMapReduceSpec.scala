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
import map_reduce._

import Implicits.{mongoDBAsScala, mongoConnAsScala, mongoCollAsScala, mongoCursorAsScala}
import com.mongodb._
import org.scalatest.{GivenWhenThen, FeatureSpec}

class ScalaMapReduceSpec extends FeatureSpec with GivenWhenThen with Logging {
  feature("The map/reduce engine works correctly") {
    val conn = new Mongo().asScala
    scenario("Error conditions such as a non-existant collection should not blow up but return an error-state result") {
      given("A Mongo object connected to the default [localhost]")
      assert(conn != null)
      implicit val mongo = conn("foo")("barBazFooBar") // should be nonexistant - @todo ensure it is random
      when("A Map Reduce is run, it doesn't explode despite failure")
      val keySet = distinctKeySet("Foo", "bar", "Baz")
      then("Iteration doesn't blow up either")
      for (x <- keySet) {
        log.info("Keyset entry: %s", x)
      }

    }
  }
  def distinctKeySet(keys: String*)(implicit mongo: MongoCollection): MapReduceResult = {
    log.debug("Running a Distinct KeySet MapReduce for Keys (%s)", keys)
    val keySet = keys.flatMap(x => "'%s': this.%s, ".format(x, x)).mkString
    log.trace("KeySet: %s", keySet)
    val map = "function () { emit({%s}, 1); }".format(keySet)
    log.debug("Map Function: %s", map)
    val reduce = "function(k, v) { return 1; }"
    log.trace("Reduce Function: %s", reduce)
    val mr = MapReduceCommand(mongo.getName, map, reduce, None, None, None, None, None)
    log.debug("Map/Reduce Command: %s", mr)
    val result = mongo.mapReduce(mr)
    log.trace("M/R Result: %s", result)
    result
  }
}
