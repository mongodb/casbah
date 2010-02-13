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
import com.novus.util.Logging
import map_reduce.{MapReduceResult, MapReduceCommand}
import org.scala_tools.javautils.Imports._
import Implicits._
import collection.mutable.ArrayBuffer

trait ScalaMongoCollectionWrapper extends Logging {
  val underlying: DBCollection
  implicit val db = underlying.getDB().asScala
  // there are two apply methods on the java api i've left out for now
  def checkForIDIndex(key: DBObject) = underlying.checkForIDIndex(key)
  def createIndex(keys: DBObject) = underlying.createIndex(keys)
  def distinct(key: String) = underlying.distinct(key).asScala
  def distinct(key: String, query: DBObject) = underlying.distinct(key, query).asScala
  def drop() = underlying.drop
  def dropIndex(keys: DBObject) = underlying.dropIndex(keys)
  def dropIndex(name: String) = underlying.dropIndex(name)
  def dropIndexes() = underlying.dropIndexes
  def dropIndexes(name: String) = underlying.dropIndexes
  def ensureIDIndex() = underlying.ensureIDIndex
  def ensureIndex(keys: DBObject) = underlying.ensureIndex(keys)
  def ensureIndex(keys: DBObject, force: Boolean) = underlying.ensureIndex(keys, force)
  def ensureIndex(keys: DBObject, force: Boolean, unique: Boolean) = underlying.ensureIndex(keys, force, unique)
  def ensureIndex(keys: DBObject, name: String) = underlying.ensureIndex(keys, name)
  def ensureIndex(keys: DBObject, name: String, unique: Boolean) = underlying.ensureIndex(keys, name, unique)

  //def find(ref: DBObject, fields: DBObject, numToSkip: Int, batchSize: Int, options: Int) = underlying.find(ref, fields, numToSkip, batchSize, options) asScala

  def getCollection(n: String) = underlying.getCollection(n)
  def getCount() = underlying.getCount()
  def getCount(query: DBObject) = underlying.getCount(query)
  def getCount(query: DBObject, fields: DBObject) = underlying.getCount(query, fields)
  def getDB() = underlying.getDB().asScala
  def getFullName() = underlying.getFullName
  def getIndexInfo() = underlying.getIndexInfo.asScala
  def getName() = underlying.getName
  def getObjectClass() = underlying.getObjectClass
  def getWriteConcern() = underlying.getWriteConcern
  def group(key: DBObject, cond: DBObject, initial: DBObject, reduce: String) = {
    val result = underlying.group(key, cond, initial, reduce).toMap.asScala
    result.map(_._2.asInstanceOf[DBObject]).asInstanceOf[ArrayBuffer[DBObject]]
  }
  /**
   * Perform an absurdly simple grouping with no initial object or reduce function.
   */
  def group(key: DBObject, cond: DBObject): ArrayBuffer[DBObject] = group(key, cond, new BasicDBObject, "function(obj, prev) {}")
  def group(key: DBObject, cond: DBObject, function: String): ArrayBuffer[DBObject] = group(key, cond, new BasicDBObject, function)

  /**
   * Enables you to call group with the finalize parameter (a function that runs on each
   * row of the output for calculations before sending a return) which the Mongo Java driver does not yet
   * support, by sending a direct DBObject command.  Messy, but it works.
   */
  def group(key: DBObject, cond: DBObject, initial: DBObject, reduce: String, finalize: String) = {
    val cmdData = Map[String, Any](
      ("ns" -> getName),
      ("key" -> key),
      ("cond" -> cond),
      ("$reduce" -> reduce),
      ("initial" -> initial),
      ("finalize", finalize)).asDBObject
    val result = getDB.command(BasicDBObjectBuilder.start("group", cmdData).get)
    if (result.get("ok").asInstanceOf[Double] != 1) {
      log.warning("Group Statement Failed.")
    }
    log.info("Group command result count : %s keys: %s ", result.get("count"), result.get("keys"))
    result.get("retval").asInstanceOf[DBObject].toMap.asScala.map(_._2.asInstanceOf[DBObject]).asInstanceOf[ArrayBuffer[DBObject]]
  }
  override def hashCode() = underlying.hashCode
  def insert(doc: DBObject) = underlying.insert(doc)
  def insert(doc: Array[DBObject]) = underlying.insert(doc)
  def insert(lst: List[DBObject]) = underlying.insert(lst.asJava)
  //def mapReduce(command: DBObject) = underlying.mapReduce(command)
  //def mapReduce(map: String, reduce: String, outputCollection: String, query: DBObject) = underlying.mapReduce(map, reduce, outputCollection, query)

  /**
   * The Java Driver is a bit outdated and is missing the finalize option.
   * Additionally, it returns ZERO information about the actual results of the mapreduce,
   * just a cursor to the result collection.
   * This is less than ideal.  So I've wrapped it in something more useful.
   */
  def mapReduce(command: MapReduceCommand): MapReduceResult  = {
    val result = getDB.command(command.toDBObj)
    new MapReduceResult(result)
  }
  def remove(o: DBObject) = underlying.remove(o)
  def rename(newName: String) = underlying.rename(newName)
  def resetIndexCache() = underlying.resetIndexCache()
  def save(jo: DBObject) = underlying.save(jo)
  def setHintFields(lst: List[DBObject]) = underlying.setHintFields(lst.asJava)
  def setInternalClass(path: String, c: Class[_]) = underlying.setInternalClass(path, c)
  def setObjectClass[A <: DBObject](c: Class[A])(implicit m: scala.reflect.Manifest[A]) = {
    underlying.setObjectClass(c)
    new ScalaTypedMongoCollection[A](underlying)
  }
  def setWriteConcern(concern: DB.WriteConcern) = underlying.setWriteConcern(concern)
  override def toString() = underlying.toString
  def update(q: DBObject, o: DBObject) = underlying.update(q, o)
  def update(q: DBObject, o: DBObject, upsert: Boolean, multi: Boolean) = underlying.update(q, o, upsert, multi)
  def updateMulti(q: DBObject, o: DBObject) = underlying.updateMulti(q, o)
  override def equals(obj: Any) = obj match {
    case other: ScalaMongoCollectionWrapper => underlying.equals(other.underlying)
    case _ => false
  }

  def count() = getCount
  def count(query: DBObject) = getCount(query)
  def count(query: DBObject, fields: DBObject) = getCount(query, fields)

  def size = count

  def lastError = underlying.getDB.getLastError

  /**
   * MongoDB <code>insert</code> method
   * @param x object to insert into the collection
   */
  def <<[A <: DBObject](x: A) =  insert(x)

  /**
   * MongoDB <code>insert</code> with subsequent check for object existence
   * @param x object to insert into the collection
   * @return <code>None</code> if such object exists already (with the same identity)
   * <code>Some(x)</code> in the case of success
   */
  def <<?[A <: DBObject](x: A): Option[A] = {
    insert(x)
    lastError get "err" match {
      case null => Some(x)
      case msg: String => None
    }
  }

  /**
   * MongoDB DB collection.save method
   * @param x object to save to the collection
   */
  def +=[A <: DBObject](x: A)  = save(x)

  /**
   * MongoDB DBCollection.remove method
   * @param x object to remove from the collection
   */
  def -=[A <: DBObject](x: A) = remove(x)

  def optWrap[A <: DBObject](obj: A): Option[A] = {
    if (obj == null) None else Some(obj)
  }
}

class ScalaMongoCollection(val underlying: DBCollection) extends ScalaMongoCollectionWrapper with Iterable[DBObject] {

/*  def this(coll: DBCollection) = {
    this()
    underlying = coll
  }*/

  def elements: ScalaMongoCursor  = find
  def find() = underlying.find.asScala
  def find(ref: DBObject) = underlying.find(ref) asScala
  def find(ref: DBObject, keys: DBObject) = underlying.find(ref, keys) asScala
  def find(ref: DBObject, fields: DBObject, numToSkip: Int, batchSize: Int) = underlying.find(ref, fields, numToSkip, batchSize) asScala
  def findOne() = optWrap(underlying.findOne())
  def findOne(o: DBObject) = optWrap(underlying.findOne(o))
  def findOne(o: DBObject, fields: DBObject) = optWrap(underlying.findOne(o, fields))
  def findOne(obj: Object) = optWrap(underlying.findOne(obj))
  def findOne(obj: Object, fields: DBObject) = optWrap(underlying.findOne(obj, fields))
  def head = headOption.get
  def headOption = findOne
  def tail = find.skip(1).toArray.toList
}

class ScalaTypedMongoCollection[A <: DBObject](val underlying: DBCollection)(implicit m: scala.reflect.Manifest[A]) extends Iterable[A] with ScalaMongoCollectionWrapper {
  type UnderlyingObj = A

  println("Manifest erasure: " + m.erasure)
  underlying.setObjectClass(m.erasure)
  /*def this(coll: DBCollection)(implicit m: scala.reflect.Manifest[A]) = {
    this()
    println("Manifest erasure: " + m.erasure)
    underlying = coll
    underlying.setObjectClass(m.erasure)
  }*/

  def elements = find
  //override def setObjectClass[A](c: Class[A]) = this
  def find() = underlying.find.asScalaTyped(m)
  def find(ref: DBObject) = underlying.find(ref) asScalaTyped(m)
  def find(ref: DBObject, keys: DBObject) = underlying.find(ref, keys) asScalaTyped(m)
  def findOne() = optWrap(underlying.findOne().asInstanceOf[A])
  def findOne(o: DBObject) = optWrap(underlying.findOne(o).asInstanceOf[A])
  def findOne(o: DBObject, fields: DBObject) = optWrap(underlying.findOne(o, fields).asInstanceOf[A])
  def findOne(obj: Object) = optWrap(underlying.findOne(obj).asInstanceOf[A])
  def findOne(obj: Object, fields: DBObject) = optWrap(underlying.findOne(obj, fields).asInstanceOf[A])
  //override def find(ref: DBObject, fields: DBObject, numToSkip: Int, batchSize: Int): ScalaTypedMongoCursor[A] = underlying.find(ref, fields, numToSkip, batchSize) asScalaTyped
  //override def find(ref: DBObject, fields: DBObject, numToSkip: Int, batchSize: Int, options: Int) = underlying.find(ref, fields, numToSkip, batchSize, options) asScalaTyped
  def head = findOne.get
  def headOption = Some(findOne.get.asInstanceOf[A])
  def tail = find.skip(1).map(_.asInstanceOf[A]).toList

}