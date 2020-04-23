/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
 * Copyright (c) 2009 Novus Partners, Inc. <http://novus.com>
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
package util

import com.mongodb.{ casbah, Bytes }

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging

import org.bson.types.BSONTimestamp

/**
 * For a more detailed understanding of how the MongoDB Oplog works, see Kristina Chodorow's blogpost:
 *
 * http://www.kchodorow.com/blog/2010/10/12/replication-internals/
 */
class MongoOpLog(
    mongoClient:    MongoClient           = MongoClient(),
    startTimestamp: Option[BSONTimestamp] = None,
    namespace:      Option[String]        = None,
    replicaSet:     Boolean               = true
) extends Iterator[MongoOpLogEntry] with Logging {

  implicit object BSONTimestampOk extends ValidDateOrNumericType[org.bson.types.BSONTimestamp]

  protected val local: MongoDB = mongoClient("local")
  protected val oplogName: String = if (replicaSet) "oplog.rs" else "oplog.$main"
  protected val oplog: MongoCollection = local(oplogName)

  val tsp: BSONTimestamp = verifyOpLog

  log.debug("Beginning monitoring OpLog at '%s'", tsp)

  val q = namespace match {
    case Some(ns) => ("ts" $gt tsp) ++~ ("ns" -> ns)
    case None     => "ts" $gt tsp
  }

  log.debug("OpLog Filter: '%s'", q)

  // scalastyle:off public.methods.have.type
  val cursor: _root_.com.mongodb.casbah.Imports.MongoCursor = oplog.find(q)
  cursor.option = Bytes.QUERYOPTION_TAILABLE
  cursor.option = Bytes.QUERYOPTION_AWAITDATA

  def next() = MongoOpLogEntry(cursor.next())

  // scalastyle:on public.methods.have.type

  def hasNext: Boolean = cursor.hasNext

  def close(): Unit = cursor.close()

  def verifyOpLog: BSONTimestamp = {
    // Verify the oplog exists
    val last = oplog.find().sort(MongoDBObject("$natural" -> 1)).limit(1)
    assume(
      last.hasNext,
      "No oplog found. mongod must be a --master or belong to a Replica Set."
    )

    /**
     * If a startTimestamp was specified attempt to sync from there.
     * An exception is thrown if the timestamp isn't found because
     * you won't be able to sync.
     */
    startTimestamp match {
      case Some(ts) => {
        oplog.findOne(MongoDBObject("ts" -> ts)).orElse(
          throw new Exception("No oplog entry for requested start timestamp.")
        )
        ts
      }
      case None => last.next().as[BSONTimestamp]("ts")
    }
  }

}

object MongoOpLogEntry {
  // scalastyle:off public.methods.have.type
  def apply(entry: MongoDBObject) = entry("op") match {
    case InsertOp.typeCode =>
      MongoInsertOperation(
        entry.as[BSONTimestamp]("ts"),
        entry.getAs[Long]("h"),

        /** TODO - It won't be there for Master/Slave, but should we check it for RS? */
        entry.as[String]("ns"),
        entry.as[DBObject]("o")
      )
    case UpdateOp.typeCode =>
      MongoUpdateOperation(
        entry.as[BSONTimestamp]("ts"),
        entry.getAs[Long]("h"),

        /** TODO - It won't be there for Master/Slave, but should we check it for RS? */
        entry.as[String]("ns"),
        entry.as[DBObject]("o"),
        entry.as[DBObject]("o2")
      )
    case DeleteOp.typeCode =>
      MongoDeleteOperation(
        entry.as[BSONTimestamp]("ts"),
        entry.getAs[Long]("h"),

        /** TODO - It won't be there for Master/Slave, but should we check it for RS? */
        entry.as[String]("ns"),
        entry.as[DBObject]("o")
      )
    case NoOp.typeCode =>
      MongoNoOperation(
        entry.as[BSONTimestamp]("ts"),
        entry.getAs[Long]("h"),
        /** TODO - It won't be there for Master/Slave, but should we check it for RS? */
        entry.as[String]("ns"),
        entry.as[DBObject]("o")
      )

  }
}

sealed trait MongoOpType {
  def typeCode: String
}

case object InsertOp extends MongoOpType {
  val typeCode = "i"
}

case object UpdateOp extends MongoOpType {
  val typeCode = "u"
}

case object DeleteOp extends MongoOpType {
  val typeCode = "d"
}
case object NoOp extends MongoOpType {
  val typeCode = "n"
}

sealed trait MongoOpLogEntry {
  val timestamp: BSONTimestamp
  lazy val ts = timestamp

  /** Master/Slave does *not* include the opID, so make it Option. */
  val opID: Option[Long]
  lazy val h = opID

  val opType: MongoOpType
  lazy val op = opType

  val namespace: String
  lazy val ns = namespace

  val document: MongoDBObject
  lazy val o = document

}

case class MongoInsertOperation(timestamp: BSONTimestamp, opID: Option[Long], namespace: String, document: MongoDBObject) extends MongoOpLogEntry {
  val opType = InsertOp
}

case class MongoUpdateOperation(timestamp: BSONTimestamp, opID: Option[Long], namespace: String,
                                document: MongoDBObject, documentID: MongoDBObject) extends MongoOpLogEntry {
  val opType = UpdateOp
  /** In updates, 'o' gives the modifications, and 'o2' includes the 'update criteria' (the query to run) */
  lazy val o2 = documentID
}

case class MongoDeleteOperation(timestamp: BSONTimestamp, opID: Option[Long], namespace: String, document: MongoDBObject) extends MongoOpLogEntry {
  val opType = DeleteOp
}

case class MongoNoOperation(timestamp: BSONTimestamp, opID: Option[Long], namespace: String, document: MongoDBObject) extends MongoOpLogEntry {
  val opType = NoOp
}
