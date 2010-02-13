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

import com.mongodb._
import org.scala_tools.javautils.Imports._
import Implicits._

trait ScalaMongoCursorWrapper[A <: DBObject] extends Iterator[A] {
  val underlying: DBCursor

  def count() = underlying.count
  def itcount() = underlying.itcount()
  def jIterator() = underlying.iterator asScala
  def length() = underlying.length
  def numGetMores() = underlying.numGetMores
  def numSeen() =  underlying.numSeen
  def remove() = underlying.remove

  def curr() = underlying.curr.asInstanceOf[A]
  def explain() = underlying.explain.asInstanceOf[A]

  def next(): A = underlying.next.asInstanceOf[A]
  def hasNext: Boolean = underlying.hasNext

}

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