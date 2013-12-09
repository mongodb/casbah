/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
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

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, MILLISECONDS}

import com.mongodb.{MongoExecutionTimeoutException, DBCursor, DBDecoderFactory, DBEncoderFactory, DBCollection}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging

import com.mongodb.casbah.map_reduce.{MapReduceResult, MapReduceCommand}
import com.mongodb.casbah.TypeImports.WriteResult
import com.mongodb.casbah.commons.TypeImports.DBObject
import com.mongodb.casbah.TypeImports.DBEncoder

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
 * @version 2.0, 12/23/10
 * @since 1.0
 */
trait MongoCollectionBase extends Logging {
  self =>
  type T <: DBObject
  type CursorType

  /**
   * The underlying Java Mongo Driver Collection object we proxy.
   */
  def underlying: DBCollection

  /**
   * If defined, load the customDecoderFactory.
   */
  customDecoderFactory.foreach {
    decoder =>
      log.debug("Loading Custom DBDecoderFactory '%s' into collection (%s)", decoder, this)
      underlying.setDBDecoderFactory(decoder)
  }

  def customDecoderFactory: Option[DBDecoderFactory] = None

  def customEncoderFactory: Option[DBEncoderFactory] = Option(underlying.getDBEncoderFactory)

  def iterator = find()

  /** Returns the database this collection is a member of.
    * @return this collection's database
    */
  implicit val db = underlying.getDB.asScala

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
   * @param query query to apply on collection
   */
  def distinct[A <% DBObject](key: String, query: A = MongoDBObject.empty, readPrefs: ReadPreference = getReadPreference) = underlying.distinct(key, query, readPrefs).asScala

  /** Drops (deletes) this collection
    */
  def drop() = underlying.drop()

  /** Drops (deletes) this collection
    */
  def dropCollection() = underlying.drop()

  def dropIndex[A <% DBObject](keys: A) = underlying.dropIndex(keys)

  def dropIndex(name: String) = underlying.dropIndex(name)

  /** Drops all indices from this collection
    */
  def dropIndexes() = underlying.dropIndexes()

  def dropIndexes(name: String) = underlying.dropIndexes()

  /** Creates an index on a set of fields, if one does not already exist.
    * @param keys an object with a key set of the fields desired for the index
    */
  def ensureIndex[A <% DBObject](keys: A) = underlying.ensureIndex(keys)

  /** Ensures an index on this collection (that is, the index will be created if it does not exist).
    * ensureIndex is optimized and is inexpensive if the index already exists.
    * @param keys fields to use for index
    * @param options options for the index (name, unique, etc)
    */
  def ensureIndex[A <% DBObject, B <% DBObject](keys: A, options: B) = underlying.ensureIndex(keys, options)

  /** Ensures an index on this collection (that is, the index will be created if it does not exist).
    * ensureIndex is optimized and is inexpensive if the index already exists.
    * @param keys fields to use for index
    * @param name an identifier for the index
    */
  def ensureIndex[A <% DBObject](keys: A, name: String) = underlying.ensureIndex(keys, name)

  /** Ensures an optionally unique index on this collection.
    * @param keys fields to use for index
    * @param name an identifier for the index
    * @param unique if the index should be unique
    */
  def ensureIndex[A <% DBObject](keys: A, name: String, unique: Boolean) = underlying.ensureIndex(keys, name, unique)

  /** Ensures an index on this collection (that is, the index will be created if it does not exist).
    * ensureIndex is optimized and is inexpensive if the index already exists.
    * This creates an ascending index on a particular field.
    * @param fieldName an identifier for the index
    */
  def ensureIndex(fieldName: String) = underlying.ensureIndex(fieldName)

  /** Queries for all objects in this collection.
    * @return a cursor which will iterate over every object
    */
  def find() = _newCursor(underlying.find)

  /** Queries for an object in this collection.
    * @param ref object for which to search
    * @return an iterator over the results
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
    */
  def find[A <% DBObject, B <% DBObject](ref: A, keys: B) = _newCursor(underlying.find(ref, keys))

  /** Finds an object.
    * @param ref query used to search
    * @param fields the fields of matching objects to return
    * @param numToSkip will not return the first <tt>numToSkip</tt> matches
    * @param batchSize if positive, is the # of objects per batch sent back from the db.  all objects that match will be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or the # that fit in a batch
    * @return the objects, if found
    */
  @deprecated("Use `find().skip().batchSize()`.", "2.7")
  def find[A <% DBObject, B <% DBObject](ref: A, fields: B, numToSkip: Int, batchSize: Int) =
    _newCursor(underlying.find(ref, fields).skip(numToSkip).batchSize(batchSize))

  /**
   * Returns a single object from this collection.
   * @return (Option[T]) Some() of the object found, or <code>None</code> if this collection is empty
   */
  def findOne() = _typedValue(underlying.findOne())

  /**
   * Returns a single object from this collection matching the query.
   *
   * @param o           the query object
   * @param fields      (optional) fields to return
   * @param orderBy     (optional) a document whose fields specify the attributes on which to sort the result set.
   * @param readPrefs   (optional)
   * @param maxTime (optional) the maximum duration that the server will allow this operation to execute before killing it
   *
   * @return            (Option[T]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOne[A <% DBObject, B <% DBObject, C <% DBObject](o: A = MongoDBObject.empty,
                                                           fields: B = MongoDBObject.empty,
                                                           orderBy: C = MongoDBObject.empty,
                                                           readPrefs: ReadPreference = getReadPreference,
                                                           maxTime: Duration = Duration(0, MILLISECONDS)) = {
    val document = underlying.find(o, fields)
      .sort(orderBy)
      .setReadPreference(readPrefs)
      .maxTime(maxTime.length, maxTime.unit)
      .one()
    _typedValue(document)
  }

  /**
   * Find an object by its ID.
   * Finds an object by its id. This compares the passed in
   * value to the _id field of the document.
   *
   * Returns a single object from this collection matching the query.
   * @param id the id to match
   * @return (Option[T]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOneByID(id: AnyRef) = _typedValue(underlying.findOne(id))

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
   */
  def findOneByID[B <% DBObject](id: AnyRef, fields: B) =
    _typedValue(underlying.findOne(id, fields))

  /**
   * Finds the first document in the query (sorted) and updates it.
   * If remove is specified it will be removed. If new is specified then the updated
   * document will be returned, otherwise the old document is returned (or it would be lost forever).
   * You can also specify the fields to return in the document, optionally.
   *
   * @param query query to match
   * @param update update to apply
   *
   * @return (Option[T]) of the the found document (before, or after the update)
   */
  def findAndModify[A <% DBObject, B <% DBObject](query: A, update: B) =
    _typedValue(underlying.findAndModify(query, update))

  /**
   * Finds the first document in the query (sorted) and updates it.
   *
   * @param query query to match
   * @param sort sort to apply before picking first document
   * @param update update to apply
   *
   * @return the old document
   */
  def findAndModify[A <% DBObject, B <% DBObject, C <% DBObject](query: A, sort: B, update: C) =
    _typedValue(underlying.findAndModify(query, sort, update))

  /**
   * Finds the first document in the query and updates it.
   *
   * @param query query to match
   * @param fields fields to be returned
   * @param sort sort to apply before picking first document
   * @param remove if true, document found will be removed
   * @param update update to apply
   * @param returnNew if true, the updated document is returned, otherwise the old document is returned (or it would be lost forever)
   * @param upsert do upsert (insert if document not present)
   *
   * @return the old document
   */
  def findAndModify[A <% DBObject, B <% DBObject, C <% DBObject, D <% DBObject](query: A, fields: B, sort: C,
                                                                                remove: Boolean, update: D, returnNew: Boolean, upsert: Boolean) =
    _typedValue(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert))

  /**
   * Finds the first document in the query and updates it.
   *
   * @param query       query to match
   * @param fields      fields to be returned
   * @param sort        sort to apply before picking first document
   * @param remove      if true, document found will be removed
   * @param update      update to apply
   * @param returnNew   if true, the updated document is returned, otherwise the old document is returned (or it would be lost forever)
   * @param upsert      do upsert (insert if document not present)
   * @param maxTime the maximum duration that the server will allow this operation to execute before killing it
   *
   * @return the old document
   */
  def findAndModify[A <% DBObject, B <% DBObject, C <% DBObject, D <% DBObject](query: A, fields: B, sort: C,
                                                                                remove: Boolean, update: D, returnNew: Boolean, upsert: Boolean, maxTime: Duration) =
    _typedValue(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert,
      maxTime.length, maxTime.unit))

  /**
   * Finds the first document in the query and removes it.
   * @return the removed document
   */
  def findAndRemove[A <% DBObject](query: A) =
    _typedValue(underlying.findAndRemove(query))

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
   * @throws MongoException()
   */
  def request(op: this.type => WriteResult) = {
    op(this).getLastError.throwOnError()
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
   * @throws MongoException()
   */
  def request(w: Int, wTimeout: Int = 0, fsync: Boolean = false)(op: this.type => WriteResult) = {
    this.writeConcern = WriteConcern(w, wTimeout, fsync)
    op(this).getLastError.throwOnError()
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
   * @throws MongoException()
   */
  def request(writeConcern: WriteConcern)(op: this.type => WriteResult) = {
    this.writeConcern = writeConcern
    op(this).getLastError.throwOnError()
  }

  /** Find a collection that is prefixed with this collection's name.
    * A typical use of this might be
    * <blockquote><pre>
    * DBCollection users = mongo.getCollection( "wiki" ).getCollection( "users" );
    * </pre></blockquote>
    * Which is equilalent to
    * <pre><blockquote>
    * DBCollection users = mongo.getCollection( "wiki.users" );
    * </pre></blockquote>
    * @param n the name of the collection to find
    * @return the matching collection
    *
    *         TODO - Make this support type construction
    */
  def getCollection(n: String) = underlying.getCollection(n).asScala

  /** Find a collection that is prefixed with this collection's name.
    * A typical use of this might be
    * <blockquote><pre>
    * DBCollection users = mongo.getCollection( "wiki" ).getCollection( "users" );
    * </pre></blockquote>
    * Which is equilalent to
    * <pre><blockquote>
    * DBCollection users = mongo.getCollection( "wiki.users" );
    * </pre></blockquote>
    * @param n the name of the collection to find
    * @return the matching collection
    *
    *         TODO - Make this support type construction
    */
  def collection(n: String) = underlying.getCollection(n).asScala

  /**
   * Returns the number of documents in the collection
   * that match the specified query
   *
   * @param query          specifies the selection criteria
   * @param fields         this is ignored
   * @param limit          limit the count to this value
   * @param skip           number of documents to skip
   * @param readPrefs      The [ReadPreference] to be used for this operation
   * @param maxTime    the maximum duration that the server will allow this operation to execute before killing it
   *
   * @return the number of documents that matches selection criteria
   */
  def getCount[A <% DBObject, B <% DBObject](query: A = MongoDBObject.empty, fields: B = MongoDBObject.empty,
                                             limit: Long = 0, skip: Long = 0, readPrefs: ReadPreference = getReadPreference,
                                             maxTime: Duration = Duration(0, MILLISECONDS)) = {
    underlying.find(query, fields)
      .skip(skip.toInt)
      .limit(limit.toInt)
      .setReadPreference(readPrefs)
      .maxTime(maxTime.length, maxTime.unit)
      .count()
  }

  /** Returns the database this collection is a member of.
    * @return this collection's database
    */
  def getDB = underlying.getDB.asScala

  /** Returns the full name of this collection, with the database name as a prefix.
    * @return  the name of this collection
    */
  def getFullName = underlying.getFullName

  /** Returns the full name of this collection, with the database name as a prefix.
    * @return  the name of this collection
    */
  def fullName = getFullName

  /**
   * Return a list of the indexes for this collection.  Each object
   * in the list is the "info document" from MongoDB
   *
   * @return list of index documents
   */
  def getIndexInfo = underlying.getIndexInfo.asScala

  /**
   * Return a list of the indexes for this collection.  Each object
   * in the list is the "info document" from MongoDB
   *
   * @return list of index documents
   */
  def indexInfo = getIndexInfo

  def getName = underlying.getName

  def name = getName

  /** Gets the default class for objects in the collection
    * @return the class
    */
  def getObjectClass = underlying.getObjectClass

  /** Gets the default class for objects in the collection
    * @return the class
    */
  def objectClass = getObjectClass

  /**
   * setObjectClass
   *
   * Set a subtype of DBObject which will be used
   * to deserialize documents returned from MongoDB.
   *
   * This method will return a new <code>MongoTypedCollection[A]</code>
   * which you should capture if you want explicit casting.
   * Else, this collection will instantiate instances of A but cast them to
   * the current <code>T</code> (DBObject if you have a generic collection)
   *
   * @param  c (Class[A])
   * @tparam A A Subtype of DBObject
   *
   *           TODO - Ensure proper subtype return
   */
  def setObjectClass[A <: DBObject : Manifest](c: Class[A]) = {
    underlying.setObjectClass(c)
    new MongoGenericTypedCollection[A](underlying = self.underlying)
  }

  /**
   * setObjectClass
   *
   * Set a subtype of DBObject which will be used
   * to deserialize documents returned from MongoDB.
   *
   * This method will return a new <code>MongoTypedCollection[A]</code>
   * which you should capture if you want explicit casting.
   * Else, this collection will instantiate instances of A but cast them to
   * the current <code>T</code> (DBObject if you have a generic collection)
   *
   * @param  c (Class[A])
   * @tparam A A Subtype of DBObject
   *
   */
  def objectClass_=[A <: DBObject : Manifest](c: Class[A]) = setObjectClass(c)

  def stats = getStats

  def getStats = underlying.getStats


  /**
   * Enables you to call group with the finalize parameter (a function that runs on each
   * row of the output for calculations before sending a return) which the Mongo Java driver does not yet
   * support, by sending a direct DBObject command.  Messy, but it works.
   */
  def group[A <% DBObject, B <% DBObject, C <% DBObject](key: A, cond: B, initial: C,
                                                         reduce: String, finalize: String = null,
                                                         readPrefs: ReadPreference = getReadPreference): Iterable[T] = {
    underlying.group(key, cond, initial, reduce, finalize, readPrefs).map(_._2.asInstanceOf[T])
  }

  /**
   * Saves document(s) to the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param doc  array of documents (<% DBObject) to save
   * @param concern the WriteConcern for the insert
   */
  def insert[A](doc: A, concern: com.mongodb.WriteConcern)(implicit dbObjView: A => DBObject): WriteResult = insert(doc)(dbObjView, concern = concern)

  /**
   * Saves document(s) to the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param docs  array of documents (<% DBObject) to save
   *              TODO - Wrapper for WriteResult?
   */
  def insert[A](docs: A*)(implicit dbObjView: A => DBObject, concern: com.mongodb.WriteConcern = writeConcern, encoder: DBEncoder = customEncoderFactory.map(_.create).orNull): WriteResult = {
    val b = new scala.collection.mutable.ArrayBuilder.ofRef[DBObject]
    b.sizeHint(docs.size)
    for (x <- docs) b += dbObjView(x)
    underlying.insert(b.result(), concern, encoder)
  }

  def isCapped = underlying.isCapped

  /**
   * performs an aggregation operation
   *
   * @param pipeline the aggregation pipeline
   *
   * @return The aggregation operation's result set
   * @deprecated @see aggregate(List[DBObject]) instead
   */
  @deprecated("Use aggregate(List(DBObject) instead", "2.7")
  def aggregate(pipeline: DBObject*): AggregationOutput =
    underlying.aggregate(pipeline.toList.asJava).asScala

  /**
   * performs an aggregation operation
   *
   * @param pipeline the aggregation pipeline
   *
   * @return The aggregation operation's result set
   *
   */
  def aggregate[A <% DBObject](pipeline: Iterable[A]): AggregationOutput =
    underlying.aggregate(pipeline.map(_.asInstanceOf[DBObject]).toList.asJava).asScala

  /**
   * performs an aggregation operation
   *
   * @param pipeline the aggregation pipeline
   * @param options the aggregation options
   *
   * @return The aggregation operation's result set
   *
   */
  def aggregate[A <% DBObject](pipeline: Iterable[A], options: AggregationOptions): AggregationCursor =
    aggregate(pipeline, options, getReadPreference)

  /**
   * performs an aggregation operation
   *
   * @param pipeline the aggregation pipeline
   * @param readPreference The readPreference for the aggregation
   *
   * @return The aggregation operation's result set
   *
   */
  def aggregate[A <% DBObject](pipeline: Iterable[A], readPreference: ReadPreference): AggregationOutput =
    underlying.aggregate(pipeline.map(_.asInstanceOf[DBObject]).toList.asJava, readPreference).asScala

  /**
   * performs an aggregation operation
   *
   * @param pipeline the aggregation pipeline
   * @param options the aggregation options
   * @param readPreference The readPreference for the aggregation
   *
   * @return The aggregation operation's result set
   *
   */
  def aggregate[A <% DBObject](pipeline: Iterable[A], options: AggregationOptions, readPreference: ReadPreference): AggregationCursor =
    underlying.aggregate(pipeline.map(_.asInstanceOf[DBObject]).toList.asJava, options, readPreference).asScala

  /**
   * Return the explain plan for the aggregation pipeline.
   *
   * @param pipeline the aggregation pipeline to explain
   * @param options the options to apply to the aggregation
   * @return the command result.  The explain output may change from release to
   *         release, so best to simply log this.
   */
  def explainAggregate[A <% DBObject](pipeline: Iterable[A], options: AggregationOptions) =
    underlying.explainAggregate(pipeline.map(_.asInstanceOf[DBObject]).toList.asJava, options).asScala

  /**
   * mapReduce execute a mapReduce against this collection.
   *
   * @param mapFunction       the map function (JSFunction is just a type alias for String)
   * @param reduceFunction    the reduce function (JSFunction is just a type alias for String)
   * @param output            the location of the result of the map-reduce operation, defaults to inline.
   *                          You can output to a collection, output to a collection with an action, or output inline.
   * @param query             (optional) the selection criteria for the documents input to the map function.
   * @param sort              (optional) the input documents, useful for optimization.
   * @param limit             (optional) the maximum number of documents to return from the collection before map reduce
   * @param finalizeFunction  (optional) the finalize function (JSFunction is just a type alias for String)
   * @param jsScope           (optional) global variables that are accessible in the map, reduce and finalize functions
   * @param verbose           (optional) include the timing information in the result information
   * @param maxTime       (optional) the maximum duration that the server will allow this operation to execute before killing it
   */
  def mapReduce(mapFunction: JSFunction,
                reduceFunction: JSFunction,
                output: MapReduceOutputTarget,
                query: Option[DBObject] = None,
                sort: Option[DBObject] = None,
                limit: Option[Int] = None,
                finalizeFunction: Option[JSFunction] = None,
                jsScope: Option[DBObject] = None,
                verbose: Boolean = false,
                maxTime: Option[Duration] = None): MapReduceResult = {
    val cmd = MapReduceCommand(name, mapFunction, reduceFunction, output, query, sort, limit,
      finalizeFunction, jsScope, verbose, maxTime)
    mapReduce(cmd)
  }

  /**
   * mapReduce execute a mapReduce against this collection.
   *
   * Throws a MongoExecutionTimeoutException if exceeds max duration limit otherwise returns a MapReduceResult
   *
   * @param cmd the MapReduceCommand
   */
  def mapReduce(cmd: MapReduceCommand) = {
    val result = getDB.command(cmd.toDBObject)

    // Normally we catch MapReduce Errors and return them into a MapReduceResult
    // However, we we now preserve a MongoExecutionTimeoutException to keep in line with other methods
    try {
      result.throwOnError()
    } catch {
      case e: MongoExecutionTimeoutException => throw e
      case t: Throwable => true
    }
    MapReduceResult(result)
  }

  /** Removes objects from the database collection.
    * @param o the object that documents to be removed must match
    * @param concern WriteConcern for this operation
    *                TODO - Wrapper for WriteResult?
    */
  def remove[A](o: A, concern: com.mongodb.WriteConcern = getWriteConcern)(implicit dbObjView: A => DBObject, encoder: DBEncoder = customEncoderFactory.map(_.create).orNull) =
    underlying.remove(dbObjView(o), concern, encoder)

  /** Clears all indices that have not yet been applied to this collection. */
  def resetIndexCache() = underlying.resetIndexCache()

  /** Saves an object to this collection.
    * @param o the <code>DBObject</code> to save
    *          will add <code>_id</code> field to o if needed
    *          TODO - Wrapper for WriteResult?
    */
  def save[A](o: A, concern: com.mongodb.WriteConcern = getWriteConcern)(implicit dbObjView: A => DBObject) = underlying.save(dbObjView(o), concern)


  /** Set hint fields for this collection.
    * @param docs a list of <code>DBObject</code>s to be used as hints
    */
  def setHintFields[A <% DBObject](docs: List[A]) = {
    val b = List.newBuilder[DBObject]
    for (x <- docs) b += x
    underlying.setHintFields(b.result().asJava)
  }

  /** Set hint fields for this collection.
    * @param docs a list of <code>DBObject</code>s to be used as hints
    */
  def hintFields_=[A <% DBObject](docs: List[A]) = setHintFields(docs)

  def setInternalClass(path: String, c: Class[_]) = underlying.setInternalClass(path, c)

  def internalClass_=(path: String, c: Class[_]) = setInternalClass(path, c)

  override def toString = underlying.toString

  /**
   * Performs an update operation.
   * @param q search query for old object to update
   * @param o object with which to update <tt>q</tt>
   *          TODO - Wrapper for WriteResult?
   */
  def update[A, B](q: A, o: B, upsert: Boolean = false, multi: Boolean = false, concern: com.mongodb.WriteConcern = this.writeConcern)(implicit queryView: A => DBObject, objView: B => DBObject, encoder: DBEncoder = customEncoderFactory.map(_.create).orNull) =
    underlying.update(queryView(q), objView(o), upsert, multi, concern, encoder)


  /**
   * Perform a multi update
   * @param q search query for old object to update
   * @param o object with which to update <tt>q</tt>
   */
  @deprecated("In the face of default arguments this is a bit silly. Please use `update(multi=True)`.", "2.3.0")
  def updateMulti[A <% DBObject, B <% DBObject](q: A, o: B) = underlying.updateMulti(q, o)

  override def hashCode() = underlying.hashCode

  /** Checks if this collection is equal to another object.
    * @param obj object with which to compare this collection
    * @return if the two collections are the same object
    */
  override def equals(obj: Any) = obj match {
    case other: MongoCollectionBase => underlying.equals(other.underlying)
    case _ => false
  }

  def count[A <% DBObject, B <% DBObject](query: A = MongoDBObject.empty, fields: B = MongoDBObject.empty,
                                          limit: Long = 0, skip: Long = 0, readPrefs: ReadPreference = getReadPreference,
                                          maxTime: Duration = Duration(0, MILLISECONDS)) =
    getCount(query, fields, limit, skip, readPrefs, maxTime)

  /**
   * Gets the the error (if there is one) from the previous operation.  The result of
   * this command will look like
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
   * @return DBObject with error and status information
   */
  def getLastError = getDB.getLastError()

  def lastError = getLastError

  def getLastError(concern: WriteConcern) =
    getDB.getLastError(concern)

  def lastError(concern: WriteConcern) =
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
   * See the documentation for [[com.mongodb.WriteConcern]] for more info.
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
   * See the documentation for [[com.mongodb.WriteConcern]] for more info.
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
   * See the documentation for [[com.mongodb.WriteConcern]] for more info.
   *
   * @see WriteConcern
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def getWriteConcern = underlying.getWriteConcern

  /**
   *
   * get the write concern for this database,
   * which is used for writes to any collection in this database.
   * See the documentation for [[com.mongodb.WriteConcern]] for more info.
   *
   * @see WriteConcern
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def writeConcern = getWriteConcern

  /**
   * Sets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for [[com.mongodb.casbah.ReadPreference]] for more information.
   *
   * @param pref Read Preference to use
   */
  def readPreference_=(pref: ReadPreference) = setReadPreference(pref)

  /**
   * Sets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for [[com.mongodb.casbah.ReadPreference]] for more information.
   *
   * @param pref Read Preference to use
   */
  def setReadPreference(pref: ReadPreference) = underlying.setReadPreference(pref)

  /**
   * Gets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for [[com.mongodb.casbah.ReadPreference]] for more information.
   */
  def readPreference = getReadPreference

  /**
   * Gets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for [[com.mongodb.casbah.ReadPreference]] for more information.
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
  def getOptions = underlying.getOptions

  /**
   * Manipulate Network Options
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def options = getOptions

  /**
   * Sets queries to be OK to run on slave nodes.
   */
  @deprecated("Replaced with `ReadPreference.SECONDARY`", "2.3.0")
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
    new MongoCollection(self.underlying.rename(newName))

  /**
   *
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
    new MongoCollection(self.underlying.rename(newName, dropTarget))

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
  def _newCursor(cursor: DBCursor): CursorType

  /**
   * _newInstance
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of THIS concrete implementation from a
   * Java collection.  Good with calls that return a new collection.
   *
   * @param  collection (DBCollection)
   * @return (this.type)
   */
  def _newInstance(collection: DBCollection): MongoCollectionBase

  protected def _typedValue(dbObj: DBObject): Option[T] = Option(dbObj.asInstanceOf[T])

}

/**
 * Concrete collection implementation expecting standard DBObject operation
 * This is the version of MongoCollectionBase you should expect to use in most cases.
 *
 * @version 2.0, 12/23/10
 * @since 1.0
 *
 */
class MongoCollection(val underlying: DBCollection) extends MongoCollectionBase with Iterable[DBObject] {

  type T = DBObject
  type CursorType = MongoCursor

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
  def _newCursor(cursor: DBCursor) = new MongoCursor(cursor)

  /**
   * _newInstance
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of THIS concrete implementation from a
   * Java collection.  Good with calls that return a new collection.
   *
   * @param  collection (DBCollection)
   * @return (this.type)
   */
  def _newInstance(collection: DBCollection): MongoCollection = new MongoCollection(collection)

  override protected def _typedValue(dbObj: DBObject): Option[DBObject] = Option(dbObj)

  override def head = headOption.get

  override def headOption = findOne()

  override def tail = find().skip(1).toIterable

  override def iterator = find()

  override def size = count().toInt

  override def toString() = name

}

/**
 * Concrete collection implementation for typed Cursor operations via Collection.setObjectClass et al
 * This is a special case collection for typed operations
 *
 * @version 2.0, 12/23/10
 * @since 1.0
 *
 */
trait MongoTypedCollection extends MongoCollectionBase {

}

class MongoGenericTypedCollection[A <: DBObject](val underlying: DBCollection) extends MongoTypedCollection {
  type T = A
  type CursorType = MongoGenericTypedCursor[A]

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
   * @return (MongoCollectionBase)
   */
  def _newCursor(cursor: DBCursor) = new MongoGenericTypedCursor[T](cursor)

  /**
   * _newInstance
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of THIS concrete implementation from a
   * Java collection.  Good with calls that return a new collection.
   *
   * @param  collection (DBCollection)
   * @return (this.type)
   */
  def _newInstance(collection: DBCollection) = new MongoGenericTypedCollection[T](collection)
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
  @deprecated("This method is NOT a part of public API and will be dropped in 2.8", "2.7")
  def generateIndexName[A <% DBObject](keys: A) = DBCollection.genIndexName(keys)

}
