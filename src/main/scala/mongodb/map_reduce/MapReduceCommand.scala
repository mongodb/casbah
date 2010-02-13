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

package com.novus.mongodb.map_reduce

import com.mongodb._
import org.scala_tools.javautils.Imports._
import com.novus.util.Logging
import com.novus.mongodb._
import Implicits._


/**
 * Wrapper Object to provide apply methods for the MapReduceCommand class.
 *
 * @see http://www.mongodb.org/display/DOCS/MapReduce
 * 
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
object MapReduceCommand {
  def apply(collection: String,
            mapFunction: JSFunction,
            reduceFunction: JSFunction,
            outputCollection: Option[String],
            query: Option[DBObject],
            sort: Option[DBObject],
            finalizeFunction: Option[JSFunction],
            jsScope: Option[String]) = {
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
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
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

  def toDBObj = {
    val dataObj = BasicDBObjectBuilder.start
    collection match {
      case "" => throw new NotDefinedError("collection must be defined.")
      case null => throw new NotDefinedError("collection must be defined.")
      case other => dataObj.add("mapreduce", collection)
    }

    mapFunction match {
      case "" => throw new NotDefinedError("mapFunction must be defined.")
      case null => throw new NotDefinedError("mapFunction must be defined.")
      case other => dataObj.add("map", mapFunction.toString)
    }

    reduceFunction match {
      case "" => throw new NotDefinedError("reduceFunction must be defined.")
      case null => throw new NotDefinedError("reduceFunction must be defined.")
      case other => dataObj.add("reduce", reduceFunction.toString)
    }

    dataObj.add("keepTemp", keepTemp)
    dataObj.add("verbose", verbose)

    outputCollection match {
      case Some(out) => dataObj.add("out", out)
      case None => {}
    }

    query match {
      case Some(q) => dataObj.add("query", q)
      case None => {}
    }

    sort match {
      case Some(s) => dataObj.add("sort", s)
      case None => {}
    }

    finalizeFunction match {
      case Some(fF) => dataObj.add("finalize", fF.toString)
      case None => {}
    }

    jsScope match {
      case Some(s) => dataObj.add("scope", s)
      case None => {}
    }


    dataObj.get

  }

}