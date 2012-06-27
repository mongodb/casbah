/**
 * Copyright (c) 2010, 2011 10gen, Inc. <http://10gen.com>
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

import com.mongodb.casbah.util.bson.decoding._

import com.mongodb.casbah.util.Logging

import scalaj.collection.Imports._
import com.mongodb.{ DBCursor , DBCollection , DBDecoderFactory, DBEncoderFactory}

/**
* Scala wrapper for Mongo DBCollections,
* including ones which return custom DBObject subclasses
* via setObjectClass and the like.
* Provides any non-parameterized methods and the basic structure.
* Requires an underlying object of a DBCollection.
*
* This is a rewrite of the Casbah 1.0 approach which was rather
* naive and unecessarily complex.... formerly was MongoCollectionWrapper
*
* @author Brendan W. McAdams <brendan@10gen.com>
* @version 2.0, 12/23/10
* @since 1.0
*
* @tparam T (DBObject or subclass thereof)
*/
abstract class MongoCollection extends Logging with Iterable[DBObject] {
  type T <: DBObject

  /**
   * If defined, load the customDecoderFactory.
   */
  customDecoderFactory.foreach { decoder =>
    log.debug("Loading Custom DBDecoderFactory '%s' into collection (%s)", decoder, this)
    underlying.setDBDecoderFactory(decoder)
  }

  def customDecoderFactory: Option[DBDecoderFactory] = None

  def customEncoderFactory: Option[DBEncoderFactory] = Option(underlying.getDBEncoderFactory)

  /**
   * The underlying Java Mongo Driver Collection object we proxy.
   */
  def underlying: DBCollection


  /** Returns the database this collection is a member of.
   * @return this collection's database
   */
  implicit val db = underlying.getDB().asScala

  /** Adds the "private" fields _id to an object.
   * @param o <code>DBObject</code> to which to add fields
   * @return the modified parameter object
   */
  def apply[A <% DBObject](o: A) = underlying.apply(o)

  /** Adds the "private" fields _id to an object.
   * @param jo object to which to add fields
   * @param ensureID whether to add an <code>_id</code> field or not
   * @return the modified object <code>o</code>
   */
  def apply[A <% DBObject](jo: A, ensureID: Boolean) = underlying.apply(jo, ensureID)

  /** Forces creation of an index on a set of fields, if one does not already exist.
   * @param keys an object with a key set of the fields desired for the index
   */
  def createIndex[A <% DBObject](keys: A) = underlying.createIndex(keys)

  def createIndex[A <% DBObject, B <% DBObject](keys: A, options: B) =
    underlying.createIndex(keys, options)

  /**
   * find distinct values for a key
   */
  def distinct(key: String) = underlying.distinct(key).asScala

  /**
   * find distinct values for a key
   * @param query query to apply on collection
   */
  def distinct[A <% DBObject](key: String, query: A) =
    underlying.distinct(key, query).asScala

  /** Drops (deletes) this collection
   */
  def drop() = underlying.drop

  /** Drops (deletes) this collection
   */
  def dropCollection() = underlying.drop

  def dropIndex[A <% DBObject](keys: A) = underlying.dropIndex(keys)

  def dropIndex(name: String) = underlying.dropIndex(name)
  /** Drops all indices from this collection
   */
  def dropIndexes() = underlying.dropIndexes
  def dropIndexes(name: String) = underlying.dropIndexes

  /** Creates an index on a set of fields, if one does not already exist.
   * @param keys an object with a key set of the fields desired for the index
   */
  def ensureIndex[A <% DBObject](keys: A) = underlying.ensureIndex(keys)

  /** Ensures an index on this collection (that is, the index will be created if it does not exist).
   * ensureIndex is optimized and is inexpensive if the index already exists.
   * @param keys fields to use for index
   * @param name an identifier for the index
   * @dochub indexes
   */
  def ensureIndex[A <% DBObject](keys: A, name: String) = underlying.ensureIndex(keys, name)

  /** Ensures an index on this collection (that is, the index will be created if it does not exist).
   * ensureIndex is optimized and is inexpensive if the index already exists.
   * @param keys fields to use for index
   * @param options options for the index (name, unique, etc)
   * @dochub indexes
   */
  def ensureIndex[A <% DBObject, B <% DBObject](keys: A, options: B) = underlying.ensureIndex(keys, options)

  /** Ensures an optionally unique index on this collection.
   * @param keys fields to use for index
   * @param name an identifier for the index
   * @param unique if the index should be unique
   */
  def ensureIndex[A <% DBObject](keys: A, name: String, unique: Boolean) = underlying.ensureIndex(keys, name, unique)

  /** Ensures an index on this collection (that is, the index will be created if it does not exist).
   * ensureIndex is optimized and is inexpensive if the index already exists.
   * This creates an ascending index on a particular field.
   * @param name an identifier for the index
   * @dochub indexes
   */
  def ensureIndex(fieldName: String) = underlying.ensureIndex(fieldName)

  /** Queries for all objects in this collection. 
   * @return a cursor which will iterate over every object
   * @dochub find
   */
  def find() = _newCursor(underlying.find)

  /** Queries for an object in this collection.
   * @param ref object for which to search
   * @return an iterator over the results
   * @dochub find
   */
  def find[A <% DBObject](ref: A) = _newCursor(underlying.find(ref))

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
  def find[A <% DBObject, B <% DBObject](ref: A, keys: B) = _newCursor(underlying.find(ref, keys))

  /** Finds an object.
   * @param ref query used to search
   * @param fields the fields of matching objects to return
   * @param numToSkip will not return the first <tt>numToSkip</tt> matches
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  all objects that match will be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or the # that fit in a batch
   * @param options - see Bytes QUERYOPTION_*
   * @return the objects, if found
   * @dochub find
   */
  def find[A <% DBObject, B <% DBObject](ref: A, fields: B, numToSkip: Int, batchSize: Int) =
    _newCursor(underlying.find(ref, fields, numToSkip, batchSize))

  /** 
   * Returns a single object from this collection.
   * @return (Option[T]) Some() of the object found, or <code>None</code> if this collection is empty
   */
  def findOne() = Option(underlying.findOne())

  /** 
   * Returns a single object from this collection matching the query.
   * @param o the query object
   * @return (Option[T]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOne[A <% DBObject](o: A) =
    Option(underlying.findOne(o: DBObject))

  /**
   * Returns a single object from this collection matching the query.
   * @param o the query object
   * @param fields fields to return
   * @return (Option[T]) Some() of the object found, or <code>None</code> if no such object exists
   * @dochub find
   */
  def findOne[A <% DBObject, B <% DBObject](o: A, fields: B) =
    Option(underlying.findOne(o: DBObject, fields))

  /** 
   * Find an object by its ID.
   * Finds an object by its id. This compares the passed in 
   * value to the _id field of the document.
   * 
   * Returns a single object from this collection matching the query.
   * @param id the id to match
   * @return (Option[T]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOneByID(id: AnyRef) = Option(underlying.findOne(id))

  /**
   * Find an object by its ID.
   * Finds an object by its id. This compares the passed in 
   * value to the _id field of the document.
   * 
   * Returns a single object from this collection matching the query.
   *
   * @param id the id to match
   * @param fields fields to return
   * @return (Option[T]) Some() of the object found, or <code>None</code> if no such object exists
   * @dochub find
   */
  def findOneByID[B <% DBObject](id: AnyRef, fields: B) =
    Option(underlying.findOne(id, fields))

  /**
   * Finds the first document in the query (sorted) and updates it. 
   * If remove is specified it will be removed. If new is specified then the updated
   * document will be returned, otherwise the old document is returned (or it would be lost forever).
   * You can also specify the fields to return in the document, optionally.
   * @return (Option[T]) of the the found document (before, or after the update)
   */
  def findAndModify[A <% DBObject, B <% DBObject](query: A, update: B) =
    Option(underlying.findAndModify(query, update))

  /**
   * Finds the first document in the query (sorted) and updates it. 
   * @return the old document
   */
  def findAndModify[A <% DBObject, B <% DBObject, C <% DBObject](query: A, sort: B, update: C) =
    Option(underlying.findAndModify(query, sort, update))

  /**
   * Finds the first document in the query and updates it. 
   * @return the old document
   */
  def findAndModify[A <% DBObject, B <% DBObject, C <% DBObject, D <% DBObject](query: A, fields: B, sort: C,
    remove: Boolean, update: D,
    returnNew: Boolean, upsert: Boolean) =
    Option(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert))

  /**
   * Finds the first document in the query and removes it. 
   * @return the removed document
   */
  def findAndRemove[A <% DBObject](query: A) =
    Option(underlying.findAndRemove(query))

  /**
   * write concern aware write op block.
   *
   * Checks getLastError after the last write.
   * If  you run multiple ops you'll only get the final 
   * error.
   * 
   * Your op function gets a copy of this MongoDB instance.
   * 
   * This is for write ops only - you cannot return data from it.
   * 
   * Your function must return WriteResult, which is the 
   * return type of any mongo write operation like insert/save/update/remove
   * 
   * If you have set a connection or DB level WriteConcern,
   * it will be inherited.
   *
   * @throws MongoException
   */
  def request(op: this.type => WriteResult) = {
    op(this).getLastError.throwOnError
  }

  /**
   * write concern aware write op block.
   *
   * Checks getLastError after the last write.
   * If  you run multiple ops you'll only get the final 
   * error.
   * 
   * Your op function gets a copy of this MongoDB instance.
   * 
   * This is for write ops only - you cannot return data from it.
   * 
   * Your function must return WriteResult, which is the 
   * return type of any mongo write operation like insert/save/update/remove
   * 
   * @throws MongoException
   */
  def request(w: Int, wTimeout: Int = 0, fsync: Boolean = false)(op: this.type => WriteResult) =
    op(this).getLastError(WriteConcern(w, wTimeout, fsync)).throwOnError

  /**
   * write concern aware write op block.
   *
   * Checks getLastError after the last write.
   * If  you run multiple ops you'll only get the final 
   * error.
   * 
   * Your op function gets a copy of this MongoDB instance.
   * 
   * This is for write ops only - you cannot return data from it.
   * 
   * Your function must return WriteResult, which is the 
   * return type of any mongo write operation like insert/save/update/remove
   * 
   * @throws MongoException
   */
  def request(concern: com.mongodb.WriteConcern)(op: this.type => WriteResult) =
    op(this).getLastError(concern).throwOnError

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
   *
   * TODO - Make this support type construction
   */
  def getCollection(n: String) = underlying.getCollection(n).asScala

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
   *
   * TODO - Make this support type construction
   */
  def collection(n: String) = underlying.getCollection(n).asScala

  /**
   *  Returns the number of documents in the collection
   *  @return number of documents in the query
   */
  def getCount() = underlying.getCount()

  /**
   *  Returns the number of documents in the collection
   *  that match the specified query
   *
   *  @param query query to select documents to count
   *  @return number of documents that match query
   */
  def getCount[A <% DBObject](query: A) = underlying.getCount(query)

  /**
   *  Returns the number of documents in the collection
   *  that match the specified query
   *
   *  @param query query to select documents to count
   *  @param fields fields to return
   *  @return number of documents that match query and fields
   */
  def getCount[A <% DBObject, B <% DBObject](query: A, fields: B) =
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
  def getCount[A <% DBObject, B <% DBObject](query: A, fields: B, limit: Long, skip: Long) =
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

  def stats = getStats()

  def getStats() = underlying.getStats()

  def group[A <% DBObject, B <% DBObject, C <% DBObject](key: A, cond: B, initial: C, reduce: String): Iterable[T] =
    underlying.group(key, cond, initial, reduce).map(_._2.asInstanceOf[T])

  /**
   * By default perform an absurdly simple grouping with no initial object or reduce function.
   */
  def group[A <% DBObject, B <% DBObject](key: A, cond: B, function: String = "function(obj, prev) {}"): Iterable[T] =
    group(key, cond, MongoDBObject.empty, function)

  def group[A <% DBObject, B <% DBObject, C <% DBObject](key: A, cond: B, initial: C, reduce: String, finalize: String): Iterable[T] = {
    underlying.group(key,cond, initial, reduce, finalize).map(_._2.asInstanceOf[T])
  }

  /**
   * Saves document(s) to the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param arr  array of documents (<% DBObject) to save
   * @dochub insert
   * TODO - Wrapper for WriteResult?
   */
  def insert[A](docs: A*)(implicit dbObjView: A => DBObject, concern: com.mongodb.WriteConcern = writeConcern, encoder: DBEncoder = customEncoderFactory.map(_.create).orNull ): WriteResult = {
    val b = new scala.collection.mutable.ArrayBuilder.ofRef[DBObject]
    b.sizeHint(docs.size)
    for (x <- docs) b += dbObjView(x)
    underlying.insert(b.result, concern, encoder)
  }

  def isCapped = underlying.isCapped()

  /** 
   * mapReduce
   * Execute a mapReduce against this collection.
   * NOTE: JSFunction is just a type alias for String
   * 
   * @param  mapFunction (JSFunction) The JavaScript to execute for the map function
   * @param  reduceFunction (JSFunction) The JavaScript to execute for the reduce function
   * 
   */
  def mapReduce(mapFunction: JSFunction,
    reduceFunction: JSFunction,
    output: MapReduceOutputTarget,
    query: Option[DBObject] = None,
    sort: Option[DBObject] = None,
    limit: Option[Int] = None,
    finalizeFunction: Option[JSFunction] = None,
    jsScope: Option[String] = None,
    verbose: Boolean = false): map_reduce.MapReduceResult =
    map_reduce.MapReduceResult(getDB.command(MapReduceCommand(name, mapFunction, reduceFunction,
      output, query, sort, limit, finalizeFunction,
      jsScope, verbose).toDBObject))

  def mapReduce(cmd: map_reduce.MapReduceCommand) = map_reduce.MapReduceResult(getDB.command(cmd.toDBObject))

  /** Removes objects from the database collection.
   * @param o the object that documents to be removed must match
   * @param concern WriteConcern for this operation
   * @dochub remove
   * TODO - Wrapper for WriteResult?
   */
  def remove[A](o: A)(implicit dbObjView: A => DBObject, concern: com.mongodb.WriteConcern = getWriteConcern, encoder: DBEncoder = customEncoderFactory.map(_.create).orNull ) =
    underlying.remove(dbObjView(o), concern, encoder)

  /** Clears all indices that have not yet been applied to this collection. */
  def resetIndexCache() = underlying.resetIndexCache()

  /** Saves an object to this collection.
   * @param o the <code>DBObject</code> to save
   *        will add <code>_id</code> field to o if needed
   * TODO - Wrapper for WriteResult?
   */
  def save[A](o: A)(implicit dbObjView: A => DBObject, concern: com.mongodb.WriteConcern = writeConcern) = underlying.save(dbObjView(o), writeConcern)


  /** Set hint fields for this collection.
   * @param lst a list of <code>DBObject</code>s to be used as hints
   */
  def setHintFields[A <% DBObject](docs: List[A]) = {
    val b = List.newBuilder[DBObject]
    for (x <- docs) b += x
    underlying.setHintFields(b.result.asJava)
  }

  /** Set hint fields for this collection.
   * @param lst a list of <code>DBObject</code>s to be used as hints
   */
  def hintFields_=[A <% DBObject](docs: List[A]) = setHintFields(docs)

  def setInternalClass(path: String, c: Class[_]) = underlying.setInternalClass(path, c)

  def internalClass_=(path: String, c: Class[_]) = setInternalClass(path, c)

  override def toString() = underlying.toString

  /**
   * Performs an update operation.
   * @param q search query for old object to update
   * @param o object with which to update <tt>q</tt>
   * @param upsert if the database should create the element if it does not exist
   * @param multi if the update should be applied to all objects matching (db version 1.1.3 and above)
   * @see http://www.mongodb.org/display/DOCS/Atomic+Operations
   * @dochub update
   * TODO - Wrapper for WriteResult?
   */
   def update[A, B](q: A, o: B, upsert: Boolean = false, multi: Boolean = false)(implicit queryView: A => DBObject, objView: B => DBObject, concern: com.mongodb.WriteConcern = this.writeConcern, encoder: DBEncoder = customEncoderFactory.map(_.create).orNull ) = 
    underlying.update(queryView(q), objView(o), upsert, multi, concern, encoder)


  /**
   * Perform a multi update
   * @dochub update
   * @param q search query for old object to update
   * @param o object with which to update <tt>q</tt>
   */
  @deprecated("In the face of default arguments this is a bit silly. Please use update(multi=True)")
  def updateMulti[A <% DBObject, B <% DBObject](q: A, o: B) = underlying.updateMulti(q, o)

  override def hashCode() = underlying.hashCode

  /** Checks if this collection is equal to another object.
   * @param o object with which to compare this collection
   * @return if the two collections are the same object
   */
  override def equals(obj: Any) = obj match {
    case other: MongoCollection => underlying.equals(other.underlying)
    case _ => false
  }

  def count = getCount
  def count[A <% DBObject](query: A) = getCount(query)
  def count[A <% DBObject, B <% DBObject](query: A, fields: B) = getCount(query, fields)

  /**
   *  Gets the the error (if there is one) from the previous operation.  The result of
   *  this command will look like
   *
   * <pre>
   * { "err" :  errorMessage  , "ok" : 1.0 }
   * </pre>
   *
   * The value for errorMessage will be null if no error occurred, or a description otherwise.
   *
   * Care must be taken to ensure that calls to getLastError go to the same connection as that
   * of the previous operation. See com.mongodb.Mongo.requestStart for more information.
   *
   *  @return DBObject with error and status information
   */
  def getLastError() = getDB.getLastError
  def lastError() = getLastError()
  def getLastError(concern: com.mongodb.WriteConcern) =
    getDB.getLastError(concern)
  def lastError(concern: com.mongodb.WriteConcern) =
    getLastError(concern)
  def getLastError(w: Int, wTimeout: Int, fsync: Boolean) =
    getDB.getLastError(w, wTimeout, fsync)
  def lastError(w: Int, wTimeout: Int, fsync: Boolean) =
    getLastError(w, wTimeout, fsync)

  /**
   * Save an object to the Collection
   *
   * @param x object to save to the collection
   */
  def +=[A <% DBObject](x: A) = save(x)

  /**
   * Remove a matching object from the collection
   *
   * @param x object to remove from the collection
   */
  def -=[A <% DBObject](x: A) = remove(x)

  /**
   * 
   * Set the write concern for this database.
   * Will be used for writes to any collection in this database.
   * See the documentation for {@link com.mongodb.WriteConcern} for more info.
   * 
   * @param concern (com.mongodb.WriteConcern) The write concern to use
   * @see com.mongodb.WriteConcern 
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def setWriteConcern(concern: com.mongodb.WriteConcern) = underlying.setWriteConcern(concern)

  /**
   * 
   * Set the write concern for this database.
   * Will be used for writes to any collection in this database.
   * See the documentation for {@link com.mongodb.WriteConcern} for more info.
   * 
   * @param concern (WriteConcern) The write concern to use
   * @see WriteConcern 
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def writeConcern_=(concern: com.mongodb.WriteConcern) = setWriteConcern(concern)

  /**
   * 
   * get the write concern for this database,
   * which is used for writes to any collection in this database.
   * See the documentation for {@link com.mongodb.WriteConcern} for more info.
   * 
   * @see com.mongodb.WriteConcern 
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def getWriteConcern = underlying.getWriteConcern()

  /**
   * 
   * get the write concern for this database,
   * which is used for writes to any collection in this database.
   * See the documentation for {@link com.mongodb.WriteConcern} for more info.
   *
   * @see com.mongodb.WriteConcern 
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def writeConcern = getWriteConcern

  /**
   * Sets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for {@link ReadPreference} for more information.
   *
   * @param preference Read Preference to use
   */
  def readPreference_=(pref: ReadPreference) = setReadPreference(pref)

  /**
   * Sets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for {@link ReadPreference} for more information.
   *
   * @param preference Read Preference to use
   */
  def setReadPreference(pref: ReadPreference) = underlying.setReadPreference(pref)

  /**
   * Gets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for {@link ReadPreference} for more information.
   *
   * @param preference Read Preference to use
   */
  def readPreference = getReadPreference

  /**
   * Gets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for {@link ReadPreference} for more information.
   *
   * @param preference Read Preference to use
   */
  def getReadPreference = underlying.getReadPreference

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
   * @see com.mongodb.Bytes
   */
  def getOptions() = underlying.getOptions

  /** 
   * Manipulate Network Options
   * 
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def options = getOptions

  /** 
   * Sets queries to be OK to run on slave nodes.
   * @deprecated Replaced with ReadPreference.SECONDARY
   */
  @deprecated("Replaced with ReadPreference.SECONDARY")
  def slaveOk() = underlying.slaveOk() // use parens because this side-effects

  /**
   * does a rename of this collection to newName
   * As per the Java API this returns a *NEW* Collection,
   * and the old collection is probably no good anymore.
   *
   * This collection *WILL NOT* mutate --- the instance will 
   * still point at a now nonexistant collection with the old name
   * ... You must capture the return value for the new instance.
   *
   * @param newName new collection name (not a full namespace)
   * @return the new collection
   */
  def rename(newName: String): MongoCollection =
    _newInstance(underlying.rename(newName))

  /**
   * does a rename of this collection to newName
   * As per the Java API this returns a *NEW* Collection,
   * and the old collection is probably no good anymore.
   *
   * This collection *WILL NOT* mutate --- the instance will 
   * still point at a now nonexistant collection with the old name
   * ... You must capture the return value for the new instance.
   *
   * @param newName new collection name (not a full namespace)
   * @param dropTarget if a collection with the new name exists, whether or not to drop it
   * @return the new collection
   */
  def rename(newName: String, dropTarget: Boolean): MongoCollection =
    _newInstance(underlying.rename(newName, dropTarget))

  /**
   * _newCursor
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of the correct cursor implementation from a
   * Java cursor.  Good with cursor calls that return a new cursor.
   * Should figure out the right type to return based on typing setup.
   *
   * @param  cursor (DBCursor)
   * @return (MongoCursorBase)
   */
  def _newCursor(cursor: DBCursor): MongoCursor

  /**
   * _newInstance
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of THIS concrete implementation from a
   * Java collection.  Good with calls that return a new collection.
   *
   * @param  cursor (DBCollection)
   * @return (this.type)
   */
  def _newInstance(collection: DBCollection): MongoCollection


  override def head = headOption.get
  override def headOption = findOne
  override def tail = find.skip(1).toIterable
  override def iterator = find
  override def size = count.toInt
}

/**
 * Concrete collection implementation expecting standard DBObject operation
 * This is the version of MongoCollectionBase you should expect to use in most cases.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 2.0, 12/23/10
 * @since 1.0
 *
 * @tparam DBObject
 */
class ConcreteMongoCollection(val underlying: DBCollection) extends MongoCollection {
  type T = DBObject

  /**
   * _newCursor
   * 
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of the correct cursor implementation from a 
   * Java cursor.  Good with cursor calls that return a new cursor.
   * Should figure out the right type to return based on typing setup.
   *
   * @param  cursor (DBCursor) 
   * @return (MongoCursorBase)
   */
  def _newCursor(cursor: DBCursor) = new ConcreteMongoCursor(cursor)

  /** 
   * _newInstance
   * 
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of THIS concrete implementation from a 
   * Java collection.  Good with calls that return a new collection.
   *
   * @param  cursor (DBCollection) 
   * @return (this.type)
   */
  def _newInstance(collection: DBCollection): MongoCollection = new ConcreteMongoCollection(collection)

  def asLazy: LazyMongoCollection = new LazyMongoCollection(underlying)
}

/**
 * Proxy of a DBCollection which uses an OptimizedLazyDBFactory.
 *
 * Doesn't *require* OptimizedLazyDBObjects to save (They're readonly in
 * the current implementation anyway)
 *
 * According to initial tests of the Lazy system:
 * """
 * Sub-objects can be obtained with no copy of data, just the offset
 * changes.
 * For a 700 bytes object, when accessing only a couple fields, the driver
 * speed is about 2.5x.
 * """
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 2.2, 8/18/11
 * @since 1.0
 *
 * @param  val underlying (DBCollection)
 */
class LazyMongoCollection(val underlying: DBCollection) extends MongoCollection {
  type T = OptimizedLazyDBObject

  override def customDecoderFactory = Some(OptimizedLazyDBDecoder.Factory)


  /**
   * _newCursor
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of the correct cursor implementation from a
   * Java cursor.  Good with cursor calls that return a new cursor.
   * Should figure out the right type to return based on typing setup.
   *
   * @param  cursor (DBCursor)
   * @return (MongoCursorBase)
   */
   def _newCursor(cursor: DBCursor) = new LazyMongoCursor(cursor)

  /**
   * _newInstance
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of THIS concrete implementation from a
   * Java collection.  Good with calls that return a new collection.
   *
   * @param  cursor (DBCollection)
   * @return (this.type)
   */
   def _newInstance(collection: DBCollection): MongoCollection = new LazyMongoCollection(collection)

}


/** Helper object for some static methods
 */
object MongoCollection extends Logging {

  /** 
   * generateIndexName
   * 
   * Generate an index name from the set of fields it is over
   *
   * @param  keys (A) The names of the fields used in this index
   * @return a String containing the new name, represented from the index' fields
   * @tparam A A View of DBObject
   */
  def generateIndexName[A <% DBObject](keys: A) = DBCollection.genIndexName(keys)

}

