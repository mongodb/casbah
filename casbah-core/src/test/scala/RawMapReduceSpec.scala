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

import scala.sys.process._
import org.specs2.specification.Scope
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.conversions.scala._

import com.github.nscala_time.time.Imports._

import com.mongodb.casbah.Imports._


@SuppressWarnings(Array("deprecation"))
class RawMapReduceSpec extends com.mongodb.casbah.commons.test.CasbahMutableSpecification {
  sequential
  implicit val mongoDB = MongoClient()("casbahIntegration")

  "MongoDB 1.7+ Map/Reduce functionality" should {

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

    "Produce results in a named collection for all data" in new testData {
      val cmd = MongoDBObject(
        "mapreduce" -> "yield_historical.in",
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "finalize" -> finalizeJS,
        "verbose" -> true,
        "out" -> "yield_historical.out.all")

      val result = mongoDB.command(cmd)

      log.info("M/R Result: %s", result)


      result.getAs[Double]("ok") must beSome(1.0)
      result.getAs[String]("result") must beSome("yield_historical.out.all")
      /*result.expand[Int]("counts.input") must beSome(5193)
      result.expand[Int]("counts.emit") must beSome(5193)
      result.expand[Int]("counts.output") must beSome(21)*/

      val mongo = mongoDB(result.as[String]("result"))
      Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))
    }

    "Produce results in a named collection for inline data" in new testData {
      val cmd = MongoDBObject(
        "mapreduce" -> "yield_historical.in",
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "finalize" -> finalizeJS,
        "verbose" -> true,
        "out" -> MongoDBObject("inline" -> true))

      val result = mongoDB.command(cmd)

      log.info("M/R Result: %s", result)


      result.getAs[Double]("ok") must beSome(1.0)
      result.getAs[String]("result") must beNone
      result.getAs[MongoDBList]("results") must beSome

      val mongo = result.as[MongoDBList]("results")
      // System.err.println("***" + mongo)
      Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))
      mongo(0) must beDBObject
      mongo(0) must beEqualTo(MongoDBObject("_id" -> 90.0, "value" -> 8.552400000000002))
    }

    "Produce results for merged output" in new testData {

      import java.util.Date
      mongoDB("yield_historical.out.merged").size must beEqualTo(0)
      mongoDB("yield_historical.out.aughts").size must beEqualTo(0)
      mongoDB("yield_historical.out.nineties").size must beEqualTo(0)

      val cmd90s = MongoDBObject(
        "mapreduce" -> "yield_historical.in",
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "finalize" -> finalizeJS,
        "verbose" -> true,
        "query" -> ("_id" $lt new Date(100, 1, 1)),
        "out" -> "yield_historical.out.nineties")

      val result90s = mongoDB.command(cmd90s)

      log.info("M/R result90s: %s", result90s)


      result90s.getAs[Double]("ok") must beSome(1.0)
      result90s.getAs[String]("result") must beSome("yield_historical.out.nineties")

      Some(mongoDB(result90s.as[String]("result")).size) must beEqualTo(result90s.expand[Int]("counts.output"))

      val cmd00s = MongoDBObject(
        "mapreduce" -> "yield_historical.in",
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "finalize" -> finalizeJS,
        "verbose" -> true,
        "query" -> ("_id" $gt new Date(99, 12, 31)),
        "out" -> "yield_historical.out.aughts")

      val result00s = mongoDB.command(cmd00s)

      log.info("M/R result00s: %s", result00s)


      result00s.getAs[Double]("ok") must beSome(1.0)
      result00s.getAs[String]("result") must beSome("yield_historical.out.aughts")

      Some(mongoDB(result00s.as[String]("result")).size) must beEqualTo(result00s.expand[Int]("counts.output"))

      "Merge the 90s and 00s into a single output collection" in {
        "reading the earlier output collections" in {
          cmd00s -= "query"
          cmd00s += "mapreduce" -> "yield_historical.out.aughts"
          cmd00s += "out" -> MongoDBObject("merge" -> "yield_historical.out.merged")

          cmd90s -= "query"
          cmd90s += "mapreduce" -> "yield_historical.out.nineties"
          cmd90s += "out" -> MongoDBObject("merge" -> "yield_historical.out.merged")

          var result = mongoDB.command(cmd90s)
          log.info("First pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.out.merged")

          result = mongoDB.command(cmd00s)
          log.info("second pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.out.merged")

          result.expand[Int]("counts.output") must beSome(21)

          val mongo = mongoDB(result.as[String]("result"))
          Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))
        }
        "Using a fresh query run" in {
          cmd00s += "out" -> MongoDBObject("merge" -> "yield_historical.out.merged_fresh")
          cmd90s += "out" -> MongoDBObject("merge" -> "yield_historical.out.merged_fresh")

          var result = mongoDB.command(cmd90s)
          log.info("First pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.out.merged_fresh")

          result = mongoDB.command(cmd00s)
          log.info("second pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.out.merged_fresh")

          result.expand[Int]("counts.output") must beSome(21)

          val mongo = mongoDB(result.as[String]("result"))
          Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))
        }
      }
    }

    "Produce results for reduced output (multiples into a single final collection)" in new testData {
      import java.util.Date

      mongoDB("yield_historical.out.all").size must beEqualTo(0)
      mongoDB("yield_historical.out.aughts").size must beEqualTo(0)
      mongoDB("yield_historical.out.nineties").size must beEqualTo(0)

      val cmd90s = MongoDBObject(
        "mapreduce" -> "yield_historical.in",
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "verbose" -> true,
        "query" -> ("_id" $lt new Date(100, 1, 1)),
        "out" -> "yield_historical.out.nineties")

      val result90s = mongoDB.command(cmd90s)

      log.info("M/R result90s: %s", result90s)


      result90s.getAs[Double]("ok") must beSome(1.0)
      result90s.getAs[String]("result") must beSome("yield_historical.out.nineties")

      Some(mongoDB(result90s.as[String]("result")).size) must beEqualTo(result90s.expand[Int]("counts.output"))

      val cmd00s = MongoDBObject(
        "mapreduce" -> "yield_historical.in",
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "verbose" -> true,
        "query" -> ("_id" $gt new Date(99, 12, 31)),
        "out" -> "yield_historical.out.aughts")

      val result00s = mongoDB.command(cmd00s)

      log.info("M/R result00s: %s", result00s)


      result00s.getAs[Double]("ok") must beSome(1.0)
      result00s.getAs[String]("result") must beSome("yield_historical.out.aughts")

      Some(mongoDB(result00s.as[String]("result")).size) must beEqualTo(result00s.expand[Int]("counts.output"))

      "Reduce the 90s and 00s into a single output collection" in {
        "Querying against the raw data " in {
          cmd00s += "out" -> MongoDBObject("reduce" -> "yield_historical.out.all")
          //cmd00s += "finalize" -> finalizeJS

          log.info("cmd 00s: %s", cmd00s)

          cmd90s += "out" -> MongoDBObject("reduce" -> "yield_historical.out.all")
          //cmd90s += "finalize" -> finalizeJS

          log.info("cmd 90s: %s", cmd90s)

          var result = mongoDB.command(cmd90s)
          log.info("First pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.out.all")

          result = mongoDB.command(cmd00s)
          log.info("Second pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.out.all")

          result.expand[Int]("counts.output") must beSome(21)

          val mongo = mongoDB(result.as[String]("result"))
          Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))
        }

        "Querying against the intermediary collections" in {
          cmd00s += "out" -> MongoDBObject("reduce" -> "yield_historical.out.all")
          cmd00s -= "query"
          cmd00s += "mapreduce" -> "yield_historical.out.aughts"
          //cmd00s += "finalize" -> finalizeJS

          log.info("cmd 00s: %s", cmd00s)

          cmd90s += "out" -> MongoDBObject("reduce" -> "yield_historical.out.all")
          cmd90s -= "query"
          cmd90s += "mapreduce" -> "yield_historical.out.nineties"
          //cmd90s += "finalize" -> finalizeJS

          log.info("cmd 90s: %s", cmd90s)

          var result = mongoDB.command(cmd90s)
          log.info("First pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.out.all")

          result = mongoDB.command(cmd00s)
          log.info("Second pass result: %s", result)

          result.getAs[Double]("ok") must beSome(1.0)
          result.getAs[String]("result") must beSome("yield_historical.out.all")

          result.expand[Int]("counts.output") must beSome(21)

          val mongo = mongoDB(result.as[String]("result"))
          Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))

        }
      }
    }

  }

  trait testData extends Scope {
    mongoDB("yield_historical.all").drop
    mongoDB("yield_historical.merged").drop
    mongoDB("yield_historical.merged_fresh").drop
    mongoDB("yield_historical.nineties").drop
    mongoDB("yield_historical.aughts").drop
    mongoDB("yield_historical.out.all").drop
    mongoDB("yield_historical.out.merged").drop
    mongoDB("yield_historical.out.nineties").drop
    mongoDB("yield_historical.out.aughts").drop

    Seq("mongoimport", "-d", "casbahIntegration", "-c", "yield_historical.in", "--drop", "./casbah-core/src/test/resources/yield_historical_in.json").!!

    // Verify the treasury data is loaded or skip the test for now
    mongoDB("yield_historical.in").size must beGreaterThan(0)
  }

}

