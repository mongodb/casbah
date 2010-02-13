/**
 * Copyright (c) 2010, Novus Partners, Inc. <http://novus.com>
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
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

package com.novus.util.mongodb

import com.mongodb.{DBObject, BasicDBObjectBuilder}
import org.scala_tools.javautils.Imports._
import Implicits._

trait QueryOperators extends NotEqualsOp with
                             LessThanOp with
                             LessThanEqualOp with
                             GreaterThanOp with
                             GreaterThanEqualOp with
                             InOp with
                             NotInOp with
                             ModuloOp with
                             SizeOp with
                             ExistsOp


sealed trait QueryOperator {
  val field: String
  protected var dbObj: Option[DBObject] = None
  protected def op(op: String, target: Any) = {
    dbObj match {
      case Some(nested) => {
        nested.put(op, target)
        (field -> nested)
      }
      case None => {
        val opMap = BasicDBObjectBuilder.start(op, target).get
        (field -> opMap)
      }
    }
  }
}

trait NotEqualsOp extends QueryOperator {
  def $ne(target: String) = op("$ne", target)
  def $ne(target: AnyVal) = op("$ne", target)
  def $ne(target: DBObject) = op("$ne", target)
  def $ne(target: Map[String, Any]) = op("$ne", target.asDBObject)
}

trait LessThanOp extends QueryOperator {
  def $lt(target: String) = op("$lt", target)
  def $lt(target: AnyVal) = op("$lt", target)
  def $lt(target: DBObject) = op("$lt", target)
  def $lt(target: Map[String, Any]) = op("$lt", target.asDBObject)
}

trait LessThanEqualOp extends QueryOperator {
  def $lte(target: String) = op("$lte", target)
  def $lte(target: AnyVal) = op("$lte", target)
  def $lte(target: DBObject) = op("$lte", target)
  def $lte(target: Map[String, Any]) = op("$lte", target.asDBObject)
}

trait GreaterThanOp extends QueryOperator {
  def $gt(target: String) = op("$gt", target)
  def $gt(target: AnyVal) = op("$gt", target)
  def $gt(target: DBObject) = op("$gt", target)
  def $gt(target: Map[String, Any]) = op("$gt", target.asDBObject)
}

trait GreaterThanEqualOp extends QueryOperator {
  def $gte(target: String) = op("$gte", target)
  def $gte(target: AnyVal) = op("$gte", target)
  def $gte(target: DBObject) = op("$gte", target)
  def $gte(target: Map[String, Any]) = op("$gte", target.asDBObject)
}

trait InOp extends QueryOperator {
  def $in(target: Array[Any]) = op("$in", target.asJava)
  def $in(target: Any*) = op("$in", target.asJava)
}

trait NotInOp extends QueryOperator {
  def $nin(target: Array[Any]) = op("$nin", target.asJava)
  def $nin(target: Any*) = op("$nin", target.asJava)
}

trait AllOp extends QueryOperator {
  def $all(target: Array[Any]) = op("$all", target.asJava)
  def $all(target: Any*) = op("$all", target.asJava)
}

trait ModuloOp extends QueryOperator {
  def $mod(target: String) = op("$mod", target)
  def $mod(target: AnyVal) = op("$mod", target)
  def $mod(target: DBObject) = op("$mod", target)
  def $mod(target: Map[String, Any]) = op("$mod", target.asDBObject)
}

trait SizeOp extends QueryOperator {
  def $size(target: String) = op("$size", target)
  def $size(target: AnyVal) = op("$size", target)
  def $size(target: DBObject) = op("$size", target)
}

trait ExistsOp extends QueryOperator {
  def $exists(target: Boolean) = op("$exists", target)
}

// @Todo Regex support

