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

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.generic._
import scala.collection.mutable
import com.mongodb.casbah.commons.beans.BeanInfo
import scala.util.{Try, Success, Failure}
import java.util

/**
 * MapLike scala interface for Mongo DBObjects - proxies an existing DBObject.
 * Cannot act as a DBObject or implement it's interface
 * due to conflicts between the java methods and scala methods.
 * Implicits and explicit methods allow you to convert to java though.
 *
 * @since 1.0
 *
 */
@BeanInfo
class MongoDBObject(val underlying: DBObject = new BasicDBObject) extends mutable.Map[String, AnyRef]
with mutable.MapLike[String, AnyRef, MongoDBObject] with Logging with Castable {

  override def empty: MongoDBObject = MongoDBObject.empty

  def iterator: Iterator[(String, Object)] = underlying.toMap.iterator.asInstanceOf[Iterator[(String, Object)]]

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
   * @tparam A Any type as long as its not Nothing
   * @return (A)
   * @throws NoSuchElementException or ClassCastException
   */
  def as[A: NotNothing : Manifest](key: String): A = {
    val rawValue = get(key) match {
      case Some(list: BasicDBList) => new MongoDBList(list)
      case Some(x) => x
      case None => None
    }
    castToOption[A](rawValue) match {
      case Some(value) => value
      case None if underlying.containsField(key) => get(key).asInstanceOf[A]
      case _ => default(key).asInstanceOf[A]
    }
  }

  /**
   * Nested as[Type]
   *
   * A helper to recursively fetch and then finally cast items from a
   * DBObject.
   *
   * @param  keys (String*)
   * @tparam A the type to cast the final key to
   * @return (A)
   * @throws NoSuchElementException or ClassCastException
   */
  def as[A: NotNothing: Manifest](keys: String*): A = {
    val path = keys.mkString(".")
    expand(path) match {
      case None => throw new java.util.NoSuchElementException()
      case Some(value) => value
    }
  }

  // scalastyle:off null
  override def get(key: String): Option[AnyRef] = underlying.get(key) match {
    case null => None
    case value => Some(value)
  }
  // scalastyle:on null

  // scalastyle:off method.name
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

  // scalastyle:on method.name

  /**
   * Utility method to allow typing without conflicting with Map's required get() method and causing ambiguity.
   *
   * An invalid cast will cause the return to be None
   *
   * @param key String the key to lookup
   * @tparam A the type to cast the result to
   * @return Option[A] - None if value is None, the cast invalid or the key is missing otherwise Some(value)
   */
  def getAs[A: NotNothing : Manifest](key: String): Option[A] = {
    Try(as[A](key)) match {
      case Success(v) => castToOption[A](v)
      case Failure(e) => None
    }
  }

  /**
   * Nested getAs[Type]
   *
   * A helper to recursively fetch and then finally cast items from a
   * DBObject.
   *
   * @param keys Strings the keys to lookup
   * @tparam A the type to cast the final value
   * @return Option[A] - None if value is None, the cast invalid or the key is missing otherwise Some(value)
   * @return
   */
  def getAs[A: NotNothing : Manifest](keys: String*): Option[A] = expand[A](keys.mkString("."))

  def getAsOrElse[A: NotNothing : Manifest](key: String, default: => A): A = getAs[A](key) match {
    case Some(v) => v
    case None => default
  }

  /**
   * Utility method to emulate javascript dot notation
   *
   * Designed to simplify the occasional insanity of working with nested objects.
   * Your type parameter must be that of the item at the bottom of the tree you specify...
   *
   * If the cast fails it will return None
   */
  def expand[A: NotNothing: Manifest](key: String): Option[A] = {
    // scalastyle:off method.name
    @tailrec
    def _dot(dbObj: MongoDBObject, key: String): Option[_] = {
      key.indexOf('.') match {
        case -1 => dbObj.getAs[A](key)
        case i =>
          val (pfx, sfx) = key.splitAt(i)
          dbObj.getAs[DBObject](pfx) match {
            case Some(base) => _dot(base, sfx.stripPrefix("."))
            case None => None
        }
      }
    // scalastyle:on method.name
    }
    _dot(this, key) match {
      case None => None
      case Some(value) => Some(value.asInstanceOf[A])
    }
  }

  // scalastyle:off method.name public.methods.have.type
  def +=(kv: (String, AnyRef)) = {
    put(kv._1, kv._2)
    this
  }

  def -=(key: String) = {
    underlying.removeField(key)
    this
  }

  // scalastyle:on method.name public.methods.have.type

  /* Methods needed in order to be a proper DBObject */
  def containsField(s: String): Boolean = underlying.containsField(s)

  @deprecated("containsKey is deprecated in the MongoDB Driver. You should use containsField instead.", "2.0")
  def containsKey(s: String): Boolean = underlying.containsField(s)

  // method kept for backwards compatibility
  def isPartialObject: Boolean = underlying.isPartialObject

  def markAsPartialObject(): Unit = underlying.markAsPartialObject()

  def partialObject: Boolean = isPartialObject

  override def put(k: String, v: AnyRef): Option[AnyRef] = {
    // scalastyle:off null
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
    // scalastyle:on null
  }

  def putAll(o: DBObject): Unit = underlying.putAll(o)

  override def toString(): String = underlying.toString

  override def hashCode(): Int = underlying.hashCode

  override def equals(that: Any): Boolean = that match {
    case o: MongoDBObject => underlying.equals(o.underlying)
    case o: MongoDBList => underlying.equals(o.underlying)
    case _ => underlying.equals(that) | that.equals(this)
  }

  def removeField(key: String): AnyRef = underlying.removeField(key)

  def toMap: util.Map[_, _] = underlying.toMap

  def asDBObject: DBObject = underlying

  // scalastyle:off method.name
  // convenience method to get _id as ObjectId
  def `_id`: Option[ObjectId] = get("_id") match {
    case Some(id: ObjectId) => Some(id)
    case _ => None
  }

  // scalastyle:on method.name
}

object MongoDBObject {

  implicit val canBuildFrom: CanBuildFrom[Map[String, Any], (String, Any), DBObject] =
    new CanBuildFrom[Map[String, Any], (String, Any), DBObject] {
      def apply(from: Map[String, Any]): mutable.Builder[(String, Any), DBObject] = apply()

      def apply(): mutable.Builder[(String, Any), DBObject] = newBuilder[String, Any]
    }

  def empty: DBObject = new MongoDBObject()

  def apply[A <: String, B <: Any](elems: (A, B)*): DBObject = (newBuilder[A, B] ++= elems).result()

  def apply[A <: String, B <: Any](elems: List[(A, B)]): DBObject = apply(elems: _*)

  def newBuilder[A <: String, B <: Any]: mutable.Builder[(String, Any), DBObject] = new MongoDBObjectBuilder

  protected[mongodb] def convertValue(v: Any): Any = v match {
    case x: MongoDBObject => x.asDBObject
    case m: Map[_, _] => m.asInstanceOf[Map[String, _]].asDBObject
    case _v: Option[_] => Option(convertValue(_v.orNull))
    case _ => v
  }
}

sealed class MongoDBObjectBuilder extends mutable.Builder[(String, Any), DBObject] {

  import com.mongodb.BasicDBObjectBuilder

  protected val empty = BasicDBObjectBuilder.start
  protected var elems = empty

  // scalastyle:off method.name
  override def +=(x: (String, Any)): this.type = {
    val cvt = MongoDBObject.convertValue(x._2)
    cvt match {
      case _v: Option[_] => elems.add(x._1, _v.orNull)
      case _ => elems.add(x._1, cvt)
    }
    this
  }

  // scalastyle:on method.name

  def clear() {
    elems = empty
  }

  def result(): DBObject = new MongoDBObject(elems.get)
}
