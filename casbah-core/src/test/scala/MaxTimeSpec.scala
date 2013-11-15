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

package com.mongodb.casbah.test.core

import com.mongodb.MongoExecutionTimeoutException
import org.specs2.specification.{Step, Fragments}
import scala.concurrent.duration.{Duration, SECONDS}

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification
import com.mongodb.casbah.map_reduce.MapReduceCommand


class MaxTimeSpec extends CasbahMutableSpecification {

  sequential

  lazy val db = MongoClient()("casbahTest")
  lazy val collection = db("maxTime")
  val oneSecond = Duration(1, SECONDS)

  def setup(): Unit = {
    collection.drop()
    enableMaxTimeFailPoint()
  }
  def teardown(): Unit = disableMaxTimeFailPoint()
  override def map(fs: => Fragments) = Step(setup()) ^ fs ^ Step(teardown())

  "MaxTime" should {
    "be supported by aggregation" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      val aggregationOptions = AggregationOptions(oneSecond)
      lazy val aggregation = collection.aggregate(
        List(MongoDBObject("$match" -> ("score" $gte 7)),
          MongoDBObject("$project" -> MongoDBObject("score" -> 1))),
        aggregationOptions)

      aggregation should throwA[MongoExecutionTimeoutException]
    }

    "be supported by findAndModify" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      lazy val findAndModify = collection.findAndModify(query=MongoDBObject("_id" -> 1), fields=MongoDBObject(),
        sort=MongoDBObject(), remove=false, update=MongoDBObject("a" -> 1),
        returnNew=true, upsert=false, oneSecond)

      findAndModify should throwA[MongoExecutionTimeoutException]
    }

    "be supported by cursors" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      val cursor = collection.find().maxTime(oneSecond)
      cursor.next() should throwA[MongoExecutionTimeoutException]
      cursor.toList should throwA[MongoExecutionTimeoutException]
    }

    "be supported when calling findOne" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      lazy val op = collection.findOne(MongoDBObject.empty, MongoDBObject.empty,
                                       MongoDBObject.empty, ReadPreference.Primary,
                                       oneSecond)
      op should throwA[MongoExecutionTimeoutException]
    }

    "be supported when calling one" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      lazy val op = collection.find().maxTime(oneSecond).one()
      op should throwA[MongoExecutionTimeoutException]
    }

    "be supported when calling getCount" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      lazy val op = collection.getCount(maxTime=oneSecond)
      op should throwA[MongoExecutionTimeoutException]
    }

    "be supported when calling count" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      lazy val op = collection.count(maxTime=oneSecond)
      op should throwA[MongoExecutionTimeoutException]
    }

    "be supported when calling a chained count" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      lazy val op = collection.find().maxTime(oneSecond).count
      op should throwA[MongoExecutionTimeoutException]
    }

    "be supported when calling size" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      lazy val op = collection.find().maxTime(oneSecond).size
      op should throwA[MongoExecutionTimeoutException]
    }

    "be supported when calling commands" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      lazy val op = db.command(MongoDBObject("isMaster" -> 1, "maxTimeMS" -> 1)).throwOnError()
      op should throwA[MongoExecutionTimeoutException]
    }

    "be supported when calling mapReduce" in {
      serverIsAtLeastVersion(2,5) must beTrue.orSkip("Needs server >= 2.5")
      collection += MongoDBObject("x" -> List(1,2,3))
      collection += MongoDBObject("x" -> List(1,2,3))
      val mapJS = "function(){ for ( var i=0; i<this.x.length; i++ ){ emit( this.x[i] , 1 ); } }";
      val reduceJS = "function(key,values){ var sum=0; for( var i=0; i<values.length; i++ ) sum += values[i]; return sum;}";
      lazy val op = collection.mapReduce(mapJS, reduceJS, "test", maxTime=Some(oneSecond))
      op should throwA[MongoExecutionTimeoutException]
    }
  }
}