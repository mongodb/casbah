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

package com.novus.util.mongodb

import collection.jcl.{LinkedHashMap, HashSet}
import reflect.Manifest
import com.mongodb.DBObject
import com.novus.util.Logging

/**
 * The <code>ReflectiveBeanMapper</code> object provides utility functions
 * and internal caching for on-the-fly, reflective Mongo DBObjects.
 *
 * @version 1.0
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * 
 */
object ReflectiveBeanMapper extends Logging {
  type ProxyMethod = (Any, java.lang.Object*) => java.lang.Object
  protected[mongodb] val getters = new scala.collection.mutable.HashMap[Tuple2[Class[_], String], ProxyMethod]
  protected[mongodb] val setters = new scala.collection.mutable.HashMap[Tuple3[Class[_], String, AnyRef], ProxyMethod]

  def apply(caller: ReflectiveBeanMapper, field: String): Option[ProxyMethod] =  {
    getters.get((caller.getClass, field)) match {
      case Some(getProxy) => Some(getProxy)
      case None => {
        try {
          log.debug("Creating new method mapping for getter %s", field)
          val getMethod = caller.getClass.getMethod("get%s".format(field.capitalize))
          val getMethodProxy = getMethod.invoke _
          getters.update((caller.getClass, field), getMethodProxy)
          Some(getMethodProxy)
        } catch {
          case nsmE => None // @todo this is probably a problem.  Compile time check?
        }
      }
    }
  }

  def apply(caller: ReflectiveBeanMapper, field: String, value: Class[_]): Option[ProxyMethod] = {
    log.debug("Value: %s for %s", value.toString, "set%s".format(field.capitalize))
    // @todo make this less...wonky
    val lookupType = value.toString match {
      case "class scala.BigDecimal" =>  classOf[java.math.BigDecimal]
      case "class com.mongodb.BasicDBObject" =>  classOf[com.mongodb.DBObject]
      case unknown => value
    }
    log.debug("Lookup tuple: %s", lookupType)
    setters.get((caller.getClass, field, lookupType)) match {
      case Some(setProxy) => Some(setProxy)
      case None => {
        try {
          log.debug("Creating new method mapping for setter %s on %s", field, lookupType)
          val setMethod = caller.getClass.getMethod("set%s".format(field.capitalize), lookupType)
          val setMethodProxy = setMethod.invoke _
          setters.update((caller.getClass, field, lookupType), setMethodProxy)
          Some(setMethodProxy)
        } catch {
          case nsmE => None // @todo this is probably a problem.  Compile time check?
        }
      }
    }

  }

}

/**
 * <code>ReflectiveBeanMapper</code> trait which provides an implementation of
 * the MongoDB <code>DBObject</code> which has scala-friendly support for sitting on top of a Javabean,
 * and utilising scala-like functionality.
 *
 * Instead of treating a <code>ReflectiveBeanMapper</code> like a Map-like object as you do with a
 * <code>BasicDBObject</code>, you can define Scala methods for setters and getters
 * (see the Tests for examples) which automatically can serialize/deserialize to Mongo DBObject format.
 *
 * At the moment, any non-mapped value will get placed in an 'other_fields' hashmap.  It will be fetchable
 * directly via 'get' but will NOT be included in <code>toMap</code> calls.
 *
 * @todo preload via attributes, currently only puts out values that have been set.
 *
 * @version 1.0
 * @author Brendan W. McAdams <bmcadams@novus.com>
 */
trait ReflectiveBeanMapper extends DBObject with Logging {
  import org.scala_tools.javautils.Implicits._
  private var _partial = false

  val other_fields = new scala.collection.mutable.HashMap[String, Any]

  def mongoID = other_fields.get("_id")
  def mongoNS = other_fields.get("_ns")

  /**
   * Define a getter for an OPTIONAL value (one for which you want to get Option[A] instead of
   * null in empty value cases)
   * @param field A string value indicating the fieldName for the getter (e.g. "foo" maps to "getFoo")
   * @param returnType A class of the type of the object you expect to be returned for Casting
   * @param A a type automatically picked up from the classtype of returnType
   */
  def optGetter[A](field: String, returnType: Class[A]): Option[A] = {
    val out = getter(field, returnType)
    if (out == null) None else Some(out.asInstanceOf[A])
  }

  def getter[A](field: String, returnType: Class[A]): A = {
    log.debug("Getter lookup trying for field %s returnType %s", field, returnType)
    ReflectiveBeanMapper(this, field) match {
      case Some(proxy) => {
        log.debug("Got back a getter %s", proxy)
        val ret = proxy(this)
        log.debug("Return value from getter invocation: %s", ret)
        ret.asInstanceOf[A]
      }
      case None => {
        log.trace("Unable to find defined getter for field " + field)
        // @todo - A is lost by erasure... put a manifest in here?
        if (other_fields.contains(field) && other_fields.get(field).isInstanceOf[A]) {
          other_fields.get(field) match {
            case Some(v) => v.asInstanceOf[A]
            case None => null.asInstanceOf[A]
          }
        }
        else {
          null.asInstanceOf[A]
        }
      }
    }
  }



  def optSetter[A](field: String, value: Option[A])(implicit m: Manifest[A]) {
    val in = value match {
      case None => null
      case Some(value) => value
    }
    setter(field, in.asInstanceOf[A])
  }

  def setter[A](field: String, value: A)(implicit m: Manifest[A]) {
    log.debug("Setter lookup trying for field %s value %s (Manifest erasure type %s)", field, value, m.erasure)
    val tVal = if (value.isInstanceOf[scala.BigDecimal]) value.asInstanceOf[scala.BigDecimal].bigDecimal else value
    ReflectiveBeanMapper(this, field, m.erasure) match {
      case Some(proxy) => {
        log.debug("Got back a setter %s", proxy)
        val ret = proxy(this, tVal.asInstanceOf[Object])
        log.debug("Return value from setter: %s", ret)
        ret
      }
      case None => {
        log.trace("Unable to find defined setter for field " + field)
        other_fields.update(field, value)
      }
    }
  }


  def dataSet = {

  }
  def toMap = {
    val dataMap =  new LinkedHashMap[String, AnyRef]()
    dataMap ++= ReflectiveBeanMapper.getters.filter(x => x._1._1 == this.getClass).map(x => x._1._2 -> x._2(this))
    dataMap.underlying
  }


  def removeField(key: String) = {
    val cur = get(key)
    for ((k, v) <- ReflectiveBeanMapper.setters.filter(x => x._1._1 == this.getClass && x._1._2 == key)) {
      v(this, null)
    }
    cur.asInstanceOf[Object]
  }

  def put(key: String, value: AnyRef) = {
    // @todo find a cleaner way to do this.
    try {
      val tVal = if (value.isInstanceOf[scala.BigDecimal]) value.asInstanceOf[scala.BigDecimal].bigDecimal else value
      ReflectiveBeanMapper(this, key, tVal.getClass) match {
        case Some(proxy) => {
          log.debug("Proxy Method: %s", proxy)
          proxy(this, tVal.asInstanceOf[Object])
        }
        case None => {
          log.trace("Unable to find defined setter for field " + key)
          other_fields.update(key, value)
        }
      }
    } catch {
      case x: NullPointerException => log.warning("Caught a NPE... Passing.")
    }
    value
  }

  def put[A](key: String, value: A)(implicit m: Manifest[A]) = {
    log.debug("Parametered put setting %s to %s[%s]", key, value, m.erasure)
    setter(key, value)
    value.asInstanceOf[Object]
  }


  def putAll(dbObj: DBObject): Unit =  putAll(dbObj.toMap)

  def putAll(fieldMap: Map[_,_]): Unit = {
    for ((k, v) <- fieldMap.asInstanceOf[Map[String, AnyRef]]) {
      try {
        put(k, v)
      } catch {
        case any => log.warning("Can't put %s with %s, failed... %s", k, v, any)
      }
    }
  }


  def get(key: String) = {
    ReflectiveBeanMapper(this, key) match {
      case Some(proxy) => {
        proxy(this)
      }
      case None => {
        log.warning("Unable to find defined getter for field " + key)
        if (other_fields.contains(key)) {
          other_fields.get(key) match {
            case Some(v) => v.asInstanceOf[AnyRef]
            case None => null
          }
        }
        else {
          null
        }
      }
    }
  }

  def containsKey(key: String): Boolean = containsField(key)

  def containsField(field: String): Boolean = ReflectiveBeanMapper.getters.contains((this.getClass, field))

  def keySet = {
    val dataSet = new HashSet[String]()
    for(n <- ReflectiveBeanMapper.getters) {
      log.debug("N: %s", n)
      if (n._1._1 == this.getClass) {
        log.debug("Class Match %s == %s", n._1._1, this.getClass)
        dataSet += n._1._2
      } else {
        log.debug("NO Class Match %s == %s", n._1._1, this.getClass)
      }
    }

    log.debug("DataSet: %s", dataSet)
    log.debug("Getters: %s", ReflectiveBeanMapper.getters)
    dataSet.underlying
  }


  def markAsPartialObject: Unit = _partial = true

  def isPartialObject: Boolean = _partial

}

