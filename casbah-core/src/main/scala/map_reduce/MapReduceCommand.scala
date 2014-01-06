/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
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

package com.mongodb.casbah
package map_reduce

import scala.concurrent.duration.Duration

import com.mongodb.casbah.Imports._

class MapReduceException(msg: String) extends MongoException("MongoDB Map/Reduce Error: " + msg)

trait MapReduceOutputTarget

case class MapReduceStandardOutput(collection: String) extends MapReduceOutputTarget

case class MapReduceMergeOutput(collection: String) extends MapReduceOutputTarget

case class MapReduceReduceOutput(collection: String) extends MapReduceOutputTarget

case object MapReduceInlineOutput extends MapReduceOutputTarget

/**
 * Case class for invoking MongoDB mapReduces.
 *
 * This wrapper class is used in it's place, and passed directly to a db.runCommand call.
 *
 * @see <a href="http://www.mongodb.org/display/DOCS/MapReduce">The MongoDB Map/Reduce Documentation</a>
 *
 * @param input             the collection name to run the map reduce on
 * @param map               the map function (JSFunction is just a type alias for String)
 * @param reduce            the reduce function (JSFunction is just a type alias for String)
 * @param output            (optional) the location of the result of the map-reduce operation, defaults to inline.
 *                          You can output to a collection, output to a collection with an action, or output inline.
 * @param query             (optional) the selection criteria for the documents input to the map function.
 * @param sort              (optional) the input documents, useful for optimization.
 * @param limit             (optional) the maximum number of documents to return from the collection before map reduce
 * @param finalizeFunction  (optional) the finalize function (JSFunction is just a type alias for String)
 * @param jsScope           (optional) global variables that are accessible in the map, reduce and finalize functions
 * @param verbose           (optional) include the timing information in the result information
 * @param maxTime           (optional) the maximum duration that the server will allow this operation to execute before killing it
 */
case class MapReduceCommand protected[mongodb](input: String = "", map: JSFunction = "", reduce: JSFunction = "",
                                               output: MapReduceOutputTarget = MapReduceInlineOutput,
                                               query: Option[DBObject] = None, sort: Option[DBObject] = None,
                                               limit: Option[Int] = None, finalizeFunction: Option[JSFunction] = None,
                                               jsScope: Option[DBObject] = None, verbose: Boolean = false,
                                               maxTime: Option[Duration] = None) {

  // scalastyle:off null cyclomatic.complexity method.length

  def toDBObject: DBObject = {
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

    maxTime match {
      case Some(mD) => dataObj += "maxTimeMS" -> mD.toMillis
      case None => {}
    }

    dataObj.result()

  }

  override def toString: String = toDBObject.toString
}
