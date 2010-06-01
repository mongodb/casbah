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
trait ScalaMongoCursorWrapper[A <: DBObject] extends Iterator[A] {
  val underlying: DBCursor

  def count() = underlying.count
  def itcount() = underlying.itcount()
  def jIterator() = underlying.iterator asScala
  override def length() = underlying.length
  def numGetMores() = underlying.numGetMores
  def numSeen() =  underlying.numSeen
  def remove() = underlying.remove

  def curr() = underlying.curr.asInstanceOf[A]
  def explain() = underlying.explain.asInstanceOf[A]

  def next(): A = underlying.next.asInstanceOf[A]
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
protected class ScalaMongoCursor (val underlying: DBCursor) extends ScalaMongoCursorWrapper[DBObject]  {
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

  override def toString() =  "ScalaMongoCursor{Iterator[DBObject] with %d objects.}".format(count)
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
protected class ScalaTypedMongoCursor[A <: DBObject] (val underlying: DBCursor)(implicit m: scala.reflect.Manifest[A])  extends ScalaMongoCursorWrapper[A]  {
  //def addOption(option: Int) = underlying.addOption(option) asScala
  def batchSize(n: Int) = underlying.batchSize(n) asScalaTyped(m)
  def copy() = underlying.copy asScalaTyped(m)
  //def getKeysWanted() = underlying.getKeysWanted
  def getSizes() = underlying.getSizes asScala
  def hint(indexKeys: DBObject) = underlying.hint(indexKeys) asScalaTyped(m)
  def hint(indexName: String) = underlying.hint(indexName) asScalaTyped(m)
  def limit(n: Int) = underlying.limit(n) asScalaTyped(m)

  def skip(n: Int) = underlying.skip(n) asScalaTyped(m)
  def snapshot() = underlying.snapshot() asScalaTyped(m)
  // @todo Add fluid interface for sorting that doesn't require a DBObject
  def sort(orderBy: DBObject) = underlying.sort(orderBy) asScalaTyped(m)
  def toArray() = {
    //log.warning("WARNING: Converting a MongoDB Cursor to an Array incurs a huge memory and performance penalty (buffered network pointer vs. all in memory)")
    underlying.toArray() asScala
  }
  def toArray(min: Int) = {
    //log.warning("WARNING: Converting a MongoDB Cursor to an Array incurs a huge memory and performance penalty (buffered network pointer vs. all in memory)")
    underlying.toArray(min) asScala
  }
  override def toString() =  "ScalaMongoCursor{Iterator[%s] with %d objects.}".format(m.toString, count)
}
