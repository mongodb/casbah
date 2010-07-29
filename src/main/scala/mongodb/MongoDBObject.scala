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
 *     http://bitbucket.org/novus/casbah
 * 
 * NOTICE: Portions of this work are derived from the Apache License 2.0 "mongo-scala-driver" work
 * by Alexander Azarov <azarov@osinka.ru>, available from http://github.com/alaz/mongo-scala-driver
 */

package com.novus.casbah
package mongodb

import Implicits._
import util.Logging

import com.mongodb._

import scala.collection.JavaConversions._
import scala.collection.generic._
import scala.collection.mutable.Map
import scala.reflect._

/** 
 *  MapLike scala interface for Mongo DBObjects - proxies an existing DBObject.
 *  Cannot act as a DBObject or implement it's interface
 * due to conflicts between the java methods and scala methods.
 * Implicits and explicit methods allow you to convert to java though.
 * 
 * This is a very crappy, thin interface and likely to go away. Use it at your own risk.
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0, 06/02/10
 * @since 1.0
 * 
 * @tparam String 
 * @tparam Object 
 */
@BeanInfo
trait MongoDBObject extends Map[String, Object] with Logging {
  val underlying: DBObject

  def iterator = underlying.toMap.iterator.asInstanceOf[Iterator[(String, Object)]]


  override def get(key: String): Option[Object] = underlying.get(key) match {
    case null => None
    case value => Some(value)
  }


  /** Lazy utility method to allow typing without conflicting with Map's required get() method and causing ambiguity */
  def getAs[A <% AnyRef : Manifest](key: String): Option[A] = {
    require(manifest[A] != manifest[scala.Nothing], "Type inference failed; getAs[A]() requires an explicit type argument (e.g. dbObject[<ReturnType](\"someKey\") ) to function correctly.")
    underlying.get(key) match {
      case null => None
      case value => Some(value.asInstanceOf[A])
    }
  }

  def +=(kv: (String, Object)) = {
    put(kv._1, kv._2)
    this
  }

  def -=(key: String) = { 
    underlying.removeField(key)
    this
  }
    
  /* Methods needed in order to be a proper DBObject */
  def containsField(s: String) = underlying.containsField(s)
  @deprecated("containsKey is deprecated in the MongoDB Driver. You should use containsField instead.")
  def containsKey(s: String) = underlying.containsField(s) // method kept for backwards compatibility
  //def get(s: String) = underlying.get(s)
  def isPartialObject = underlying.isPartialObject
  def markAsPartialObject = underlying.markAsPartialObject
  def partialObject = isPartialObject
  override def put(k: String, v: AnyRef) = v match {
    case x: MongoDBObject => put(k, x.asDBObject) 
    case _ => underlying.put(k, v) match {
      case null => None
      case value => Some(value)
    }
  }
  def putAll(o: DBObject) = underlying.putAll(o)
  def putAll(m: Map[_, _]) = underlying.putAll(m)
  def putAll(m: java.util.Map[_, _]) = underlying.putAll(m)
  def removeField(key: String) = underlying.removeField(key)
  def toMap = underlying.toMap
  def asDBObject = underlying
}


object MongoDBObject  {
  
  def empty = new MongoDBObject { val underlying = new BasicDBObject }

  def apply[A <: String, B <: Any](elems: (A, B)*) = (newBuilder[A, B] ++= elems).result

  def newBuilder[A <: String, B <: Any]: MongoDBObjectBuilder = new MongoDBObjectBuilder

}

protected[mongodb] class MongoDBObjectBuilder extends scala.collection.mutable.Builder[(String, Any), MongoDBObject] {
  protected val empty = BasicDBObjectBuilder.start
  protected var elems = empty
  override def +=(x: (String, Any)) = { 
    elems.add(x._1, x._2)
    this 
  }

  def clear() { elems = empty }
  def result = new MongoDBObject { val underlying = elems.get }
}

// vim: set ts=2 sw=2 sts=2 et:
