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
import scala.collection.JavaConversions._
import scala.collection.generic._
import scala.collection.mutable.{Builder, Map, MapLike}
import scala.reflect._

/** 
 *  MapLike scala interface for Mongo DBObjects - proxies an existing DBObject.
 *  Cannot act as a DBObject or implement it's interface
 * due to conflicts between the java methods and scala methods.
 * Implicits and explicit methods allow you to convert to java though.
 * 
 * We will likely reimplement DBObject itself longterm as a pure base. on the wire format
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 * 
 * @tparam String 
 * @tparam Object 
 */
@BeanInfo
class MongoDBObject(val underlying: DBObject = new BasicDBObject) extends Map[String, AnyRef]
                                                        with MapLike[String, AnyRef, MongoDBObject] with Logging {

  override def empty: MongoDBObject = MongoDBObject.empty

  def iterator = underlying.toMap.iterator.asInstanceOf[Iterator[(String, Object)]]

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
   * @param  key (String) 
   * @tparam A 
   * @return (A)
   * @throws NoSuchElementException
   */
  def as[A : NotNothing](key: String): A = {
    underlying.get(key) match {
      case null => default(key).asInstanceOf[A]
      case value => value.asInstanceOf[A]
    }
  }

  override def get(key: String): Option[AnyRef] = underlying.get(key) match {
    case null => None
    case value => Some(value)
  }

  def ++(pairs: (String, _)*): DBObject = {
    val b = MongoDBObject.newBuilder
    for ((k, v) <- pairs) b += k -> v
    this ++ b.result
  }

  def ++[A <% DBObject](other: A): DBObject = {
    super.++(other: DBObject)
  }

  /**
   * Returns a new list with this MongoDBObject at the *end*
   * Currently only supports composing with other DBObjects, primarily for
   * the use of the Query DSL; if you want heterogenous lists, use
   * MongoDBList directly.
   * @see MongoDBList
   *
   */
  def ::[A <% DBObject](elem: A): Seq[DBObject] = Seq(elem: DBObject, this)

  /**
   * Returns a new list with this MongoDBObject at the *end*
   * Currently only supports composing with other DBObjects, primarily for
   * the use of the Query DSL; if you want heterogenous lists, use
   * MongoDBList directly.
   * @see MongoDBList
   *
   */
  def ::(elem: (String, Any)): Seq[DBObject] = Seq(MongoDBObject(elem), this)

  /** Lazy utility method to allow typing without conflicting with Map's required get() method and causing ambiguity */
  def getAs[A : NotNothing : Manifest](key: String): Option[A] = {
    underlying.get(key) match {
      case null => None
      case value if manifest[A] >:> Manifest.classType(value.getClass) =>
        Some(value.asInstanceOf[A])
      case fail => 
        throw new IllegalArgumentException("Unable to cast '%s' as '%s'; please check your types.".format(Manifest.classType(fail.getClass), manifest[A]))
        None
    }
  }

  def getAsOrElse[A : NotNothing : Manifest](key: String, default: => A): A = getAs[A](key) match {
    case Some(v) => v
    case None => default
  }

  /**
   * Utility method to emulate javascript dot notation
   * Designed to simplify the occasional insanity of working with nested objects.
   * Your type parameter must be that of the item at the bottom of the tree you specify...
   * If cast fails - it's your own fault.
   */
  def expand[A : NotNothing](key: String): Option[A] = {
    @tailrec
    def _dot(dbObj: MongoDBObject, key: String): Option[_] =
      if (key.indexOf('.') < 0) {
        dbObj.getAs[AnyRef](key)
      } else {
        val (pfx, sfx) = key.splitAt(key.indexOf('.'))
        dbObj.getAs[DBObject](pfx) match {
          case Some(base) => _dot(base, sfx.stripPrefix("."))
          case None => None
        }
      }

    _dot(this, key) match {
      case None => None
      case Some(value) => Some(value.asInstanceOf[A])
    }
  }

  def +=(kv: (String, AnyRef)) = {
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
  def isPartialObject = underlying.isPartialObject
  def markAsPartialObject = underlying.markAsPartialObject
  def partialObject = isPartialObject

  override def put(k: String, v: AnyRef) = {
    val cvt = MongoDBObject.convertValue(v) 
    cvt match {
      case _v: Option[_] => 
        underlying.put(k, _v.orNull) match {
          case null => None 
          case value => Some(value)
        }
      case _ =>
        underlying.put(k, cvt) match {
          case null => None
          case value => Some(value)
        }
      }
  }

  def putAll(o: DBObject) = underlying.putAll(o)

  override def toString() = underlying.toString
  override def hashCode() = underlying.hashCode
  override def equals(that: Any) = that match {
    case o: MongoDBObject => underlying.equals(o.underlying)
    case o: MongoDBList => underlying.equals(o.underlying)
    case _ => underlying.equals(that)
  }

  def removeField(key: String) = underlying.removeField(key)
  def toMap = underlying.toMap
  def asDBObject = underlying

  // convenience method to get _id as ObjectId
  def `_id`: Option[ObjectId] = get("_id") match {
    case Some(id: ObjectId) => Some(id)
    case _ => None
  }
}

object MongoDBObject {

  implicit val canBuildFrom: CanBuildFrom[Map[String, Any], (String, Any), DBObject] = 
    new CanBuildFrom[Map[String, Any], (String, Any), DBObject] {
      def apply(from: Map[String, Any]) = apply()
      def apply() = newBuilder[String, Any]
    }

  def empty: DBObject = new MongoDBObject()

  def apply[A <: String, B <: Any](elems: (A, B)*): DBObject = (newBuilder[A, B] ++= elems).result
  def apply[A <: String, B <: Any](elems: List[(A, B)]): DBObject = apply(elems: _*)

  def newBuilder[A <: String, B <: Any]: Builder[(String, Any), DBObject] = new MongoDBObjectBuilder

  protected[mongodb] def convertValue(v: Any): Any = v match {
    case x: MongoDBObject => 
      x.asDBObject
    case m: scala.collection.Map[String, _] => 
      // attempt to convert it to a DBObject
      m.asDBObject
    case _v: Option[_] =>
      val n = convertValue(_v.orNull)
      val z = Option(n)
      z
    case _ =>
      v
  }

}

sealed class MongoDBObjectBuilder extends Builder[(String, Any), DBObject] {
  import com.mongodb.BasicDBObjectBuilder

  protected val empty = BasicDBObjectBuilder.start
  protected var elems = empty

  override def +=(x: (String, Any)) = {
    val cvt = MongoDBObject.convertValue(x._2) 
    cvt match {
      case _v: Option[_] => elems.add(x._1, _v.orNull)
      case _ => elems.add(x._1, cvt)
    }
    this
  }

  def clear() { elems = empty }
  def result(): DBObject = new MongoDBObject(elems.get)
}

// vim: set ts=2 sw=2 sts=2 et:
