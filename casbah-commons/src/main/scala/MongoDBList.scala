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
package commons

import com.mongodb.casbah.commons.Imports._

import scala.annotation.tailrec
import scala.collection.generic._
import scala.collection.mutable.Buffer
import scala.reflect._

import scalaj.collection.Imports._


import com.mongodb.BasicDBList


trait MongoDBList extends Buffer[AnyRef] {
  val underlying: BasicDBList
  
  def apply(i: Int) = underlying.get(i)
  
  def update(i: Int, elem: AnyRef) = 
    underlying.set(i, elem)

  def +=:(elem: AnyRef): this.type = { 
    underlying.subList(0, 0).add(elem)
    this
  }

  def +=(elem: AnyRef): this.type = {
    underlying.add(elem)
    this
  }

  def insertAll(i: Int, elems: Traversable[AnyRef]) = {
    val ins = underlying.subList(0, i)
    elems.foreach(x => ins.add(x)) 
  }

  def remove(i: Int) = underlying.remove(i)

  def clear = underlying.clear

  def result = this

  def length = underlying.size

  override def isEmpty = underlying.isEmpty
  override def iterator = underlying.iterator.asScala
}

object MongoDBList {
  
  def empty: BasicDBList = 
    new MongoDBList { val underlying = new BasicDBList }

  def apply[A <: Any](elems: A*): BasicDBList = {
    val b = newBuilder[A]
    for (xs <- elems) xs match {
      case p: Tuple2[String, _] => b += MongoDBObject(p) 
      case _ => b += xs 
    }
    b.result
  }

  def concat[A](xss: Traversable[A]*): BasicDBList = {
    val b = newBuilder[A]
    if (xss forall (_.isInstanceOf[IndexedSeq[_]]))
      b.sizeHint(xss map (_.size) sum)

    for (xs <- xss) b ++= xs
    b.result
  }


  def newBuilder[A <: Any]: MongoDBListBuilder = 
    new MongoDBListBuilder

}

sealed class MongoDBListBuilder 
       extends scala.collection.mutable.Builder[Any, BasicDBList] {

  protected val empty = new BasicDBList

  protected var elems = empty

  override def +=(x: Any) = { 
    val v = x match {
      case _ => x.asInstanceOf[AnyRef]
    }
    elems.add(v)
    this 
  }

  def clear() { elems = empty }

  def result: BasicDBList = new MongoDBList { val underlying = elems }
}
