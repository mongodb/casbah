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

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.util.Logging

import com.mongodb.casbah.map_reduce.{MapReduceResult, MapReduceCommand}


import scalaj.collection.Imports._
import collection.mutable.ArrayBuffer


/**
 * Base trait for all MongoCollection wrapper objects.
 * Provides any non-parameterized methods and the basic structure.
 * Requires an underlying object of a DBCollection.
 *
 * @todo Copy the MongoDB docs over for the proxied methods.
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 */
trait MongoCollectionWrapper extends Logging {
  /**
   * The underlying Java Mongo Driver Collection object we proxy.
   */
  val underlying: com.mongodb.DBCollection
  implicit val db = underlying.getDB().asScala
  
  // there are two apply methods on the java api i've left out for now as they'll do whacky things to Scala probably.
  //def checkForIDIndex(key: DBObject) = underlying.checkForIDIndex(key)
  def createIndex(keys: DBObject) = underlying.createIndex(keys)
  def distinct(key: String) = underlying.distinct(key).asScala
  def distinct(key: String, query: DBObject) = underlying.distinct(key, query).asScala
  def dropCollection() = underlying.drop
  def dropIndex(keys: DBObject) = underlying.dropIndex(keys)
  def dropIndex(name: String) = underlying.dropIndex(name)
  def dropIndexes() = underlying.dropIndexes
  def dropIndexes(name: String) = underlying.dropIndexes
  //def ensureIDIndex() = underlying.ensureIDIndex
  def ensureIndex(keys: DBObject) = underlying.ensureIndex(keys)
  //def ensureIndex(keys: DBObject, force: Boolean) = underlying.ensureIndex(keys, force)
  //def ensureIndex(keys: DBObject, force: Boolean, unique: Boolean) = underlying.ensureIndex(keys, force, unique)
  def ensureIndex(keys: DBObject, name: String) = underlying.ensureIndex(keys, name)
  def ensureIndex(keys: DBObject, name: String, unique: Boolean) = underlying.ensureIndex(keys, name, unique)

  def getCollection(n: String) = underlying.getCollection(n)
  def getCount() = underlying.getCount()
  def getCount(query: DBObject) = underlying.getCount(query)
  def getCount(query: DBObject, fields: DBObject) = underlying.getCount(query, fields)
  /*def count = getCount()
  def count(query: DBObject) = getCount(query)
  def count(query: DBObject, fields: DBObject) = getCount(query, fields)*/
  def getDB() = underlying.getDB().asScala
  //def DB = getDB()
  def getFullName() = underlying.getFullName
  def fullName = getFullName()
  def getIndexInfo() = underlying.getIndexInfo.asScala
  def indexInfo = getIndexInfo()
  def getName() = underlying.getName
  def name = getName()
  def getObjectClass() = underlying.getObjectClass
  def objectClass = getObjectClass()
  def getWriteConcern() = underlying.getWriteConcern
  def writeConcern = getWriteConcern

  def group(key: DBObject, cond: DBObject, initial: DBObject, reduce: String) = {
    val result = underlying.group(key, cond, initial, reduce)   
    result.map(_._2.asInstanceOf[DBObject])
  }
  /**
   * Perform an absurdly simple grouping with no initial object or reduce function.
   */
  def group(key: DBObject, cond: DBObject): Iterable[DBObject] = group(key, cond, MongoDBObject.empty, "function(obj, prev) {}")
  def group(key: DBObject, cond: DBObject, function: String): Iterable[DBObject] = group(key, cond, MongoDBObject.empty, function)

  /**
   * Enables you to call group with the finalize parameter (a function that runs on each
   * row of the output for calculations before sending a return) which the Mongo Java driver does not yet
   * support, by sending a direct DBObject command.  Messy, but it works.
   */
  def group(key: DBObject, cond: DBObject, initial: DBObject, reduce: String, finalize: String) = {
    val cmdData = MongoDBObject(
      "ns" -> getName,
      "key" -> key,
      "cond" -> cond,
      "$reduce" -> reduce,
      "initial" -> initial,
      "finalize" -> finalize)
    log.trace("Executing group command: %s", cmdData)
    val result = getDB.command(MongoDBObject("group" -> cmdData))
    if (result.get("ok").asInstanceOf[Double] != 1) {
      log.warning("Group Statement Failed.")
    }
    log.trace("Group command result count : %s keys: %s ", result.get("count"), result.get("keys"))
    result.get("retval").asInstanceOf[DBObject].toMap.asScala.map(_._2.asInstanceOf[DBObject]).asInstanceOf[ArrayBuffer[DBObject]]
  }

  /** Emulates a SQL MAX() call ever so gently */
  def maxValue(field: String, condition: DBObject) = {
    val initial = MongoDBObject("max" -> "")
    val groupResult = group(MongoDBObject.empty,
          condition,
          initial,
          """
          function(obj, aggr) {
            if (aggr.max == '') {
              aggr.max = obj.%s;
            } else if (obj.%s > aggr.max) {
              aggr.max = obj.%s;
            }
          }""".format(field, field, field), "")
    log.trace("Max Grouping Result: %s", groupResult)
    groupResult.head.get("max").asInstanceOf[Double]
  }

  def maxDate(field: String, condition: DBObject) = {
    val initial = Map("max" -> "").asDBObject
    val groupResult = group(MongoDBObject.empty,
      condition,
      initial,
      """
      function(obj, aggr) {
        if (aggr.max == '') {
          aggr.max = obj.%s;
        } else if (obj.%s > aggr.max) {
          aggr.max = obj.%s;
        }
      }""".format(field, field, field), "")
    log.trace("Max Date Grouping Result: %s", groupResult)
    groupResult.head.get("max").asInstanceOf[java.util.Date]
  }
  def minDate(field: String, condition: DBObject) = {
    val initial = Map("max" -> "").asDBObject
    val groupResult = group(MongoDBObject.empty,
      condition,
      initial,
      """
      function(obj, aggr) {
        if (aggr.max == '') {
          aggr.max = obj.%s;
        } else if (obj.%s > aggr.max) {
          aggr.max = obj.%s;
        }
      }""".format(field, field, field), "")
    log.trace("Max Date Grouping Result: %s", groupResult)
    groupResult.head.get("max").asInstanceOf[java.util.Date]
  }
  /** Emulates a SQL MIN() call ever so gently */
  def minValue(field: String, condition: DBObject) = {
    val initial = Map("min" -> "").asDBObject
    group(MongoDBObject.empty,
          condition,
          initial,
          """
          function(obj, aggr) {
            if (aggr.min == '') {
              aggr.min = obj.%s;
            } else if (obj.%s < aggr.min) {
              aggr.min = obj.%s;
            }
           }""".format(field, field, field), "").
        head.get("min").asInstanceOf[Double]
  }

  /** Emulates a SQL AVG() call ever so gently */
  def avgValue(field: String, condition: DBObject) = {
    val initial = Map("count" -> 0, "total" -> 0, "avg" -> 0).asDBObject
    group(MongoDBObject.empty,
      condition,
      initial,
      """
      function(obj, aggr) {
        aggr.total += obj.%s;
        aggr.count += 1; 
      }
      """.format(field),
      "function(aggr) { aggr.avg = aggr.total / aggr.count }").head.get("avg").asInstanceOf[Double]
  }

  override def hashCode() = underlying.hashCode
  def insert(doc: DBObject) = underlying.insert(doc)
  def insert(doc: DBObject*) = underlying.insert(doc: _*)
  def insert(lst: List[DBObject]) = underlying.insert(lst.asJava)

  /**
   * The Java Driver is a bit outdated and is missing the finalize option.
   * Additionally, it returns ZERO information about the actual results of the mapreduce,
   * just a cursor to the result collection.
   * This is less than ideal.  So I've wrapped it in something more useful.
   * @deprecated Due to poor original design on my part, you should probably use the explicitly parameterized fversion of this on collection, or use the MapReduceCommand function on DB instead.
   * @param command An instance of MapReduceCommand representing the required MapReduce
   * @return MapReduceResult a wrapped result object.  This contains the returns success, counts etc, but implements iterator and can be iterated directly
   */
  def mapReduce(command: MapReduceCommand): MapReduceResult  = {
    val result = getDB.command(command.asDBObject)
    new MapReduceResult(result)
  }
  /**
   * The Java Driver is a bit outdated and is missing the finalize option.
   * Additionally, it returns ZERO information about the actual results of the mapreduce,
   * just a cursor to the result collection.
   * This is less than ideal.  So I've wrapped it in something more useful.
   *
   * @param command An instance of MapReduceCommand representing the required MapReduce
   * @return MapReduceResult a wrapped result object.  This contains the returns success, counts etc, but implements iterator and can be iterated directly
   */
  def mapReduce(mapFunction: JSFunction, reduceFunction: JSFunction, outputCollection: Option[String] = None,
                query: Option[DBObject] = None, sort: Option[DBObject] = None, finalizeFunction: Option[JSFunction] = None, 
                jsScope: Option[String] = None): MapReduceResult =  
              new MapReduceResult(getDB.command(MapReduceCommand(name, mapFunction, reduceFunction, 
                                                                 outputCollection, query, sort, finalizeFunction,
                                                                 jsScope).asDBObject))
  def remove(o: DBObject) = underlying.remove(o)
  def rename(newName: String) = underlying.rename(newName)
  def resetIndexCache() = underlying.resetIndexCache()
  def save(jo: DBObject) = underlying.save(jo)
  def setHintFields(lst: List[DBObject]) = underlying.setHintFields(lst.asJava)
  def setInternalClass(path: String, c: Class[_]) = underlying.setInternalClass(path, c)
  def setObjectClass[A <: DBObject : Manifest](c: Class[A]) = {
    underlying.setObjectClass(c)
    new MongoTypedCollection[A](underlying)
  }
  def setWriteConcern(concern: com.mongodb.WriteConcern) = underlying.setWriteConcern(concern)
  override def toString() = underlying.toString
  def update(q: DBObject, o: DBObject) = underlying.update(q, o)
  def update(q: DBObject, o: DBObject, upsert: Boolean, multi: Boolean) = underlying.update(q, o, upsert, multi)
  def updateMulti(q: DBObject, o: DBObject) = underlying.updateMulti(q, o)
  override def equals(obj: Any) = obj match {
    case other: MongoCollectionWrapper => underlying.equals(other.underlying)
    case _ => false
  }

  def count = getCount
  def count(query: DBObject) = getCount(query)
  def count(query: DBObject, fields: DBObject) = getCount(query, fields)

  def lastError = underlying.getDB.getLastError

  /**
   * MongoDB DB collection.save method
   *
   * @author Alexander Azarov <azarov@osinka.ru>
   * 
   * @param x object to save to the collection
   */
  def +=[A <% DBObject : Manifest](x: A)  = save(x)

  /**
   * MongoDB DBCollection.remove method
   *
   * @author Alexander Azarov <azarov@osinka.ru>
   * 
   * @param x object to remove from the collection
   */
  def -=[A <% DBObject : Manifest](x: A) = remove(x)

  /**
   * Helper method for anyone who returns an Option
   * to quickly wrap their dbObject, determining null
   * to swap as None
   *
   */
  def optWrap[A <% DBObject](obj: A): Option[A] = {
    if (obj == null) None else Some(obj)
  }
}

/**
 * A Non-Generic, DBObject returning implementation of the <code>ScalaMongoCollectionWrapper</code>
 * Which implements Iterable, to allow iterating directly over it to list all of the underlying objects.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 *
 * @param underlying DBCollection object to proxy
 */
class MongoCollection(val underlying: com.mongodb.DBCollection) extends MongoCollectionWrapper with Iterable[DBObject] {

/*  def this(coll: DBCollection) = {
    this()
    underlying = coll
  }*/

  override def elements: MongoCursor  = find
  override def iterator: MongoCursor  = find
  def find() = underlying.find.asScala
  def find(ref: DBObject) = underlying.find(ref) asScala
  def find(ref: DBObject, keys: DBObject) = underlying.find(ref, keys) asScala
  def find(ref: DBObject, fields: DBObject, numToSkip: Int, batchSize: Int) = underlying.find(ref, fields, numToSkip, batchSize) asScala
  def findOne(): Option[DBObject] = optWrap(underlying.findOne())
  def findOne(o: DBObject): Option[DBObject] = optWrap(underlying.findOne(o))
  def findOne(o: DBObject, fields: DBObject): Option[DBObject] = optWrap(underlying.findOne(o, fields))
  def findOneView[A <% DBObject : Manifest](o: A) = optWrap(underlying.findOne(o))
  def findOneView[A <% DBObject : Manifest, B <% DBObject : Manifest](o: A, fields: B) = 
    optWrap(underlying.findOne(o, fields))
  /**
   * Finds an object by its id. This compares the passed in value to the _id field of the document
   * It also serves to totally SCREW anyone trying to use context/view bounds of DBObject ;)
   * I've put some hackery in to try to detect possible conversions....
   */
  def findOne(obj: Object): Option[DBObject] = obj match {
    case dbobj: MongoDBObject => {
      log.debug("View convertable[mongodbobject] - rerouting.")
      findOne(dbobj.asDBObject)
    }
    case map: Map[String, Any] => {
      log.debug("View convertable[map]- rerouting.")
      findOne(map.asDBObject)
    }
    case prod: Product => {
      log.debug("View convertable[product] - rerouting.")
      findOne(prod.asDBObject)
    }
    case _ => optWrap(underlying.findOne(obj))
  }
  /**
   * Finds an object by its id. This compares the passed in value to the _id field of the document
   * It also serves to totally SCREW anyone trying to use context/view bounds of DBObject ;)
   */
  def findOne(obj: Object, fields: DBObject): Option[DBObject] =  obj match {

    case dbobj: MongoDBObject => {
      log.debug("View convertable[mongodbobject] - rerouting.")
      findOneView(dbobj.asDBObject, fields)
    }
    case map: Map[String, Any] => {
      log.debug("View convertable[map]- rerouting.")
      findOneView(map.asDBObject, fields)
    }
    case prod: Product => {
      log.debug("View convertable[product] - rerouting.")
      findOneView(prod.asDBObject, fields)
    }
     case _ => optWrap(underlying.findOne(obj, fields))
   }
  override def head = headOption.get
  override def headOption = findOne
  override def tail = find.skip(1).toList


  /**
   * write concern aware write op block.
   *
   * Guarantees that the operations in the passed
   * block are executed in the same connection
   * via requestStart() and requestDone().
   * 
   * Calls &amp; throws getLastError afterwards,
   * so if you run multiple ops you'll only get the final 
   * error.
   * 
   * Your op function gets a copy of this MongoDB.
   * 
   * This is for update ops only - you cannot return data from it.
   * 
   * 
   * @throws MongoException
   */
  def request(op: MongoCollection => Unit) = getDB.request(db => op(db(name)))

}

/**
 * A Generic, parameterized DBObject-subclass returning implementation of the <code>ScalaMongoCollectionWrapper</code>
 * This is instantiated with a type (and an implicitly discovered or explicitly passed Manifest object) to determine it's underlying type.
 *
 * It will attempt to deserialize *ALL* returned results (except for things like group and mapReduce which don't return collection objects)
 * to it's type, on the assumption that the collection matches the type's spec.
 *
 *
 * implements Iterable, to allow iterating directly over it to list all of the underlying objects.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 *
 * @param A  type representing a DBObject subclass which this class should return instead of generic DBObjects
 * @param underlying DBCollection object to proxy
 * @param m Manifest[A] representing the erasure for the underlying type - used to get around the JVM's insanity
 */
class MongoTypedCollection[A <: DBObject : Manifest](val underlying: com.mongodb.DBCollection) extends Iterable[A] 
                                                                                       with MongoCollectionWrapper {
                                                                                     
  type UnderlyingObj = A
  val m = manifest[A]
  log.debug("Manifest erasure: " + m.erasure)
  underlying.setObjectClass(m.erasure)
  /*def this(coll: DBCollection)(implicit m: scala.reflect.Manifest[A]) = {
    this()
    println("Manifest erasure: " + m.erasure)
    underlying = coll
    underlying.setObjectClass(m.erasure)
  }*/

  override def elements = find
  override def iterator = find
  //override def setObjectClass[A](c: Class[A]) = this
  def find() = underlying.find.asScalaTyped
  def find(ref: DBObject) = underlying.find(ref) asScalaTyped
  def find(ref: DBObject, keys: DBObject) = underlying.find(ref, keys) asScalaTyped
  def findOne() = optWrap(underlying.findOne().asInstanceOf[A])
  def findOne(o: DBObject) = optWrap(underlying.findOne(o).asInstanceOf[A])
  def findOne(o: DBObject, fields: DBObject) = optWrap(underlying.findOne(o, fields).asInstanceOf[A])
  def findOne(obj: Object) = optWrap(underlying.findOne(obj).asInstanceOf[A])
  def findOne(obj: Object, fields: DBObject) = optWrap(underlying.findOne(obj, fields).asInstanceOf[A])
  //override def find(ref: DBObject, fields: DBObject, numToSkip: Int, batchSize: Int): ScalaTypedMongoCursor[A] = underlying.find(ref, fields, numToSkip, batchSize) asScalaTyped
  //override def find(ref: DBObject, fields: DBObject, numToSkip: Int, batchSize: Int, options: Int) = underlying.find(ref, fields, numToSkip, batchSize, options) asScalaTyped
  override def head = findOne.get
  override def headOption = Some(findOne.get.asInstanceOf[A])
  override def tail = find.skip(1).map(_.asInstanceOf[A]).toList

}
