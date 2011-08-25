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

package com.mongodb.casbah.util;
package bson.decoding;

import com.mongodb._
import org.bson._
import org.bson.types._

import com.mongodb.casbah.util.bson.decoding.io.BSONByteBuffer;

import java.util.Date 

import scala.annotation.{ tailrec, switch }

import scala.util.control.Exception._
import scala.collection.JavaConverters._
import scala.collection.mutable.{ HashMap, HashSet } 

/**
 * @author Brendan McAdams <brendan@10gen.com>
 */
class OptimizedLazyBSONCallback extends BSONCallback {
  protected var _root: Option[AnyRef] = None

  def apply(): AnyRef = root

  def root: AnyRef = _root.getOrElse(null) // fun with Java. (sigh)

  def get: AnyRef = root

  def reset: Unit = _root = None
    
  def setRootObject(root: AnyRef): Unit = 
    this.root = root

  def root_=(root: AnyRef): Unit = 
    _root = Option(root)

  def createObject(data: Array[Byte], offset: Int): AnyRef = {
    // TODO - LazyBSONList support
    new OptimizedLazyBSONObject(
      BSONByteBuffer(data, offset, data.length - offset), 
      this,
      offset)
  }

  def createDBRef(ns: String, id: ObjectId): AnyRef = 
    new BasicBSONObject("$ns", ns).append("$id", id)


  def createBSONCallback: BSONCallback = 
    throw new UnsupportedOperationException
  
  def objectStart: Unit = 
    throw new UnsupportedOperationException

  def objectStart(name: String): Unit = 
    throw new UnsupportedOperationException

  def objectStart(array: Boolean): Unit = 
    throw new UnsupportedOperationException

  def objectDone: AnyRef = 
    throw new UnsupportedOperationException

  def arrayStart: Unit = 
    throw new UnsupportedOperationException

  def arrayStart(name: String): Unit = 
    throw new UnsupportedOperationException

  def arrayDone: AnyRef = 
    throw new UnsupportedOperationException


  def gotNull(name: String): Unit = 
    throw new UnsupportedOperationException

  def gotUndefined(name: String): Unit = 
    throw new UnsupportedOperationException

  def gotMinKey(name: String): Unit = 
    throw new UnsupportedOperationException

  def gotMaxKey(name: String) = 
    throw new UnsupportedOperationException

  def gotBoolean(name: String, v: Boolean): Unit = 
    throw new UnsupportedOperationException

  def gotDouble(name: String, v: Double): Unit = 
    throw new UnsupportedOperationException

  def gotInt(name: String, v: Int): Unit = 
    throw new UnsupportedOperationException

  def gotLong(name: String, v: Long): Unit = 
    throw new UnsupportedOperationException

  def gotDate(name: String, millis: Long): Unit = 
    throw new UnsupportedOperationException

  def gotString(name: String, v: String): Unit = 
    throw new UnsupportedOperationException

  def gotSymbol(name: String, v: String): Unit = 
    throw new UnsupportedOperationException

  def gotRegex(name: String, pattern: String, flags: String): Unit = 
    throw new UnsupportedOperationException

  def gotTimestamp(name: String, time: Int, inc: Int): Unit = 
    throw new UnsupportedOperationException

  def gotObjectId(name: String, v: ObjectId): Unit = 
    throw new UnsupportedOperationException

  def gotDBRef(name: String, ns: String, id: ObjectId): Unit = 
    throw new UnsupportedOperationException

  @Deprecated
  def gotBinaryArray(name: String, data: Array[Byte]): Unit = 
    throw new UnsupportedOperationException

  def gotBinary(name: String, _type: Byte, data: Array[Byte]): Unit = 
    root = createObject( data, 0 )

  def gotBinary(_type: Byte, data: Array[Byte]): Unit = 
  gotBinary(null, _type, data)

  def gotUUID(name: String, part1: Long, part2: Long): Unit = 
    throw new UnsupportedOperationException

  def gotCode(name: String, code: String): Unit = 
    throw new UnsupportedOperationException

  def gotCodeWScope(name: String, code: String, scope: AnyRef): Unit = 
    throw new UnsupportedOperationException
}

class OptimizedLazyDBCallback(val collection: DBCollection) 
  extends OptimizedLazyBSONCallback with DBCallback {
  // Screw it - if the system gives me a null collection I want to BLOW UP
  val db = collection.getDB()

  override def createObject(data: Array[Byte], offset: Int): AnyRef = {
    val o = new OptimizedLazyDBObject( 
                      BSONByteBuffer(data, offset, data.length - offset), 
                      this, offset )
    /**
     * Need to detect DBRef w/o searching through all fields ...
     * $ref must be the 1st key.
     */
    val it = o.keySet.iterator 
    if (it.hasNext && it.next == "$ref" && o.containsField("$id"))
      new DBRef(db, o)
    else 
      o
  }

  override def createDBRef(ns: String, oid: ObjectId): AnyRef = 
    new DBRef(db, ns, oid)

}
