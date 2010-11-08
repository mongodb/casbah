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


class MapReduceError(msg: String) extends Error("MongoDB Map/Reduce Error: " + msg)

/**
 * Wrapper Object to provide apply methods for the MapReduceCommand class.
 *
 * @see <a href="http://www.mongodb.org/display/DOCS/MapReduce">The MongoDB Map/Reduce Documentation</a>
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
object MapReduceCommand {
  def apply(collection: String,
            mapFunction: JSFunction,
            reduceFunction: JSFunction,
            outputCollection: Option[String] = None,
            query: Option[DBObject] = None,
            sort: Option[DBObject] = None,
            finalizeFunction: Option[JSFunction] = None,
            jsScope: Option[String] = None) = {
    val mrc = new MapReduceCommand()
    mrc.collection = collection
    mrc.mapFunction = mapFunction
    mrc.reduceFunction = reduceFunction
    mrc.outputCollection = outputCollection
    mrc.query = query
    mrc.sort = sort
    mrc.finalizeFunction = finalizeFunction
    mrc.jsScope = jsScope
    mrc
  }

  def apply(collection: String,
            mapFunction: JSFunction,
            reduceFunction: JSFunction,
            outputCollection: String) = {
      val mrc = new MapReduceCommand()
      mrc.collection = collection
      mrc.mapFunction = mapFunction
      mrc.reduceFunction = reduceFunction
      mrc.outputCollection = Some(outputCollection)
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
class MapReduceCommand {
  var collection: String = ""
  var mapFunction: JSFunction = ""
  var reduceFunction: JSFunction = ""
  var keepTemp = false // moot if outputCollection is specific
  var verbose = true // provides statistics on job execution
  var outputCollection: Option[String] = None
  var query: Option[DBObject] = None
  var sort: Option[DBObject] = None
  var finalizeFunction: Option[JSFunction] = None
  var jsScope: Option[String] = None

  def asDBObject = toDBObj 

  def toDBObj = {
    val dataObj = MongoDBObject.newBuilder
    collection match {
      case "" => throw new MapReduceError("collection must be defined.")
      case null => throw new MapReduceError("collection must be defined.")
      case other => dataObj += "mapreduce" -> collection
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

    dataObj += "keepTemp" -> keepTemp
    dataObj += "verbose" -> verbose

    outputCollection match {
      case Some(out) => dataObj += "out" -> out
      case None => {}
    }

    query match {
      case Some(q) => dataObj += "query" -> q
      case None => {}
    }

    sort match {
      case Some(s) => dataObj += "sort" -> s
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
