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

package com.novus.mongodb
package map_reduce

import Implicits._

import com.mongodb._
import scalaj.collection.Imports._
import com.novus.util.Logging
import com.novus.mongodb._


/**
 * Wrapper for MongoDB MapReduceResults, implementing iterator to allow direct iterator over the result set.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 *
 * @param resultObj a DBObject directly conforming to the mapReduce result spec as defined in the MongoDB Docs.
 * 
 */
class MapReduceResult(resultObj: DBObject)(implicit db: ScalaMongoDB) extends Iterator[DBObject] with Logging {
  log.debug("Map Reduce Result: %s", resultObj)
  // Convert the object to a map to have a quicker, saner shred...
  val FAIL = "#FAIL"
  val result = if (resultObj.containsField("result"))  {
                 resultObj.get("result").toString
               } else  {
                 log.warning("Map/Reduce Result field is empty. Setting an error state explicitly.")
                 FAIL
               }// Unless you've defined a table named #FAIL this should give you empty results back.
                
/*  val result = resultMap.get("result") match {
    case Some(v) => v
    case None => throw new IllegalArgumentException("Cannot find field 'result' in Map/Reduce Results.")
  }*/
  val resultHandle = db(result.toString)
  
  private val resultCursor = resultHandle.find

  def next(): DBObject = resultCursor.next

  def hasNext: Boolean = resultCursor.hasNext

  def size = resultHandle.count

  private val counts = resultObj.get("counts").asInstanceOf[DBObject]
  // Number of objects scanned
  val input_count: Int = if (counts != null) counts.get("input").toString.toInt else 0 //, throw new IllegalArgumentException("Cannot find field 'counts.input' in Map/Reduce Results."))
  // Number of times 'emit' was called
  val emit_count: Int = if (counts != null) counts.get("emit").toString.toInt else 0//, throw new IllegalArgumentException("Cannot find field 'counts.emit' in Map/Reduce Results."))
  // Number of items in output collection
  val output_count: Int = if (counts != null) counts.get("output").toString.toInt else 0//throw new IllegalArgumentException("Cannot find field 'counts.output' in Map/Reduce Results."))

  val timeMillis = if (counts != null) resultObj.get("timeMillis").toString.toInt else -1 //throw new IllegalArgumentException("Cannot find field 'timeMillis' in Map/Reduce Results."))

  val ok = if (resultObj.get("ok") == 1) true else false

  if (!ok) log.warning("Job result is NOT OK.")


  val err = resultObj.get("errmsg")

  val success = err match {
    case null => {
      log.debug("Map/ Reduce Success.")
      true
    }
    case msg => {
      log.error("Map/Reduce failed: %s", msg)
      false
    }
  }

  override def toString = {
    if (success) {
      "{MapReduceResult Proxying Result [%s] Handle [%s]}".format(result, resultHandle.toString)
    }
    else {
      "{MapReduceResult - Failure with Error [%s]".format(err.toString)
    }
  }
}
