/**
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
 *     http://github.com/novus/casbah
 * 
 */

package com.novus.casbah
package commons

import com.novus.casbah.commons.Imports._

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.generic._
import scala.collection.mutable.Buffer
import scala.reflect._


import com.mongodb.BasicDBList


trait MongoDBList extends Buffer[Any] {
  val underlying: BasicDBList
  
  def apply(i: Int) = underlying.get(i)
  
  def update(i: Int, elem: Any) = update[Any](i, elem)
  def update[A <: Any](i: Int, elem: A) = 
    underlying.set(i, elem.asInstanceOf[AnyRef])

  def +=:(elem: Any): this.type = +=:[Any](elem)
  def +=:[A <: Any](elem: A): this.type = { 
    underlying.subList(0, 0).add(elem.asInstanceOf[AnyRef])
    this
  }

  def +=(elem: Any): this.type = +=[Any](elem)
  def +=[A <: Any](elem: A): this.type = {
    underlying.add(elem.asInstanceOf[AnyRef])
    this
  }

  def insertAll(i: Int, elems: Traversable[Any]) = insertAll[Any](i, elems)
  def insertAll[A <: Any](i: Int, elems: Traversable[A]) = {
    val ins = underlying.subList(0, i)
    elems.foreach(x => ins.add(x.asInstanceOf[AnyRef])) 
  }

  def remove(i: Int) = underlying.remove(i) 

  def clear = underlying.clear

  def result = this

  def length = underlying.size

  override def isEmpty = underlying.isEmpty
  override def iterator: Iterator[Any] = underlying.iterator
}

object MongoDBList {
  
  def empty: BasicDBList = 
    new MongoDBList { val underlying = new BasicDBList }

  def apply[A <: Any](elems: A*): DBObject = 
    (newBuilder[A] ++= elems).result

  def newBuilder[A <: Any]: MongoDBListBuilder = 
    new MongoDBListBuilder

}

sealed class MongoDBListBuilder 
       extends scala.collection.mutable.Builder[Any, BasicDBList] {

  protected val empty = new BasicDBList

  protected var elems = empty

  override def +=(x: Any) = { 
    elems.add(x.asInstanceOf[AnyRef])
    this 
  }

  def clear() { elems = empty }

  def result: BasicDBList = new MongoDBList { val underlying = elems }
}
