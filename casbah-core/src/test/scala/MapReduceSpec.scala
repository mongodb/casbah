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
class MapReduceSpec extends CasbahDBTestSpecification {

  "Casbah's Map/Reduce Engine" should {

    "Handle error conditions such as non-existent collections gracefully" in {
      collection.dropCollection()

      val keySet = List("Foo", "bar", "Baz").flatMap(x => "'%s': this.%s, ".format(x, x)).mkString
      val map = "function () { emit({%s}, 1); }".format(keySet)
      val reduce = "function(k, v) { return 1; }"

      val result = collection.mapReduce(map, reduce, MapReduceInlineOutput)
      result must beEmpty
    }
  }

  val mapJS = """
      function m() {
          var key = typeof(this._id) == "number" ? this._id : this._id.getYear();
          emit(key, { count: 1, sum: this.bc10Year })
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

  "MongoDB 1.7+ Map/Reduce functionality" should {

    "Produce results in a named collection for all data" in new testData {
      val result = collection.mapReduce(
        mapJS,
        reduceJS,
        "yield_historical.all",
        finalizeFunction = Some(finalizeJS)
      )

      result.isError must beFalse
      result.size must beEqualTo(result.raw.expand[Int]("counts.output").getOrElse(-1))
    }

    "Produce results in a named collection for inline data" in new testData {
      val result = collection.mapReduce(
        mapJS,
        reduceJS,
        MapReduceInlineOutput,
        finalizeFunction = Some(finalizeJS),
        verbose = true
      )

      result.isError must beFalse
      result.raw.getAs[String]("result") must beNone
      result.size must beGreaterThan(0)
      result.size must beEqualTo(result.raw.expand[Int]("counts.output").getOrElse(-1))

      val item = result.next()
      item must beDBObject
      item must beEqualTo(MongoDBObject("_id" -> 90.0, "value" -> 8.552400000000002))
    }

    "Produce results with variable from jsScope" in new testData {
      val mapJSScoped = """
        function m() {
          var key = typeof(this._id) == "number" ? this._id : this._id.getYear()
          emit(key, { count: 1, sum: this.bc10Year * scopedBoost })
        }
                        """

      val result = collection.mapReduce(
        mapJSScoped,
        reduceJS,
        MapReduceInlineOutput,
        finalizeFunction = Some(finalizeJS),
        jsScope = Some(MongoDBObject("scopedBoost" -> 2)),
        verbose = true
      )

      result.isError must beFalse
      result.raw.getAs[String]("result") must beNone
      result.size must beGreaterThan(0)
      result.size must beEqualTo(result.raw.expand[Int]("counts.output").getOrElse(-1))

      val item = result.next()
      item must beDBObject
      item must beEqualTo(MongoDBObject("_id" -> 90.0, "value" -> 17.104800000000004))
    }

    val cmd90s = MapReduceCommand(
      collection.name,
      mapJS,
      reduceJS,
      "yield_historical.nineties",
      Some("_id" $lt new Date(100, 1, 1)),
      finalizeFunction = Some(finalizeJS),
      verbose = true
    )

    val cmd00s = MapReduceCommand(
      collection.name,
      mapJS,
      reduceJS,
      "yield_historical.aughts",
      Some("_id" $gt new Date(99, 12, 31)),
      finalizeFunction = Some(finalizeJS),
      verbose = true
    )

    "Produce results for merged output" in new testData {

      val result90s = collection.mapReduce(cmd90s)

      log.info("M/R result90s: %s", result90s)

      result90s.isError must beFalse
      result90s.raw.getAs[String]("result") must beSome("yield_historical.nineties")
      result90s.size must beGreaterThan(0)
      result90s.size must beEqualTo(result90s.raw.expand[Int]("counts.output").getOrElse(-1))

      val result00s = collection.mapReduce(cmd00s)

      result00s.isError must beFalse
      result00s.raw.getAs[String]("result") must beSome("yield_historical.aughts")
      result00s.size must beGreaterThan(0)
      result00s.size must beEqualTo(result00s.raw.expand[Int]("counts.output").getOrElse(-1))
    }

    "Merge the 90s and 00s into a single output collection" in {
      "reading the earlier output collections" in {

        val cmd90sMerged = cmd90s.copy(
          query = None,
          input = "yield_historical.nineties",
          output = MapReduceMergeOutput("yield_historical.merged")
        )

        val result90s = collection.mapReduce(cmd90sMerged)
        result90s.isError must beFalse
        result90s.raw.getAs[String]("result") must beSome("yield_historical.merged")

        val cmd00sMerged = cmd00s.copy(
          query = None,
          input = "yield_historical.aughts",
          output = MapReduceMergeOutput("yield_historical.merged")
        )

        val result00s = collection.mapReduce(cmd00sMerged)
        result00s.isError must beFalse
        result00s.raw.getAs[String]("result") must beSome("yield_historical.merged")
        result00s.outputCount must beEqualTo(21)
        result00s.size must beEqualTo(result00s.outputCount)
      }
      "Using a fresh query run" in {

        val cmd90sMerged = cmd90s.copy(
          query = None,
          input = "yield_historical.nineties",
          output = MapReduceMergeOutput("yield_historical.merged_fresh")
        )

        val result90s = collection.mapReduce(cmd90sMerged)
        result90s.isError must beFalse
        result90s.raw.getAs[String]("result") must beSome("yield_historical.merged_fresh")

        val cmd00sMerged = cmd00s.copy(
          query = None,
          input = "yield_historical.aughts",
          output = MapReduceMergeOutput("yield_historical.merged_fresh")
        )

        val result00s = collection.mapReduce(cmd00sMerged)
        result00s.isError must beFalse
        result00s.raw.getAs[String]("result") must beSome("yield_historical.merged_fresh")

        result00s.outputCount must beEqualTo(21)
        result00s.size must beEqualTo(result00s.outputCount)
      }
    }
  }

  "Produce results for reduced output (multiples into a single final collection)" in new testData {

    import java.util.Date

    val cmd90s = MapReduceCommand(
      collection.name,
      mapJS,
      reduceJS,
      "yield_historical.nineties",
      Some("_id" $lt new Date(100, 1, 1)),
      finalizeFunction = Some(finalizeJS),
      verbose = true
    )

    val result90s = collection.mapReduce(cmd90s)
    result90s must not beNull

    result90s.isError must beFalse
    result90s.raw.getAs[String]("result") must beSome("yield_historical.nineties")
    result90s.size must beGreaterThan(0)
    result90s.size must beEqualTo(result90s.raw.expand[Int]("counts.output").getOrElse(-1))

    val cmd00s = MapReduceCommand(
      collection.name,
      mapJS,
      reduceJS,
      "yield_historical.aughts",
      Some("_id" $gt new Date(99, 12, 31)),
      finalizeFunction = Some(finalizeJS),
      verbose = true
    )

    val result00s = collection.mapReduce(cmd00s)

    log.info("M/R result00s: %s", result00s)

    result00s.isError must beFalse
    result00s.raw.getAs[String]("result") must beSome("yield_historical.aughts")
    result00s.size must beGreaterThan(0)
    result00s.size must beEqualTo(result00s.raw.expand[Int]("counts.output").getOrElse(-1))

    "Reduce the 90s and 00s into a single output collection" in {
      "Querying against the raw data " in {

        val cmd90sReduced = cmd90s.copy(
          query = None,
          input = "yield_historical.nineties",
          output = MapReduceReduceOutput("yield_historical.reduced")
        )
        val result90s = collection.mapReduce(cmd90sReduced)
        result90s.isError must beFalse
        result90s.raw.getAs[String]("result") must beSome("yield_historical.reduced")

        val cmd00sReduced = cmd00s.copy(
          query = None,
          input = "yield_historical.aughts",
          output = MapReduceReduceOutput("yield_historical.reduced")
        )

        val result00s = collection.mapReduce(cmd00sReduced)
        result00s.isError must beFalse
        result00s.raw.getAs[String]("result") must beSome("yield_historical.reduced")

        result00s.outputCount must beEqualTo(21)
        result00s.size must beEqualTo(result00s.outputCount)
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

