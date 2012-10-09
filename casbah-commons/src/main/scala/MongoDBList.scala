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


import scala.collection.mutable._
import scalaj.collection.Imports._

class MongoDBList(val underlying: BasicDBList = new BasicDBList) extends Seq[Any] {

  def apply(i: Int) = underlying.get(i)

  def update(i: Int, elem: Any) =
    underlying.set(i, elem.asInstanceOf[AnyRef])

  def +=:(elem: Any): this.type = {
    underlying.subList(0, 0).add(elem.asInstanceOf[AnyRef])
    this
  }

  def +=(elem: Any): this.type = {
    underlying.add(elem.asInstanceOf[AnyRef])
    this
  }

  def insertAll(i: Int, elems: scala.collection.Traversable[Any]) = {
    val ins = underlying.subList(0, i)
    elems.foreach(x => ins.add(x.asInstanceOf[AnyRef]))
  }

  def remove(i: Int) = underlying.remove(i)

  /**
   * as
   *
   * Works like apply(), unsafe, bare return of a value.
   * Returns default if nothing matching is found, else
   * tries to cast a value to the specified type.
   *
   * Unless you overrode it, default throws
   * a NoSuchElementException
   *
   * @param idx (Int)
   * @tparam A
   * @return (A)
   * @throws NoSuchElementException
   */
  def as[A : NotNothing](idx: Int): A = {
    underlying.get(idx) match {
      case null => throw new NoSuchElementException
      case value => value.asInstanceOf[A]
    }
  }

  /** Lazy utility method to allow typing without conflicting with Map's required get() method and causing ambiguity */
  def getAs[A : NotNothing](idx: Int): Option[A] = {
    underlying.get(idx) match {
      case null => None
      case value => Some(value.asInstanceOf[A])
    }
  }

  def getAsOrElse[A : NotNothing](idx: Int, default: => A): A = getAs[A](idx) match {
    case Some(v) => v
    case None => default
  }

  def clear = underlying.clear

  def result = this

  def length = underlying.size

  override def isEmpty = underlying.isEmpty
  override def iterator = underlying.iterator.asScala

  override def toString() = underlying.toString
  override def hashCode() = underlying.hashCode
  override def equals(that: Any) = that match {
    case o: MongoDBObject => underlying.equals(o.underlying)
    case o: MongoDBList => underlying.equals(o.underlying)
    case _ => underlying.equals(that)
  }
}

object MongoDBList {

  def empty: MongoDBList = new MongoDBList()

  def apply[A <: Any](elems: A*): MongoDBList = {
    val b = newBuilder[A]
    for (xs <- elems) xs match {
      case p: Tuple2[String, _] => b += MongoDBObject(p)
      case _ => b += xs
    }
    b.result
  }

  def concat[A](xss: scala.collection.Traversable[A]*): MongoDBList = {
    val b = newBuilder[A]
    if (xss forall (_.isInstanceOf[IndexedSeq[_]]))
      b.sizeHint(xss map (_.size) sum)

    for (xs <- xss) b ++= xs
    b.result
  }

  def newBuilder[A <: Any]: MongoDBListBuilder = new MongoDBListBuilder

}

sealed class MongoDBListBuilder extends scala.collection.mutable.Builder[Any, Seq[Any]] {

  protected val empty: MongoDBList = new MongoDBList()

  protected var elems: MongoDBList = empty

  override def +=(x: Any) = {
    val v = x match {
      case _ => x.asInstanceOf[AnyRef]
    }
    elems.add(v)
    this
  }

  def clear() { elems = empty }

  def result: MongoDBList = elems
}
