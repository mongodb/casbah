/**
 * Copyright (c) 2008 - 2011 10gen, Inc. <http://10gen.com>
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
 */

package com.mongodb.casbah.query.dsl

import com.mongodb.casbah.util.Logging

import com.mongodb.casbah.query._
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder


protected[dsl] class QueryExpressionObject(_underlying: DBObject = new BasicDBObject) extends com.mongodb.casbah.commons.MongoDBObject(_underlying) {}


protected[dsl] object QueryExpressionObject {

  implicit val canBuildFrom: CanBuildFrom[Map[String, Any], (String, Any), DBObject] = new CanBuildFrom[Map[String, Any], (String, Any), DBObject] {
    def apply(from: Map[String, Any]) = apply()
    def apply() = newBuilder[String, Any]
  }

  def empty: DBObject = new QueryExpressionObject()

  //  def apply[A <: String, B <% Any](otherMap: scala.collection.Map[A, B]) = (newBuilder[A, B] ++= otherMap).result
  def apply[A <: String, B <: Any](elems: (A, B)*): DBObject = (newBuilder[A, B] ++= elems).result
  def apply[A <: String, B <: Any](elems: List[(A, B)]): DBObject = apply(elems: _*)

  def newBuilder[A <: String, B <: Any]: Builder[(String, Any), DBObject] = new QueryExpressionObjectBuilder

}

sealed class QueryExpressionObjectBuilder extends Builder[(String, Any), DBObject] {
  import com.mongodb.BasicDBObjectBuilder

  protected val empty = BasicDBObjectBuilder.start
  protected var elems = empty
  override def +=(x: (String, Any)) = {
    elems.add(x._1, x._2)
    this
  }

  def clear() { elems = empty }
  def result(): DBObject = new QueryExpressionObject(elems.get)
}

