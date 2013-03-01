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

import scala.collection.JavaConverters._

class MapReduceException(msg: String) extends MongoException("MongoDB Map/Reduce Error: " + msg)

trait MapReduceOutputTarget

case class MapReduceStandardOutput(collection: String) extends MapReduceOutputTarget
case class MapReduceMergeOutput(collection: String) extends MapReduceOutputTarget
case class MapReduceReduceOutput(collection: String) extends MapReduceOutputTarget
case object MapReduceInlineOutput extends MapReduceOutputTarget

/**
 * Wrapper Object to provide apply methods for the MapReduceCommand class.
 *
 * @see <a href="http://www.mongodb.org/display/DOCS/MapReduce">The MongoDB Map/Reduce Documentation</a>
 *
 */
object MapReduceCommand {
  def apply(input: String, map: JSFunction,
    reduce: JSFunction,
    output: MapReduceOutputTarget,
    query: Option[DBObject] = None,
    sort: Option[DBObject] = None,
    limit: Option[Int] = None,
    finalizeFunction: Option[JSFunction] = None,
    jsScope: Option[DBObject] = None,
    verbose: Boolean = false) = {
    val mrc = new MapReduceCommand()
    mrc.input = input
    mrc.map = map
    mrc.reduce = reduce
    mrc.output = output
    mrc.query = query
    mrc.sort = sort
    mrc.limit = limit
    mrc.finalizeFunction = finalizeFunction
    mrc.jsScope = jsScope
    mrc.verbose = verbose
    mrc
  }
}

/**
 * Wrapper class for invoking MongoDB mapReduces.
 *
 * The Java driver doesn't provide support for many of the possible options in the latest
 * versions of MongoDB, so this wrapper class is used in it's place, and passed directly to
 * a db.runCommand call.
 *
 * @todo Integrate support for Lift's JavaScript DSL
 *
 * @see http://www.mongodb.org/display/DOCS/MapReduce
 *
 */
class MapReduceCommand protected[mongodb] () {
  var input: String = ""
  var map: JSFunction = ""
  var reduce: JSFunction = ""
  var verbose = false // if true, provides statistics on job execution
  var output: MapReduceOutputTarget = MapReduceInlineOutput
  var query: Option[DBObject] = None
  var sort: Option[DBObject] = None
  var limit: Option[Int] = None
  var finalizeFunction: Option[JSFunction] = None
  var jsScope: Option[DBObject] = None

  def toDBObject = {
    val dataObj = MongoDBObject.newBuilder
    input match {
      case "" => throw new MapReduceException("input must be defined.")
      case null => throw new MapReduceException("input must be defined.")
      case other => dataObj += "mapreduce" -> input
    }

    map match {
      case "" => throw new MapReduceException("map must be defined.")
      case null => throw new MapReduceException("map must be defined.")
      case other => dataObj += "map" -> map.toString
    }

    reduce match {
      case "" => throw new MapReduceException("reduce must be defined.")
      case null => throw new MapReduceException("reduce must be defined.")
      case other => dataObj += "reduce" -> reduce.toString
    }

    dataObj += "verbose" -> verbose

    dataObj += "out" -> (output match {
      case MapReduceStandardOutput(coll: String) => coll
      case MapReduceMergeOutput(coll: String) => MongoDBObject("merge" -> coll)
      case MapReduceReduceOutput(coll: String) => MongoDBObject("reduce" -> coll)
      case MapReduceInlineOutput => MongoDBObject("inline" -> true)
      case other => throw new IllegalArgumentException("Invalid Output Type '%s'".format(other))
    })

    query match {
      case Some(q) => dataObj += "query" -> q
      case None => {}
    }

    sort match {
      case Some(s) => dataObj += "sort" -> s
      case None => {}
    }

    limit match {
      case Some(i) => dataObj += "limit" -> i
      case None => {}
    }

    finalizeFunction match {
      case Some(fF) => dataObj += "finalize" -> fF.toString
      case None => {}
    }

    jsScope match {
      case Some(s) => dataObj += "scope" -> s
      case None => {}
    }

    dataObj.result

  }

  override def toString = toDBObject.toString
}
