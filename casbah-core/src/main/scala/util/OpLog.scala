/**
 * Copyright (c) 2010, 2011 10gen, Inc. <http://10gen.com>
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
package util

import org.bson.types.BSONTimestamp
import com.mongodb.Bytes

import com.mongodb.casbah.Imports._

import scala.util.control.Exception._

class MongoOpLog(mongo: MongoConnection = MongoConnection(),
  startTimestamp: Option[BSONTimestamp] = None,
  namespace: Option[String] = None) extends Iterator[MongoOpLogEntry] with Logging {

  implicit object BSONTimestampOk extends ValidDateOrNumericType[org.bson.types.BSONTimestamp]

  protected val local = mongo("local")
  protected val oplog = local("oplog.$main")

  val tsp = verifyOpLog

  log.debug("Beginning monitoring OpLog at '%s'", tsp)

  val q = namespace match {
    case Some(ns) => ("ts" $gt tsp) ++ ("ns" -> ns)
    case None => "ts" $gt tsp
  }

  log.debug("OpLog Filter: '%s'", q)

  val cursor = oplog.find(q)

  cursor.options = Bytes.QUERYOPTION_TAILABLE
  cursor.options = Bytes.QUERYOPTION_AWAITDATA

  def hasNext = cursor.hasNext

  def next = MongoOpLogEntry(cursor.next)

  def verifyOpLog: BSONTimestamp = {
    // Verify the oplog exists 
    val last = oplog.find().sort(MongoDBObject("$natural" -> 1)).limit(1)
    assume(last.hasNext,
      "No oplog found. mongod must be a --master or belong to a Replica Set.")
    /**
     * If a startTimestamp was specified attempt to sync from there.
     * An exception is thrown if the timestamp isn't found because
     * you won't be able to sync. 
     */
    startTimestamp match {
      case Some(ts) => {
        oplog.findOne(MongoDBObject("ts" -> ts)).orElse(
          throw new Exception("No oplog entry for requested start timestamp."))
        ts
      }
      case None => last.next().as[BSONTimestamp]("ts")
    }
  }

}

object MongoOpLogEntry {
  def apply(entry: MongoDBObject) = entry("op") match {
    case InsertOp =>
      MongoInsertOperation(
        entry.as[BSONTimestamp]("ts"),
        entry.as[Long]("h"),
        entry.as[String]("ns"),
        entry.as[DBObject]("o"))
    case UpdateOp =>
      MongoUpdateOperation(
        entry.as[BSONTimestamp]("ts"),
        entry.as[Long]("h"),
        entry.as[String]("ns"),
        entry.as[DBObject]("o"),
        entry.as[DBObject]("o2"))
    case DeleteOp =>
      MongoInsertOperation(
        entry.as[BSONTimestamp]("ts"),
        entry.as[Long]("h"),
        entry.as[String]("ns"),
        entry.as[DBObject]("o"))
  }
}

sealed trait MongoOpType { def typeCode: String }
case object InsertOp extends MongoOpType { val typeCode = "i" }
case object UpdateOp extends MongoOpType { val typeCode = "u" }
case object DeleteOp extends MongoOpType { val typeCode = "d" }

sealed trait MongoOpLogEntry {
  val timestamp: BSONTimestamp
  lazy val ts = timestamp

  val opID: Long
  lazy val h = opID

  val opType: MongoOpType
  lazy val op = opType

  val namespace: String
  lazy val ns = namespace

  val document: MongoDBObject
  lazy val o = document

}

case class MongoInsertOperation(timestamp: BSONTimestamp, opID: Long, namespace: String, document: MongoDBObject) extends MongoOpLogEntry {
  val opType = InsertOp
}

case class MongoUpdateOperation(timestamp: BSONTimestamp, opID: Long, namespace: String, document: MongoDBObject, documentID: MongoDBObject) extends MongoOpLogEntry {
  val opType = UpdateOp
  lazy val o2 = documentID
}

case class MongoDeleteOperation(timestamp: BSONTimestamp, opID: Long, namespace: String, document: MongoDBObject) extends MongoOpLogEntry {
  val opType = DeleteOp
}

// vim: set ts=2 sw=2 sts=2 et:
