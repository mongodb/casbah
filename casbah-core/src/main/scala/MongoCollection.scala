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
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
trait MongoCollectionWrapper extends Logging {
  /**
   * The underlying Java Mongo Driver Collection object we proxy.
   */
  val underlying: com.mongodb.DBCollection
  /** Returns the database this collection is a member of.
   * @return this collection's database
   */
  implicit val db = underlying.getDB().asScala
  
  /** Adds the "private" fields _id to an object.
   * @param o <code>DBObject</code> to which to add fields
   * @return the modified parameter object
   */
  def apply[A <% DBObject : Manifest](o: A) = underlying.apply(o)

  /** Adds the "private" fields _id to an object.
   * @param jo object to which to add fields
   * @param ensureID whether to add an <code>_id</code> field or not
   * @return the modified object <code>o</code>
   */
  def apply[A <% DBObject : Manifest](jo: A, ensureID: Boolean) = underlying.apply(jo, ensureID)

  /** Forces creation of an index on a set of fields, if one does not already exist.
   * @param keys an object with a key set of the fields desired for the index
   */
  def createIndex[A <% DBObject : Manifest](keys: A) = underlying.createIndex(keys)
  
  def createIndex[A <% DBObject : Manifest, B <% DBObject : Manifest](keys: A, options: B) = 
    underlying.createIndex(keys, options)

  /**
   * find distinct values for a key
   */
  def distinct(key: String) = underlying.distinct(key).asScala

  /**
   * find distinct values for a key
   * @param query query to apply on collection
   */
  def distinct[A <% DBObject : Manifest](key: String, query: A) = 
    underlying.distinct(key, query).asScala

  /** Drops (deletes) this collection
   */
  def drop() = underlying.drop
  /** Drops (deletes) this collection
   */
  def dropCollection() = underlying.drop

  def dropIndex[A <% DBObject : Manifest](keys: A) = 
    underlying.dropIndex(keys)
  def dropIndex(name: String) = underlying.dropIndex(name)
  /** Drops all indices from this collection
   */
  def dropIndexes() = underlying.dropIndexes
  def dropIndexes(name: String) = underlying.dropIndexes

  /** Creates an index on a set of fields, if one does not already exist.
   * @param keys an object with a key set of the fields desired for the index
   */
  def ensureIndex[A <% DBObject : Manifest](keys: A) = 
    underlying.ensureIndex(keys)
  /** Ensures an index on this collection (that is, the index will be created if it does not exist).
   * ensureIndex is optimized and is inexpensive if the index already exists.
   * @param keys fields to use for index
   * @param name an identifier for the index
   * @dochub indexes
   */
  def ensureIndex[A <% DBObject : Manifest](keys: A, name: String) = underlying.ensureIndex(keys, name)
  /** Ensures an optionally unique index on this collection.
   * @param keys fields to use for index
   * @param name an identifier for the index
   * @param unique if the index should be unique
   */
  def ensureIndex[A <% DBObject : Manifest](keys: A, name: String, unique: Boolean) = underlying.ensureIndex(keys, name, unique)
  def ensureIndex(name: String) = underlying.ensureIndex(name)

  /** Find a collection that is prefixed with this collection's name.
   * A typical use of this might be 
   * <blockquote><pre>
   *    DBCollection users = mongo.getCollection( "wiki" ).getCollection( "users" );
   * </pre></blockquote>
   * Which is equilalent to
   * <pre><blockquote>
   *   DBCollection users = mongo.getCollection( "wiki.users" );
   * </pre></blockquote>
   * @param n the name of the collection to find
   * @return the matching collection
   */
  def getCollection(n: String) = underlying.getCollection(n)


  /**
   *  Returns the number of documents in the collection
   *  @return number of documents that match query
   */
  def getCount() = underlying.getCount()
  /**
   *  Returns the number of documents in the collection
   *  that match the specified query
   *
   *  @param query query to select documents to count
   *  @return number of documents that match query
   */
  def getCount[A <% DBObject : Manifest](query: A) = underlying.getCount(query)
  /**
   *  Returns the number of documents in the collection
   *  that match the specified query
   *
   *  @param query query to select documents to count
   *  @param fields fields to return
   *  @return number of documents that match query and fields
   */
  def getCount[A <% DBObject : Manifest, B<% DBObject : Manifest](query: A, fields: B) = 
    underlying.getCount(query, fields)

  /**
   *  Returns the number of documents in the collection
   *  that match the specified query
   *
   *  @param query query to select documents to count
   *  @param fields fields to return
   *  @param limit Max # of fields
   *  @param skip # of fields to skip
   *  @return number of documents that match query and fields
   */
  def getCount[A <% DBObject : Manifest, B<% DBObject : Manifest](query: A, fields: B, limit: Long, skip: Long) = 
    underlying.getCount(query, fields, limit, skip)

  /** Returns the database this collection is a member of.
   * @return this collection's database
   */
  def getDB() = underlying.getDB().asScala
  
  /** Returns the full name of this collection, with the database name as a prefix.
   * @return  the name of this collection
   */
  def getFullName() = underlying.getFullName
  /** Returns the full name of this collection, with the database name as a prefix.
   * @return  the name of this collection
   */
  def fullName = getFullName()
  /**
   *   Return a list of the indexes for this collection.  Each object
   *   in the list is the "info document" from MongoDB
   *
   *   @return list of index documents
   */
  def getIndexInfo() = underlying.getIndexInfo.asScala
  /**
   *   Return a list of the indexes for this collection.  Each object
   *   in the list is the "info document" from MongoDB
   *
   *   @return list of index documents
   */
  def indexInfo = getIndexInfo()

  def getName() = underlying.getName
  def name = getName()
  /** Gets the default class for objects in the collection
   * @return the class
   */
  def getObjectClass() = underlying.getObjectClass
  /** Gets the default class for objects in the collection
   * @return the class
   */
  def objectClass = getObjectClass()

  def stats = getStats()
  def getStats() = underlying.getStats()

  def group[A <% DBObject : Manifest, B <% DBObject : Manifest, C <% DBObject : Manifest](key: A, cond: B, initial: C, reduce: String) = {
    val result = underlying.group(key, cond, initial, reduce)   
    result.map(_._2.asInstanceOf[DBObject])
  }
  /**
   * Perform an absurdly simple grouping with no initial object or reduce function.
   */
  def group[A <% DBObject : Manifest, B <% DBObject : Manifest](key: A, cond: B): Iterable[DBObject] = group(key, cond, MongoDBObject.empty, "function(obj, prev) {}")
  def group[A <% DBObject : Manifest, B <% DBObject : Manifest](key: A, cond: B, function: String): Iterable[DBObject] = group(key, cond, MongoDBObject.empty, function)

  /**
   * Enables you to call group with the finalize parameter (a function that runs on each
   * row of the output for calculations before sending a return) which the Mongo Java driver does not yet
   * support, by sending a direct DBObject command.  Messy, but it works.
   */
  def group[A <% DBObject : Manifest, B <% DBObject : Manifest, C <% DBObject : Manifest](key: A, cond: B, initial: C, reduce: String, finalize: String) = {
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
  def maxValue[A <% DBObject : Manifest](field: String, condition: A) = {
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

  def maxDate[A <% DBObject : Manifest](field: String, condition: A) = {
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
  def minDate[A <% DBObject : Manifest](field: String, condition: A) = {
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
  def minValue[A <% DBObject : Manifest](field: String, condition: A) = {
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
  def avgValue[A <% DBObject : Manifest](field: String, condition: A) = {
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

  /**
   * Saves document(s) to the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param arr  array of documents to save
   * @dochub insert
   */
  def insert[A <% DBObject : Manifest](docs: A*) = {
    val b = new scala.collection.mutable.ArrayBuilder.ofRef[DBObject]
    b.sizeHint(docs.size)
    for (x <- docs) b += x
    underlying.insert(b.result: _*)
   }

  /**
   * Saves document(s) to the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param arr  array of documents to save
   * @dochub insert
   */
  def insert[A <: Array[B] : Manifest, B <% DBObject : Manifest](docs: A, writeConcern: WriteConcern) = {
    val b = new scala.collection.mutable.ArrayBuilder.ofRef[DBObject]
    b.sizeHint(docs.size)
    for (x <- docs) b += x
    underlying.insert(b.result, writeConcern)
  }


  /**
   * Inserts a document into the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param arr  array of documents to save
   * @dochub insert
   */
  def insert[A <% DBObject : Manifest](doc: A, writeConcern: WriteConcern) = 
    underlying.insert(doc, writeConcern)

  /**
   * Saves document(s) to the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param list list of documents to save
   * @dochub insert
   */
  def insert[A <% DBObject : Manifest](docs: List[A]) = {
    val b = List.newBuilder[DBObject]
    for (x <- docs) b += x
    underlying.insert(b.result.asJava)
  }


  def isCapped = underlying.isCapped()

  /**
   * @deprecated Due to poor original design on my part, you should probably use the explicitly parameterized fversion of this on collection   * @param command An instance of MapReduceCommand representing the required MapReduce
   * @return MapReduceResult a wrapped result object.  This contains the returns success, counts etc, but implements iterator and can be iterated directly
   */
  def mapReduce(command: MapReduceCommand): MapReduceResult  = {
    val result = getDB.command(command.asDBObject)
    new MapReduceResult(result)
  }
  /**
   *
   * @param command An instance of MapReduceCommand representing the required MapReduce
   * @return MapReduceResult a wrapped result object.  This contains the returns success, counts etc, but implements iterator and can be iterated directly
   */
  def mapReduce(mapFunction: JSFunction, 
                reduceFunction: JSFunction, 
                outputCollection: Option[String] = None,
                query: Option[DBObject] = None, 
                sort: Option[DBObject] = None, 
                finalizeFunction: Option[JSFunction] = None, 
                jsScope: Option[String] = None): MapReduceResult =  
              new MapReduceResult(getDB.command(MapReduceCommand(name, mapFunction, reduceFunction, 
                                                                 outputCollection, query, sort, finalizeFunction,
                                                                 jsScope).asDBObject))
  /** Removes objects from the database collection.
   * @param o the object that documents to be removed must match
   * @dochub remove
   */
  def remove[A <% DBObject : Manifest](o: A) = underlying.remove(o)

  /** Removes objects from the database collection.
   * @param o the object that documents to be removed must match
   * @param concern WriteConcern for this operation
   * @dochub remove
   */
  def remove[A <% DBObject : Manifest](o: A, writeConcern: WriteConcern) = 
    underlying.remove(o, writeConcern)

  /**
   * does a rename of this collection to newName
   * @param newName new collection name (not a full namespace)
   * @return the new collection
   */
  def rename(newName: String) = underlying.rename(newName)

  /** Clears all indices that have not yet been applied to this collection. */
  def resetIndexCache() = underlying.resetIndexCache()

  /** Saves an object to this collection.
   * @param jo the <code>DBObject</code> to save
   *        will add <code>_id</code> field to jo if needed
   */
  def save[A <% DBObject : Manifest](jo: A) = underlying.save(jo)

  /** Saves an object to this collection.
   * @param jo the <code>DBObject</code> to save
   *        will add <code>_id</code> field to jo if needed
   */
  def save[A <% DBObject : Manifest](jo: A, writeConcern: WriteConcern) = underlying.save(jo, writeConcern)

  /** Set hint fields for this collection.
   * @param lst a list of <code>DBObject</code>s to be used as hints
   */
  def setHintFields[A <% DBObject : Manifest](docs: List[A]) = {
    val b = List.newBuilder[DBObject]
    for (x <- docs) b += x
    underlying.setHintFields(b.result.asJava)
  }

  def setInternalClass(path: String, c: Class[_]) = underlying.setInternalClass(path, c)

  override def toString() = underlying.toString

 /**
   * @dochub update
   */
  def update[A <% DBObject : Manifest, B <% DBObject : Manifest](q: A, o: B) = underlying.update(q, o)
  /**
   * Performs an update operation.
   * @param q search query for old object to update
   * @param o object with which to update <tt>q</tt>
   * @param upsert if the database should create the element if it does not exist
   * @param multi if the update should be applied to all objects matching (db version 1.1.3 and above)
   *              See http://www.mongodb.org/display/DOCS/Atomic+Operations
   * @dochub update
   */
  def update[A <% DBObject : Manifest, B <% DBObject : Manifest](q: A, o: B, upsert: Boolean, multi: Boolean) = underlying.update(q, o, upsert, multi)
  /**
   * Performs an update operation.
   * @param q search query for old object to update
   * @param o object with which to update <tt>q</tt>
   * @param upsert if the database should create the element if it does not exist
   * @param multi if the update should be applied to all objects matching (db version 1.1.3 and above)
   *              See http://www.mongodb.org/display/DOCS/Atomic+Operations
   * @param writeConcern WriteConcern for this operation
   * @dochub update
   */
  def update[A <% DBObject : Manifest, B <% DBObject : Manifest](q: A, o: B, upsert: Boolean, multi: Boolean, writeConcern: WriteConcern) =
    underlying.update(q, o, upsert, multi, writeConcern)

 /**
   * @dochub update
   */
  def updateMulti[A <% DBObject : Manifest, B <% DBObject : Manifest](q: A, o: B) = underlying.updateMulti(q, o)

  /** Checks if this collection is equal to another object.
   * @param o object with which to compare this collection
   * @return if the two collections are the same object
   */
  override def equals(obj: Any) = obj match {
    case other: MongoCollectionWrapper => underlying.equals(other.underlying)
    case _ => false
  }

  def count = getCount
  def count[A <% DBObject : Manifest](query: A) = getCount(query)
  def count[A <% DBObject : Manifest, B <% DBObject : Manifest](query: A, fields: B) = getCount(query, fields)

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



  /**
   * 
   * Set the write concern for this database.
   * Will be used for writes to any collection in this database.
   * See the documentation for {@link WriteConcern} for more info.
   * 
   * @param concern (WriteConcern) The write concern to use
   * @see WriteConcern 
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def setWriteConcern(concern: WriteConcern) = underlying.setWriteConcern(concern)

  /**
   * 
   * Set the write concern for this database.
   * Will be used for writes to any collection in this database.
   * See the documentation for {@link WriteConcern} for more info.
   * 
   * @param concern (WriteConcern) The write concern to use
   * @see WriteConcern 
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def writeConcern_=(concern: WriteConcern) = setWriteConcern(concern)

  /**
   * 
   * get the write concern for this database,
   * which is used for writes to any collection in this database.
   * See the documentation for {@link WriteConcern} for more info.
   * 
   * @see WriteConcern 
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def getWriteConcern() = underlying.getWriteConcern()

  /**
   * 
   * get the write concern for this database,
   * which is used for writes to any collection in this database.
   * See the documentation for {@link WriteConcern} for more info.
   *
   * @see WriteConcern 
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def writeConcern = getWriteConcern

  /** 
   * Manipulate Network Options
   * 
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def addOption(option: Int) = underlying.addOption(option)
  /** 
   * Manipulate Network Options
   * 
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def resetOptions() = underlying.resetOptions() // use parens because this side-effects

  /** 
   * Manipulate Network Options
   * 
   * @see com.mongodb.Mongo
   * @see com.mognodb.Bytes
   */
  def getOptions() = underlying.getOptions

  /** 
   * Manipulate Network Options
   * 
   * @see com.mongodb.Mongo
   * @see com.mognodb.Bytes
   */
  def options = getOptions
  /** 
   * Sets queries to be OK to run on slave nodes.
   */
  def slaveOk() = underlying.slaveOk()   // use parens because this side-effects

      
}

/**
 * A Non-Generic, DBObject returning implementation of the <code>ScalaMongoCollectionWrapper</code>
 * Which implements Iterable, to allow iterating directly over it to list all of the underlying objects.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 *
 * @param underlying DBCollection object to proxy
 */
class MongoCollection(val underlying: com.mongodb.DBCollection)
    extends MongoCollectionWrapper 
    with Iterable[DBObject] {

  override def elements: MongoCursor  = find
  override def iterator: MongoCursor  = find

  /** Queries for all objects in this collection. 
   * @return a cursor which will iterate over every object
   * @dochub find
   */
  def find() = underlying.find.asScala
  /** Queries for an object in this collection.
   * @param ref object for which to search
   * @return an iterator over the results
   * @dochub find
   */
  def find(ref: DBObject) = underlying.find(ref) asScala
  /** Queries for an object in this collection.
   *
   * <p>
   * An empty DBObject will match every document in the collection.
   * Regardless of fields specified, the _id fields are always returned.
   * </p>
   * <p>
   * An example that returns the "x" and "_id" fields for every document 
   * in the collection that has an "x" field:
   * </p>
   * <blockquote><pre>
   * BasicDBObject keys = new BasicDBObject();
   * keys.put("x", 1);
   *
   * DBCursor cursor = collection.find(new BasicDBObject(), keys); 
   * </pre></blockquote>
   *
   * @param ref object for which to search
   * @param keys fields to return
   * @return a cursor to iterate over results
   * @dochub find
   */
  def find(ref: DBObject, keys: DBObject) = underlying.find(ref, keys) asScala
  /** Finds an object.
   * @param ref query used to search
   * @param fields the fields of matching objects to return
   * @param numToSkip will not return the first <tt>numToSkip</tt> matches
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  all objects that match will be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or the # that fit in a batch
   * @param options - see Bytes QUERYOPTION_*
   * @return the objects, if found
   * @dochub find
   */
  def find(ref: DBObject, fields: DBObject, numToSkip: Int, batchSize: Int) = underlying.find(ref, fields, numToSkip, batchSize) asScala
  /** 
   * Returns a single object from this collection.
   * @return the object found, or <code>null</code> if the collection is empty
   */
  def findOne(): Option[DBObject] = optWrap(underlying.findOne())

  /** 
   * Returns a single object from this collection matching the query.
   * @param o the query object
   * @return the object found, or <code>null</code> if no such object exists
   */
  def findOne(o: DBObject): Option[DBObject] = optWrap(underlying.findOne(o))

  /**
   * Returns a single object from this collection matching the query.
   * @param o the query object
   * @param fields fields to return
   * @return the object found, or <code>null</code> if no such object exists
   * @dochub find
   */
  def findOne(o: DBObject, fields: DBObject): Option[DBObject] = optWrap(underlying.findOne(o, fields))
  def findOneView[A <% DBObject : Manifest](o: A) = optWrap(underlying.findOne(o))
  def findOneView[A <% DBObject : Manifest, B <% DBObject : Manifest](o: A, fields: B) = 
    optWrap(underlying.findOne(o, fields))

  /**
   * Finds the first document in the query (sorted) and updates it. 
   * If remove is specified it will be removed. If new is specified then the updated 
   * document will be returned, otherwise the old document is returned (or it would be lost forever).
   * You can also specify the fields to return in the document, optionally.
   * @return the found document (before, or after the update)
   */
  def findAndModify[A <% DBObject : Manifest, B <% DBObject : Manifest](query: A, update: B) = optWrap(underlying.findAndModify(query, update))
  /**
   * Finds the first document in the query (sorted) and updates it. 
   * @return the old document
   */
  def findAndModify[A <% DBObject : Manifest, B <% DBObject : Manifest, C <% DBObject : Manifest](query: A, sort: B, update: C) = optWrap(underlying.findAndModify(query, sort, update))
  /**
   * Finds the first document in the query and updates it. 
   * @return the old document
   */
  def findAndModify[A <% DBObject : Manifest, B <% DBObject : Manifest, C <% DBObject : Manifest, D <% DBObject : Manifest](
    query: A, fields: B, sort: C, remove: Boolean, update: D, returnNew: Boolean, upsert: Boolean
  ) = optWrap(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert))

  /**
   * Finds the first document in the query and removes it. 
   * @return the removed document
   */
  def findAndRemove[A <% DBObject : Manifest](query: A) = optWrap(underlying.findAndRemove(query))

  /**
   * Finds an object by its id. This compares the passed in value to the _id field of the document
   * I've put some hackery in to try to detect possible conversions....
   * Because of the way we're using context and view bounds in Scala
   * the broad match of this method can be problematic.
   *   
   * 
   * @param obj any valid object
   * @return the object, if found, otherwise <code>null</code>
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
   * Because of the way we're using context and view bounds in Scala
   * the broad match of this method can be problematic.
   * I've put some hackery in to try to detect possible conversions....
   * 
   * @param obj any valid object
   * @param fields fields to return
   * @return the object, if found, otherwise <code>null</code>
   * @dochub find
   */
  def findOne(obj: AnyRef, fields: DBObject): Option[DBObject] =  obj match {

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
 *
 * @param A  type representing a DBObject subclass which this class should return instead of generic DBObjects
 * @param underlying DBCollection object to proxy
 */
class MongoTypedCollection[T <: DBObject : Manifest](val underlying: com.mongodb.DBCollection) extends Iterable[T] 
                                                                                       with MongoCollectionWrapper {
                                                                                     
  type UnderlyingObj = T
  val m = manifest[T]
  log.debug("Manifest erasure: " + m.erasure)
  underlying.setObjectClass(m.erasure)


  override def elements = find
  override def iterator = find
  /** Queries for all objects in this collection. 
   * @return a cursor which will iterate over every object
   * @dochub find
   */
  def find() = underlying.find.asScalaTyped
  /** Queries for an object in this collection.
   * @param ref object for which to search
   * @return an iterator over the results
   * @dochub find
   */
  def find(ref: DBObject) = underlying.find(ref) asScalaTyped
  /** Queries for an object in this collection.
   *
   * <p>
   * An empty DBObject will match every document in the collection.
   * Regardless of fields specified, the _id fields are always returned.
   * </p>
   * <p>
   * An example that returns the "x" and "_id" fields for every document 
   * in the collection that has an "x" field:
   * </p>
   * <blockquote><pre>
   * BasicDBObject keys = new BasicDBObject();
   * keys.put("x", 1);
   *
   * DBCursor cursor = collection.find(new BasicDBObject(), keys); 
   * </pre></blockquote>
   *
   * @param ref object for which to search
   * @param keys fields to return
   * @return a cursor to iterate over results
   * @dochub find
   */
  def find(ref: DBObject, keys: DBObject) = underlying.find(ref, keys) asScalaTyped
  /** Finds an object.
   * @param ref query used to search
   * @param fields the fields of matching objects to return
   * @param numToSkip will not return the first <tt>numToSkip</tt> matches
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  all objects that match will be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or the # that fit in a batch
   * @param options - see Bytes QUERYOPTION_*
   * @return the objects, if found
   * @dochub find
   */
  def find(ref: DBObject, fields: DBObject, numToSkip: Int, batchSize: Int) = underlying.find(ref, fields, numToSkip, batchSize) asScalaTyped
 /** 
   * Returns a single object from this collection.
   * @return the object found, or <code>null</code> if the collection is empty
   */
  def findOne() = optWrap(underlying.findOne().asInstanceOf[T])
  /** 
   * Returns a single object from this collection matching the query.
   * @param o the query object
   * @return the object found, or <code>null</code> if no such object exists
   */
  def findOne(o: DBObject) = optWrap(underlying.findOne(o).asInstanceOf[T])
  /**
   * Returns a single object from this collection matching the query.
   * @param o the query object
   * @param fields fields to return
   * @return the object found, or <code>null</code> if no such object exists
   * @dochub find
   */
  def findOne(o: DBObject, fields: DBObject) = optWrap(underlying.findOne(o, fields).asInstanceOf[T])
  /**
   * Finds an object by its id.  
   * This compares the passed in value to the _id field of the document
   * 
   * @param obj any valid object
   * @return the object, if found, otherwise <code>null</code>
   */
  def findOne(obj: Object) = optWrap(underlying.findOne(obj).asInstanceOf[T])
 /**
   * Finds an object by its id.  
   * This compares the passed in value to the _id field of the document
   * 
   * @param obj any valid object
   * @param fields fields to return
   * @return the object, if found, otherwise <code>null</code>
   * @dochub find
   */
  def findOne(obj: Object, fields: DBObject) = optWrap(underlying.findOne(obj, fields).asInstanceOf[T])

  /**
   * Finds the first document in the query (sorted) and updates it. 
   * If remove is specified it will be removed. If new is specified then the updated 
   * document will be returned, otherwise the old document is returned (or it would be lost forever).
   * You can also specify the fields to return in the document, optionally.
   * @return the found document (before, or after the update)
   */
  def findAndModify[A <% DBObject : Manifest, B <% DBObject : Manifest](query: A, update: B) = optWrap(underlying.findAndModify(query, update)).asInstanceOf[T]
  /**
   * Finds the first document in the query (sorted) and updates it. 
   * @return the old document
   */
  def findAndModify[A <% DBObject : Manifest, B <% DBObject : Manifest, C <% DBObject : Manifest](query: A, sort: B, update: C) = optWrap(underlying.findAndModify(query, sort, update)).asInstanceOf[T]
  /**
   * Finds the first document in the query and updates it. 
   * @return the old document
   */
  def findAndModify[A <% DBObject : Manifest, B <% DBObject : Manifest, C <% DBObject : Manifest, D <% DBObject : Manifest](
    query: A, fields: B, sort: C, remove: Boolean, update: D, returnNew: Boolean, upsert: Boolean
  ) = optWrap(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert)).asInstanceOf[T]

  /**
   * Finds the first document in the query and removes it. 
   * @return the removed document
   */
  def findAndRemove[A <% DBObject : Manifest](query: A) = optWrap(underlying.findAndRemove(query)).asInstanceOf[T]


  override def head = findOne.get
  override def headOption = Some(findOne.get.asInstanceOf[T])
  override def tail = find.skip(1).map(_.asInstanceOf[T]).toList

  def setHintFields(lst: List[DBObject]) = underlying.setHintFields(lst.asJava)

  def setObjectClass[A <: DBObject : Manifest](c: Class[A]) = {
    underlying.setObjectClass(c)
    new MongoTypedCollection[A](underlying)
  }

  override def toString() = underlying.toString

  def update(q: DBObject, o: DBObject) = underlying.update(q, o)
  def update(q: DBObject, o: DBObject, upsert: Boolean, multi: Boolean) = underlying.update(q, o, upsert, multi)
  def updateMulti(q: DBObject, o: DBObject) = underlying.updateMulti(q, o)

  /** Checks if this collection is equal to another object.
   * @param o object with which to compare this collection
   * @return if the two collections are the same object
   */
  override def equals(obj: Any) = obj match {
    case other: MongoCollectionWrapper => underlying.equals(other.underlying)
    case _ => false
  }



  
}
