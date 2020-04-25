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
import java.util.Date

@SuppressWarnings(Array("deprecation"))
class RawMapReduceSpec extends CasbahDBTestSpecification {

  val mapJS = """
      function m() {
          emit( typeof(this._id) == "number" ? this._id : this._id.getYear(), { count: 1, sum: this.bc10Year })
      }"""

  val reduceJS = """
      function r( year, values ) {
          var n = { count: 0,  sum: 0 }
          for ( var i = 0; i < values.length; i++ ){
              n.sum += values[i].sum;
              n.count += values[i].count;
          }

          return n;
      }"""

  val finalizeJS = """
      function f( year, value ){
          value.avg = value.sum / value.count;
          return value.avg;
      }"""

  "MongoDB 1.7+ Map/Reduce functionality" should {

    "Produce results in a named collection for all data" in new testData {
      val cmd = MongoDBObject(
        "mapreduce" -> collection.name,
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "finalize" -> finalizeJS,
        "verbose" -> true,
        "out" -> "yield_historical.out.all"
      )

      val result = database.command(cmd)

      log.info("M/R Result: %s", result)

      result.getAs[Double]("ok") must beSome[Double](1.0)
      result.getAs[String]("result") must beSome[String]("yield_historical.out.all")

      val mongo = database(result.as[String]("result"))
      Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))
    }

    "Produce results in a named collection for inline data" in new testData {
      val cmd = MongoDBObject(
        "mapreduce" -> collection.name,
        "map" -> mapJS,
        "reduce" -> reduceJS,
        "finalize" -> finalizeJS,
        "verbose" -> true,
        "out" -> MongoDBObject("inline" -> true)
      )

      val result = database.command(cmd)

      log.info("M/R Result: %s", result)

      result.getAs[Double]("ok") must beSome[Double](1.0)
      result.getAs[String]("result") must beNone
      result.getAs[MongoDBList]("results") must beSome[MongoDBList]

      val mongo = result.as[MongoDBList]("results")
      Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))
      mongo(0) must beDBObject
      mongo(0) must beEqualTo(MongoDBObject("_id" -> 90.0, "value" -> 8.552400000000002))
    }
  }

  "Merged output" should {

    "load test data first" in new testData {
      database("yield_historical.out.merged").size must beEqualTo(0)
      database("yield_historical.out.aughts").size must beEqualTo(0)
      database("yield_historical.out.nineties").size must beEqualTo(0)
    }

    val cmd90s = MongoDBObject(
      "mapreduce" -> collection.name,
      "map" -> mapJS,
      "reduce" -> reduceJS,
      "finalize" -> finalizeJS,
      "verbose" -> true,
      "query" -> ("_id" $lt new Date(100, 1, 1)),
      "out" -> "yield_historical.out.nineties"
    )

    val cmd00s = MongoDBObject(
      "mapreduce" -> collection.name,
      "map" -> mapJS,
      "reduce" -> reduceJS,
      "finalize" -> finalizeJS,
      "verbose" -> true,
      "query" -> ("_id" $gt new Date(99, 12, 31)),
      "out" -> "yield_historical.out.aughts"
    )

    "Produce results for merged output" in {

      val result90s = database.command(cmd90s)
      result90s.getAs[Double]("ok") must beSome[Double](1.0)
      result90s.getAs[String]("result") must beSome[String]("yield_historical.out.nineties")

      Some(database(result90s.as[String]("result")).size) must beEqualTo(result90s.expand[Int]("counts.output"))

      val result00s = database.command(cmd00s)
      result00s.getAs[Double]("ok") must beSome[Double](1.0)
      result00s.getAs[String]("result") must beSome[String]("yield_historical.out.aughts")
      Some(database(result00s.as[String]("result")).size) must beEqualTo(result00s.expand[Int]("counts.output"))
    }

    "Merge the 90s and 00s into a single output collection reading the earlier output collections" in {

      "Reduce the 90s and 00s into a single output collection" in {
        cmd00s -= "query"
        cmd00s += "mapreduce" -> "yield_historical.out.aughts"
        cmd00s += "out" -> MongoDBObject("merge" -> "yield_historical.out.merged")

        cmd90s -= "query"
        cmd90s += "mapreduce" -> "yield_historical.out.nineties"
        cmd90s += "out" -> MongoDBObject("merge" -> "yield_historical.out.merged")

        val result90s = database.command(cmd90s)

        log.info("First pass result: %s", result90s)

        result90s.getAs[Double]("ok") must beSome[Double](1.0)
        result90s.getAs[String]("result") must beSome[String]("yield_historical.out.merged")

        val result00s = database.command(cmd00s)

        result00s.getAs[Double]("ok") must beSome[Double](1.0)
        result00s.getAs[String]("result") must beSome[String]("yield_historical.out.merged")

        result00s.expand[Int]("counts.output") must beSome(21)

        val mongo = database(result00s.as[String]("result"))
        Some(mongo.size) must beEqualTo(result00s.expand[Int]("counts.output"))
      }

      "Using a fresh query run" in {
        cmd00s += "out" -> MongoDBObject("merge" -> "yield_historical.out.merged_fresh")
        cmd90s += "out" -> MongoDBObject("merge" -> "yield_historical.out.merged_fresh")

        val result90s = database.command(cmd90s)
        log.info("First pass result: %s", result90s)

        result90s.getAs[Double]("ok") must beSome[Double](1.0)
        result90s.getAs[String]("result") must beSome[String]("yield_historical.out.merged_fresh")

        val result00s = database.command(cmd00s)
        log.info("second pass result: %s", result00s)

        result00s.getAs[Double]("ok") must beSome[Double](1.0)
        result00s.getAs[String]("result") must beSome[String]("yield_historical.out.merged_fresh")

        result00s.expand[Int]("counts.output") must beSome(21)

        val mongo = database(result00s.as[String]("result"))
        Some(mongo.size) must beEqualTo(result00s.expand[Int]("counts.output"))
      }
    }
  }

  "Reduced output (multiples into a single final collection)" should {

    "load test data first" in new testData {
      database("yield_historical.out.merged").size must beEqualTo(0)
      database("yield_historical.out.aughts").size must beEqualTo(0)
      database("yield_historical.out.nineties").size must beEqualTo(0)
    }

    val cmd90s = MongoDBObject(
      "mapreduce" -> collection.name,
      "map" -> mapJS,
      "reduce" -> reduceJS,
      "verbose" -> true,
      "query" -> ("_id" $lt new Date(100, 1, 1)),
      "out" -> "yield_historical.out.nineties"
    )

    val cmd00s = MongoDBObject(
      "mapreduce" -> collection.name,
      "map" -> mapJS,
      "reduce" -> reduceJS,
      "verbose" -> true,
      "query" -> ("_id" $gt new Date(99, 12, 31)),
      "out" -> "yield_historical.out.aughts"
    )

    "Produce results for nineties and aughties output" in {

      val result90s = database.command(cmd90s)
      result90s.getAs[Double]("ok") must beSome[Double](1.0)
      result90s.getAs[String]("result") must beSome[String]("yield_historical.out.nineties")
      Some(database(result90s.as[String]("result")).size) must beEqualTo(result90s.expand[Int]("counts.output"))

      val result00s = database.command(cmd00s)
      result00s.getAs[Double]("ok") must beSome[Double](1.0)
      result00s.getAs[String]("result") must beSome[String]("yield_historical.out.aughts")
      Some(database(result00s.as[String]("result")).size) must beEqualTo(result00s.expand[Int]("counts.output"))

    }

    "Reduce the 90s and 00s into a single output collection" in {
      "Querying against the raw data " in {
        cmd00s += "out" -> MongoDBObject("reduce" -> "yield_historical.out.all")
        //cmd00s += "finalize" -> finalizeJS

        log.info("cmd 00s: %s", cmd00s)

        cmd90s += "out" -> MongoDBObject("reduce" -> "yield_historical.out.all")
        //cmd90s += "finalize" -> finalizeJS

        log.info("cmd 90s: %s", cmd90s)

        var result = database.command(cmd90s)
        log.info("First pass result: %s", result)

        result.getAs[Double]("ok") must beSome[Double](1.0)
        result.getAs[String]("result") must beSome[String]("yield_historical.out.all")

        result = database.command(cmd00s)
        log.info("Second pass result: %s", result)

        result.getAs[Double]("ok") must beSome[Double](1.0)
        result.getAs[String]("result") must beSome[String]("yield_historical.out.all")

        result.expand[Int]("counts.output") must beSome(21)

        val mongo = database(result.as[String]("result"))
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

        var result = database.command(cmd90s)
        log.info("First pass result: %s", result)

        result.getAs[Double]("ok") must beSome[Double](1.0)
        result.getAs[String]("result") must beSome[String]("yield_historical.out.all")

        result = database.command(cmd00s)
        log.info("Second pass result: %s", result)

        result.getAs[Double]("ok") must beSome[Double](1.0)
        result.getAs[String]("result") must beSome[String]("yield_historical.out.all")

        result.expand[Int]("counts.output") must beSome[Int](21)

        val mongo = database(result.as[String]("result"))
        Some(mongo.size) must beEqualTo(result.expand[Int]("counts.output"))

      }
    }
  }

  trait testData extends Scope {
    val jsonFile = "./casbah-core/src/test/resources/yield_historical_in.json"

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
