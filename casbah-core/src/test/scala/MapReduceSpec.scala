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

@SuppressWarnings(Array("deprecation"))
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
      log.warn("KeySet: %s", keySet)

      for (x <- keySet) {
        log.trace("noop.")
      }

      keySet must notBeNull
      keySet must beEmpty

    }
  }

  "MongoDB 1.7+ Map/Reduce functionality" should {
    implicit val mongoDB = MongoConnection()("casbahTest_MR")

    verifyAndInitTreasuryData

    shareVariables

    val mapJS = """
      function m() {
          emit( typeof(this._id) == "number" ? this._id : this._id.getYear(), { count: 1, sum: this.bc10Year })
      }
    """

    val reduceJS = """
      function r( year, values ) {
          var n = { count: 0,  sum: 0 }
          for ( var i = 0; i < values.length; i++ ){ 
              n.sum += values[i].sum;
              n.count += values[i].count;
          }
          
          return n;
      }
    """

    val finalizeJS = """
      function f( year, value ){
          value.avg = value.sum / value.count;
          return value.avg;
      }
    """

    "Produce results in a named collection for all data" in {
      val coll = mongoDB("yield_historical.in")
      val result = coll.mapReduce(
        mapJS,
        reduceJS,
        "yield_historical.all",
        finalizeFunction = Some(finalizeJS))

      log.warn("M/R Result: %s", result)

      result must notBeNull

      result.isError must beFalse
      result.size must beEqualTo(result.raw.expand[Int]("counts.output").getOrElse(-1))
    }

    "Produce results in a named collection for inline data" in {
      val coll = mongoDB("yield_historical.in")
      val result = coll.mapReduce(
        mapJS,
        reduceJS,
        MapReduceInlineOutput,
        finalizeFunction = Some(finalizeJS),
        verbose = true)

      log.warn("M/R Result: %s", result)

      result must notBeNull

      result.isError must beFalse
      result.raw.getAs[String]("result") must beNone
      result.size must beGreaterThan(0)
      result.size must beEqualTo(result.raw.expand[Int]("counts.output").getOrElse(-1))

      val item = result.next
      item must haveClass[com.mongodb.CommandResult]
      item must haveSuperClass[DBObject]
      item must beEqualTo(MongoDBObject("_id" -> 90.0, "value" -> 8.552400000000002))
      log.warn("First item: %s", item)
    }

    "Produce results for merged output" in {
      verifyAndInitTreasuryData

      import java.util.Date
      mongoDB("yield_historical.merged").size must beEqualTo(0)
      mongoDB("yield_historical.aughts").size must beEqualTo(0)
      mongoDB("yield_historical.nineties").size must beEqualTo(0)

      val cmd90s = MapReduceCommand(
        "yield_historical.in",
        mapJS,
        reduceJS,
        "yield_historical.nineties",
        Some("_id" $lt new Date(100, 1, 1)),
        finalizeFunction = Some(finalizeJS),
        verbose = true)

      val result90s = mongoDB.mapReduce(cmd90s)

      log.info("M/R result90s: %s", result90s)

      result90s must notBeNull

      result90s.isError must beFalse
      result90s.raw.getAs[String]("result") must beSome("yield_historical.nineties")
      result90s.size must beGreaterThan(0)
      result90s.size must beEqualTo(result90s.raw.expand[Int]("counts.output").getOrElse(-1))

      val cmd00s = MapReduceCommand(
        "yield_historical.in",
        mapJS,
        reduceJS,
        "yield_historical.aughts",
        Some("_id" $gt new Date(99, 12, 31)),
        finalizeFunction = Some(finalizeJS),
        verbose = true)

      val result00s = mongoDB.mapReduce(cmd00s)

      log.info("M/R result00s: %s", result00s)

      result00s must notBeNull

      result00s.isError must beFalse
      result00s.raw.getAs[String]("result") must beSome("yield_historical.aughts")
      result00s.size must beGreaterThan(0)
      result00s.size must beEqualTo(result00s.raw.expand[Int]("counts.output").getOrElse(-1))

      "Merge the 90s and 00s into a single output collection" in {
        "reading the earlier output collections" in {
          cmd00s.query = None
          cmd00s.input = "yield_historical.aughts"
          cmd00s.output -> MapReduceMergeOutput("yield_historical.merged")

          cmd90s.query = None
          cmd90s.input = "yield_historical.nineties"
          cmd90s.output -> MapReduceMergeOutput("yield_historical.merged")

          var result = mongoDB.mapReduce(cmd90s)
          result must notBeNull
          result.isError must beFalse

          result.raw.getAs[String]("result") must beSome("yield_historical.merged")

          result = mongoDB.mapReduce(cmd00s)
          result must notBeNull
          result.isError must beFalse

          result.raw.getAs[String]("result") must beSome("yield_historical.merged")

          result.outputCount must_== (21)
          result.size must_== (result.outputCount)

        }
        "Using a fresh query run" in {
          cmd00s.output = MapReduceMergeOutput("yield_historical.merged_fresh")
          cmd90s.output = MapReduceMergeOutput("yield_historical.merged_fresh")

          var result = mongoDB.mapReduce(cmd90s)
          result must notBeNull
          result.isError must beFalse

          result.raw.getAs[String]("result") must beSome("yield_historical.merged_fresh")

          result = mongoDB.mapReduce(cmd00s)
          result must notBeNull
          result.isError must beFalse

          result.raw.getAs[String]("result") must beSome("yield_historical.merged_fresh")

          result.outputCount must_== (21)
          result.size must_== (result.outputCount)
        }
      }
    }

    "Produce results for reduced output (multiples into a single final collection)" in {
      verifyAndInitTreasuryData
      import java.util.Date

      mongoDB("yield_historical.all").size must beEqualTo(0)
      mongoDB("yield_historical.aughts").size must beEqualTo(0)
      mongoDB("yield_historical.nineties").size must beEqualTo(0)

      val cmd90s = MongoDBObject(
        "mapreduce" -> "yield_historical.in",
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "verbose" -> true,
        "query" -> "_id" $lt new Date(100, 1, 1),
        "out" -> "yield_historical.nineties")

      val result90s = mongoDB.command(cmd90s)

      log.info("M/R result90s: %s", result90s)

      result90s must notBeNull

      result90s.getAs[Double]("ok") must beSome(1.0)
      result90s.getAs[String]("result") must beSome("yield_historical.nineties")

      Some(mongoDB(result90s.as[String]("result")).size) must beEqualTo(result90s.expand[Int]("counts.output"))

      val cmd00s = MongoDBObject(
        "mapreduce" -> "yield_historical.in",
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "verbose" -> true,
        "query" -> "_id" $gt new Date(99, 12, 31),
        "out" -> "yield_historical.aughts")

      val result00s = mongoDB.command(cmd00s)

      log.info("M/R result00s: %s", result00s)

      result00s must notBeNull

      result00s.getAs[Double]("ok") must beSome(1.0)
      result00s.getAs[String]("result") must beSome("yield_historical.aughts")

      Some(mongoDB(result00s.as[String]("result")).size) must beEqualTo(result00s.expand[Int]("counts.output"))

      "Reduce the 90s and 00s into a single output collection" in {
        "Querying against the raw data " in {
          cmd00s += "out" -> MongoDBObject("reduce" -> "yield_historical.all")
          //cmd00s += "finalize" -> finalizeJS

          log.info("cmd 00s: %s", cmd00s)

          cmd90s += "out" -> MongoDBObject("reduce" -> "yield_historical.all")
          //cmd90s += "finalize" -> finalizeJS

          log.info("cmd 90s: %s", cmd90s)

          var result = mongoDB.command(cmd90s)
          result must notBeNull
          log.info("First pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.all")

          result = mongoDB.command(cmd00s)
          result must notBeNull
          log.info("Second pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.all")

          result.expand[Int]("counts.output") must beSome(21)

          val mongo = mongoDB(result.as[String]("result"))
          Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))
        }

        "Querying against the intermediary collections" in {
          cmd00s += "out" -> MongoDBObject("reduce" -> "yield_historical.all")
          cmd00s -= "query"
          cmd00s += "mapreduce" -> "yield_historical.out.aughts"
          //cmd00s += "finalize" -> finalizeJS

          log.info("cmd 00s: %s", cmd00s)

          cmd90s += "out" -> MongoDBObject("reduce" -> "yield_historical.all")
          cmd90s -= "query"
          cmd90s += "mapreduce" -> "yield_historical.nineties"
          //cmd90s += "finalize" -> finalizeJS

          log.info("cmd 90s: %s", cmd90s)

          var result = mongoDB.command(cmd90s)
          result must notBeNull
          log.info("First pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.all")

          result = mongoDB.command(cmd00s)
          result must notBeNull
          log.info("Second pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.all")

          result.expand[Int]("counts.output") must beSome(21)

          val mongo = mongoDB(result.as[String]("result"))
          Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))

        }
      }
    }

  }

  def verifyAndInitTreasuryData()(implicit mongoDB: MongoDB) = {
    mongoDB("yield_historical.all").drop
    mongoDB("yield_historical.merged").drop
    mongoDB("yield_historical.merged_fresh").drop
    mongoDB("yield_historical.nineties").drop
    mongoDB("yield_historical.aughts").drop

    // Verify the treasury data is loaded or skip the test for now
    mongoDB("yield_historical.in").size must beGreaterThan(0).orSkipExample
  }

  def distinctKeySet(keys: String*)(implicit mongo: MongoCollection): MapReduceResult = {
    val keySet = keys.flatMap(x => "'%s': this.%s, ".format(x, x)).mkString

    val map = "function () { emit({%s}, 1); }".format(keySet)

    val reduce = "function(k, v) { return 1; }"

    //val mr = MapReduceCommand(mongo.getName, map, reduce, MapReduceInlineOutput)

    val result = mongo.mapReduce(map, reduce, MapReduceInlineOutput)

    result
  }
}

// vim: set ts=2 sw=2 sts=2 et:
