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

object MapReduceResult extends Logging {

  protected[mongodb] def apply(resultObj: DBObject)(implicit db: MongoDB): MapReduceResult =
    if (resultObj.get("ok") == 1) {
      if (resultObj containsField "results")
        new MapReduceInlineResult(resultObj)
      else if (resultObj containsField "result")
        new MapReduceCollectionBasedResult(resultObj)
      else
        throw new MapReduceException("Invalid Response; no 'results' or 'result' field found, but 'ok' is 1. Result Object Error: '%s'".format(resultObj.getAs[String]("err")))
    } else new MapReduceError(resultObj)
}

/**
 * Wrapper for MongoDB MapReduceResults, implementing iterator to allow direct iterator over the result set.
 *
 *
 * @param raw DBObject directly conforming to the mapReduce result spec as defined in the MongoDB Docs.
 *
 */
trait MapReduceResult extends Iterator[DBObject] with Logging {

  /**
   * The raw output Object from the MongoDB MapReduce call
   */
  def raw: DBObject

  val isError = false
  lazy val ok = !isError // This may be deprecated in a future release
  val errorMessage: Option[String] = None
  lazy val err = errorMessage

  def cursor: Iterator[DBObject]

  def next(): DBObject = cursor.next()

  def hasNext: Boolean = cursor.hasNext

  /** Number of objects scanned */
  lazy val inputCount: Int = raw.expand[Int]("counts.input") getOrElse -1
  /** Number of times 'emit' was called */
  lazy val emitCount: Int = raw.expand[Int]("counts.emit") getOrElse -1
  /* Number of items in output collection */
  lazy val outputCount: Int = raw.expand[Int]("counts.output") getOrElse -1

  /** Number of milliseconds taken to execute */
  lazy val timeMillis: Int = raw.getAs[Int]("timeMillis") getOrElse -1

  /** Amount of time spent in the Map execution */
  lazy val mapTime: Option[Long] = raw.expand[Long]("timing.mapTime")
  /** Amount of time spent Emitting */
  lazy val emitLoopTime: Option[Int] = raw.expand[Int]("timing.emitLoop")
  /** Total time spent */
  lazy val totalTime: Option[Int] = raw.expand[Int]("timing.total")

}

class MapReduceCollectionBasedResult protected[mongodb] (override val raw: DBObject)(implicit db: MongoDB) extends MapReduceResult {
  override lazy val cursor: Iterator[DBObject] = db(raw.as[String]("result")).find

  override def size = cursor.size
  override def toString() = "{MapReduceResult Proxying Result stored in collection [%s] against raw response [%s]}".format(raw.as[String]("result"), raw.toString)
}

class MapReduceInlineResult protected[mongodb] (override val raw: DBObject)(implicit db: MongoDB) extends MapReduceCollectionBasedResult(raw) {
  private val results = raw.as[MongoDBList]("results")
  override lazy val cursor = new Iterator[DBObject] {
    private val iter = results.iterator
    def next() = iter.next.asInstanceOf[DBObject]
    def hasNext = iter.hasNext
  }

  override def size = results.size
  override def toString = "{MapReduceResult Proxying Result returned Inline against raw response [%s]}".format(raw.toString)
}

class MapReduceError protected[mongodb] (override val raw: DBObject)(implicit db: MongoDB) extends MapReduceResult {
  val cursor = Iterator.empty

  override val isError = true

  override val errorMessage: Option[String] = raw.getAs[String]("err")

  override def toString = "{MapReduceError '%s'}".format(errorMessage)
}

