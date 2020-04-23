/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
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
package commons

import com.mongodb.casbah.commons.Imports._

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.{ Try, Success, Failure }

class MongoDBList(val underlying: BasicDBList = new BasicDBList) extends mutable.Seq[Any] with Castable {

  def apply(i: Int): AnyRef = underlying.get(i)

  def update(i: Int, elem: Any): Unit =
    underlying.set(i, elem.asInstanceOf[AnyRef])

  // scalastyle:off method.name
  def +=:(elem: Any): this.type = {
    underlying.subList(0, 0).add(elem.asInstanceOf[AnyRef])
    this
  }

  def +=(elem: Any): this.type = {
    underlying.add(elem.asInstanceOf[AnyRef])
    this
  }

  // scalastyle:on method.name

  def insertAll(i: Int, elems: scala.Traversable[Any]) {
    val ins = underlying.subList(0, i)
    elems.foreach(x => ins.add(x.asInstanceOf[AnyRef]))
  }

  def remove(i: Int): AnyRef = underlying.remove(i)

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

  def as[A: NotNothing](idx: Int): A = {
    // scalastyle:off null
    underlying.get(idx) match {
      case null  => throw new NoSuchElementException
      case value => value.asInstanceOf[A]
    }
    // scalastyle:on null
  }

  /** Lazy utility method to allow typing without conflicting with Map's required get() method and causing ambiguity */
  def getAs[A: NotNothing: Manifest](idx: Int): Option[A] = {
    Try(as[A](idx)) match {
      case Success(v) => castToOption[A](v)
      case Failure(e) => None
    }
  }

  def getAsOrElse[A: NotNothing: Manifest](idx: Int, default: => A): A = getAs[A](idx) match {
    case Some(v) => v
    case None    => default
  }

  def clear(): Unit = underlying.clear()

  def result: MongoDBList = this

  def length: Int = underlying.size

  override def isEmpty: Boolean = underlying.isEmpty

  override def iterator: Iterator[AnyRef] = underlying.iterator.asScala

  override def toString(): String = underlying.toString

  override def hashCode(): Int = underlying.hashCode

  override def equals(that: Any): Boolean = that match {
    case o: MongoDBObject => underlying.equals(o.underlying)
    case o: MongoDBList   => underlying.equals(o.underlying)
    case _                => underlying.equals(that) | that.equals(this)
  }
}

object MongoDBList {

  def empty: MongoDBList = new MongoDBList()

  def apply[A <: Any](elems: A*): MongoDBList = {
    val b = newBuilder[A]
    for { xs <- elems } xs match {
      case p: Tuple2[_, _] => b += MongoDBObject(p.asInstanceOf[(String, Any)])
      case _               => b += xs
    }
    b.result()
  }

  def concat[A](xss: scala.Traversable[A]*): MongoDBList = {
    val b = newBuilder[A]
    if (xss forall (_.isInstanceOf[IndexedSeq[_]])) b.sizeHint(xss.map(_.size).sum)
    for { xs <- xss } b ++= xs
    b.result()
  }

  def newBuilder[A <: Any]: MongoDBListBuilder = new MongoDBListBuilder

}

sealed class MongoDBListBuilder extends scala.collection.mutable.Builder[Any, MongoDBList] {

  protected val empty: MongoDBList = new MongoDBList

  protected var elems: MongoDBList = empty

  // scalastyle:off method.name public.methods.have.type
  override def addOne(x: Any) = {
    elems.add(x.asInstanceOf[AnyRef])
    this
  }

  // scalastyle:on method.name public.methods.have.type

  def clear() {
    elems = empty
  }

  //override def result(): MongoDBList = elems
  override def result() = elems
}
