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

package com.novus.casbah
package mongodb

import com.mongodb._
import scalaj.collection.Implicits._
import Implicits._

/**
 * Base trait for all cursor wrappers.
 *
 * implements Scala's iterable - call jIterator if you want a Java iterator.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
sealed trait MongoCursorWrapper[A] {
  val underlying: DBCursor

  //def itcount() = underlying.itcount()
  def jIterator() = underlying.iterator asScala
  //override def length = underlying.length
  def numGetMores = underlying.numGetMores
  def numSeen =  underlying.numSeen
  def remove() = underlying.remove

  def curr = underlying.curr.asInstanceOf[A]
  def explain = underlying.explain
}

/**
 * A trait that iterates over DBObject or subclasses.
 */
trait DBObjectIterator[A <: DBObject] extends MongoCursorWrapper[A] with Iterator[A] {
  val underlying: DBCursor

  def count = underlying.count
  override def size = count.intValue

  def next: A = underlying.next.asInstanceOf[A]
  def hasNext: Boolean = underlying.hasNext
}

/**
 * Non-Generic DBObject returning wrapper for the Mongo Cursor objects.
 *
 * implements Scala's iterable - call jIterator if you want a Java iterator.
 * 
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 *
 * @param underlying A DBCursor object to wrap
 */
class MongoCursor protected[mongodb] (val underlying: DBCursor) extends DBObjectIterator[DBObject] {
  //def addOption(option: Int) = underlying.addOption(option) asScala
  def batchSize(n: Int) = underlying.batchSize(n) asScala
  def copy() = underlying.copy asScala
  //def getKeysWanted() = underlying.getKeysWanted
  def getSizes() = underlying.getSizes asScala
  def hint(indexKeys: DBObject) = underlying.hint(indexKeys) asScala
  def hint(indexName: String) = underlying.hint(indexName) asScala

  def limit(n: Int) = underlying.limit(n) asScala

  def skip(n: Int) = underlying.skip(n) asScala
  def snapshot() = underlying.snapshot() asScala
  // @todo Add fluid interface for sorting that doesn't require a DBObject
  def sort(orderBy: DBObject) = underlying.sort(orderBy) asScala
  def toArray() = {
    //log.warning("WARNING: Converting a MongoDB Cursor to an Array incurs a huge memory and performance penalty (buffered network pointer vs. all in memory)")
    underlying.toArray() asScala
  }
  def toArray(min: Int) = {
    //log.warning("WARNING: Converting a MongoDB Cursor to an Array incurs a huge memory and performance penalty (buffered network pointer vs. all in memory)")
    underlying.toArray(min) asScala
  }

  override def toString() =  "MongoCursor{Iterator[DBObject] with %d objects.}".format(count)
}

/**
 * Generic parameterized DBObject-subclass returning wrapper for the Mongo Cursor objects.
 * This is instantiated with a type (and an implicitly discovered or explicitly passed Manifest object) to determine it's underlying type.
 *
 * It will attempt to deserialize *ALL* returned results
 * to it's type, on the assumption that the collection matches the type's spec.
 *
 *
 * implements Scala's iterable - call <code>jIterator</code> if you want a Java iterator.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 *
 * @param A  type representing a DBObject subclass which this class should return instead of generic DBObjects
 * @param underlying DBCursor object to proxy
 * @param m Manifest[A] representing the erasure for the underlying type - used to get around the JVM's insanity
 */
class MongoTypedCursor[A <: DBObject : Manifest] protected[mongodb](val underlying: DBCursor) extends MongoCursorWrapper[A] with DBObjectIterator[A] {
  //def addOption(option: Int) = underlying.addOption(option) asScala
  def batchSize(n: Int) = underlying.batchSize(n) asScalaTyped
  def copy() = underlying.copy asScalaTyped
  //def getKeysWanted() = underlying.getKeysWanted
  def getSizes() = underlying.getSizes asScala
  def hint(indexKeys: DBObject) = underlying.hint(indexKeys) asScalaTyped
  def hint(indexName: String) = underlying.hint(indexName) asScalaTyped
  def limit(n: Int) = underlying.limit(n) asScalaTyped

  def skip(n: Int) = underlying.skip(n) asScalaTyped
  def snapshot() = underlying.snapshot() asScalaTyped
  // @todo Add fluid interface for sorting that doesn't require a DBObject
  def sort(orderBy: DBObject) = underlying.sort(orderBy) asScalaTyped
  def toArray() = {
    //log.warning("WARNING: Converting a MongoDB Cursor to an Array incurs a huge memory and performance penalty (buffered network pointer vs. all in memory)")
    underlying.toArray() asScala
  }
  def toArray(min: Int) = {
    //log.warning("WARNING: Converting a MongoDB Cursor to an Array incurs a huge memory and performance penalty (buffered network pointer vs. all in memory)")
    underlying.toArray(min) asScala
  }
  override def toString() =  "MongoCursor{Iterator[_] with %d objects.}".format(count)
}

package mapper {

  trait MappedIterator[P <: AnyRef] extends MongoCursorWrapper[P] with Iterator[P] {
    val underlying: DBCursor
    val mapper: Mapper[_, P]

    def count = underlying.count
    override def size = count.intValue

    def next: P = mapper.from_dbo(underlying.next)
    def hasNext: Boolean = underlying.hasNext
  }

  class MongoMappedCursor[P <: AnyRef : Manifest] protected[mapper](val underlying: DBCursor) extends MappedIterator[P] with MapperImplicits[P] {
    val mapper = Mapper[P]

    def batchSize(n: Int) = underlying.batchSize(n) asScalaMapped
    def copy() = underlying.copy asScalaMapped
    def hint(indexKeys: DBObject) = underlying.hint(indexKeys) asScalaMapped
    def hint(indexName: String) = underlying.hint(indexName) asScalaMapped
    def limit(n: Int) = underlying.limit(n) asScalaMapped
    def skip(n: Int) = underlying.skip(n) asScalaMapped
    def snapshot() = underlying.snapshot() asScalaMapped
    def sort(orderBy: DBObject) = underlying.sort(orderBy) asScalaMapped
    override def toString() =  "MongoMappedCursor{Iterator[_] with %d objects.}".format(count)
  }

}
