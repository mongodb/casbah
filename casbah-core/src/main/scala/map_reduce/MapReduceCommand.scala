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

import scalaj.collection.Imports._

class MapReduceError(msg: String) extends MongoException("MongoDB Map/Reduce Error: " + msg)

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
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
object MapReduceCommand {
  def apply(input: String, mapFunction: JSFunction,
    reduceFunction: JSFunction,
    output: MapReduceOutputTarget,
    query: Option[DBObject] = None,
    sort: Option[DBObject] = None,
    limit: Option[Int] = None,
    finalizeFunction: Option[JSFunction] = None,
    jsScope: Option[String] = None,
    verbose: Boolean = true) = {
    val mrc = new MapReduceCommand()
    mrc.input = input
    mrc.mapFunction = mapFunction
    mrc.reduceFunction = reduceFunction
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
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
protected[mongodb] class MapReduceCommand {
  var input: String = ""
  var mapFunction: JSFunction = ""
  var reduceFunction: JSFunction = ""
  var verbose = true // provides statistics on job execution
  var output: MapReduceOutputTarget = MapReduceInlineOutput
  var query: Option[DBObject] = None
  var sort: Option[DBObject] = None
  var limit: Option[Int] = None
  var finalizeFunction: Option[JSFunction] = None
  var jsScope: Option[String] = None

  def asDBObject = toDBObj

  def toDBObj = {
    val dataObj = MongoDBObject.newBuilder
    input match {
      case "" => throw new MapReduceError("input must be defined.")
      case null => throw new MapReduceError("input must be defined.")
      case other => dataObj += "mapreduce" -> input
    }

    mapFunction match {
      case "" => throw new MapReduceError("mapFunction must be defined.")
      case null => throw new MapReduceError("mapFunction must be defined.")
      case other => dataObj += "map" -> mapFunction.toString
    }

    reduceFunction match {
      case "" => throw new MapReduceError("reduceFunction must be defined.")
      case null => throw new MapReduceError("reduceFunction must be defined.")
      case other => dataObj += "reduce" -> reduceFunction.toString
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

}
