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

// scalastyle:off file.size.limit number.of.methods

package com.mongodb.casbah

import scala.language.reflectiveCalls
import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.concurrent.duration.{Duration, MILLISECONDS}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.TypeImports.{DBEncoder, WriteResult}
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.TypeImports.DBObject
import com.mongodb.casbah.map_reduce.{MapReduceCommand, MapReduceResult}
import com.mongodb.{CommandResult, DBCollection, DBCursor, DBDecoderFactory, DBEncoderFactory, InsertOptions, MongoExecutionTimeoutException, ParallelScanOptions => JavaParallelScanOptions, AggregationOutput => _}

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

  def iterator: CursorType = find()

  /**
   * Returns the database this collection is a member of.
   * @return this collection's database
   */
  implicit val db: MongoDB = underlying.getDB.asScala

  /**
   * Forces creation of an index on a set of fields, if one does not already exist.
   * @param keys an object with a key set of the fields desired for the index
   */
  def createIndex[A](keys: A)(implicit ev$1: A => DBObject): Unit = underlying.createIndex(keys)

  /**
   * Creates an index on the field specified, if that index does not already exist.
   *
   * @param keys    a document that contains pairs with the name of the field or fields to index and order of the index
   * @param options a document that controls the creation of the index.
   */
  def createIndex[A, B](keys: A, options: B)(implicit ev$1: A => DBObject, ev$2: B => DBObject): Unit =
    underlying.createIndex(keys, options)

  /**
   * Forces creation of an ascending index on a field with the default options.
   *
   * @param name name of field to index on
   * @throws MongoException if the operation failed
   */
  def createIndex(name: String): Unit = underlying.createIndex(name)

  /**
   * Forces creation of an index on a set of fields, if one does not already exist.
   *
   * @param keys a document that contains pairs with the name of the field or fields to index and order of the index
   * @param name an identifier for the index. If null or empty, the default name will be used.
   * @throws MongoException if the operation failed
   */
  def createIndex[A](keys: A, name: String)(implicit ev$1: A => DBObject): Unit = underlying.createIndex(keys, name)

  /**
   * Forces creation of an index on a set of fields, if one does not already exist.
   *
   * @param keys   a document that contains pairs with the name of the field or fields to index and order of the index
   * @param name   an identifier for the index. If null or empty, the default name will be used.
   * @param unique if the index should be unique
   * @throws MongoException if the operation failed
   */
  def createIndex[A](keys: A, name: String, unique: Boolean)(implicit ev$1: A => DBObject): Unit =
    underlying.createIndex(keys, name, unique)

  /**
   * Find distinct values for a key
   *
   * @param key the key to find the distinct values for
   * @param query the query (optional)
   * @param readPrefs the [[com.mongodb.ReadPreference]] for the operation.
   * @tparam A The DBObject type
   * @return
   */
  def distinct[A](key: String, query: A = MongoDBObject.empty, readPrefs: ReadPreference = getReadPreference)(implicit ev$1: A => DBObject): mutable.Buffer[_] =
    underlying.distinct(key, query, readPrefs).asScala

  /**
   * Drops (deletes) this collection
   */
  def drop(): Unit = underlying.drop()

  /**
   * Drops (deletes) this collection
   */
  def dropCollection(): Unit = underlying.drop()

  def dropIndex[A](keys: A)(implicit ev$1: A => DBObject): Unit = underlying.dropIndex(keys)

  def dropIndex(name: String): Unit = underlying.dropIndex(name)

  /**
   * Drops all indices from this collection
   */
  def dropIndexes(): Unit = underlying.dropIndexes()

  def dropIndexes(name: String): Unit = underlying.dropIndexes()

  /**
   * Queries for all objects in this collection.
   * @return a cursor which will iterate over every object
   */
  def find(): CursorType = _newCursor(underlying.find)

  /**
   * Queries for an object in this collection.
   * @param ref object for which to search
   * @return an iterator over the results
   */
  def find[A](ref: A)(implicit ev$1: A => DBObject): CursorType = _newCursor(underlying.find(ref))

  /**
   * Queries for an object in this collection.
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
  def find[A, B](ref: A, keys: B)(implicit ev$1: A => DBObject, ev$2: B => DBObject): CursorType = _newCursor(underlying.find(ref, keys))

  /**
   * Finds an object.
   * @param ref query used to search
   * @param fields the fields of matching objects to return
   * @param numToSkip will not return the first <tt>numToSkip</tt> matches
   * @param batchSize if positive, is the # of objects per batch sent back from the db.  All objects that match will
   *                  be returned.  if batchSize < 0, its a hard limit, and only 1 batch will either batchSize or
   *                  the # that fit in a batch
   * @return the objects, if found
   */
  @deprecated("Use `find().skip().batchSize()`.", "2.7")
  def find[A, B](ref: A, fields: B, numToSkip: Int, batchSize: Int)(implicit ev$1: A => DBObject, ev$2: B => DBObject): CursorType =
    _newCursor(underlying.find(ref, fields).skip(numToSkip).batchSize(batchSize))

  /**
   * Returns a single object from this collection.
   * @return (Option[T]) Some() of the object found, or <code>None</code> if this collection is empty
   */
  def findOne(): Option[T] = _typedValue(underlying.findOne())

  /**
   * Returns a single object from this collection matching the query.
   *
   * @param o           the query object
   * @param fields      (optional) fields to return
   * @param orderBy     (optional) a document whose fields specify the attributes on which to sort the result set.
   * @param readPrefs   (optional)
   * @param maxTime     (optional) the maximum duration that the server will allow this operation to execute before killing it
   *
   * @return            (Option[T]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOne[A, B, C](
    o:         A              = MongoDBObject.empty,
    fields:    B              = MongoDBObject.empty,
    orderBy:   C              = MongoDBObject.empty,
    readPrefs: ReadPreference = getReadPreference,
    maxTime:   Duration       = Duration(0, MILLISECONDS)
  )(implicit ev$1: A => DBObject, ev$2: B => DBObject, ev$3: C => DBObject): Option[T] = {
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
  def findOneByID(id: AnyRef): Option[T] = _typedValue(underlying.findOne(id))

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
  def findOneByID[B](id: AnyRef, fields: B)(implicit ev$1: B => DBObject): Option[T] =
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
  def findAndModify[A, B](query: A, update: B)(implicit ev$1: A => DBObject, ev$2: B => DBObject): Option[T] =
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
  def findAndModify[A, B, C](query: A, sort: B, update: C)(implicit ev$1: A => DBObject, ev$2: B => DBObject, ev$3: C => DBObject): Option[T] =
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
   * @return the document as it was before the modifications, unless `returnNew` is true, in which case it returns the document
   *         after the changes were made
   */
  def findAndModify[A, B, C, D](query: A, fields: B, sort: C,
                                                                                remove: Boolean, update: D,
                                                                                returnNew: Boolean, upsert: Boolean)(implicit ev$1: A => DBObject, ev$2: B => DBObject, ev$3: C => DBObject, ev$4: D => DBObject): Option[T] =
    _typedValue(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert))

  /**
   * Atomically modify and return a single document. By default, the returned document does not include the modifications made on the
   * update.
   *
   * @param query     specifies the selection criteria for the modification
   * @param fields    a subset of fields to return
   * @param sort      determines which document the operation will modify if the query selects multiple documents
   * @param remove    when true, removes the selected document
   * @param returnNew when true, returns the modified document rather than the original
   * @param update    the modifications to apply
   * @param upsert    when true, operation creates a new document if the query returns no documents
   * @param writeConcern the write concern to apply to this operation
   * @return the document as it was before the modifications, unless `returnNew` is true, in which case it returns the document
   *         after the changes were made
   * @throws WriteConcernException if the write failed due some other failure specific to the update command
   * @throws MongoException if the operation failed for some other reason
   * @since 3.1.0
   */
  def findAndModify[A, B, C, D](query: A, fields: B, sort: C,
                                                                                remove: Boolean, update: D,
                                                                                returnNew: Boolean, upsert: Boolean,
                                                                                writeConcern: WriteConcern)(implicit ev$1: A => DBObject, ev$2: B => DBObject, ev$3: C => DBObject, ev$4: D => DBObject): Option[T] =
    _typedValue(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert, writeConcern))

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
   * @return the document as it was before the modifications, unless `returnNew` is true, in which case it returns the document
   *         after the changes were made
   */
  def findAndModify[A, B, C, D](query: A, fields: B, sort: C,
                                                                                remove: Boolean, update: D,
                                                                                returnNew: Boolean, upsert: Boolean,
                                                                                maxTime: Duration)(implicit ev$1: A => DBObject, ev$2: B => DBObject, ev$3: C => DBObject, ev$4: D => DBObject): Option[T] =
    _typedValue(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert,
      maxTime.length, maxTime.unit))

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
   * @param writeConcern the write concern to apply to this operation
   * @throws WriteConcernException if the write failed due some other failure specific to the update command
   * @throws MongoException if the operation failed for some other reason
   * @since 3.1.0
   */
  def findAndModify[A, B, C, D](query: A, fields: B, sort: C,
                                                                                remove: Boolean, update: D,
                                                                                returnNew: Boolean, upsert: Boolean,
                                                                                maxTime:      Duration,
                                                                                writeConcern: WriteConcern)(implicit ev$1: A => DBObject, ev$2: B => DBObject, ev$3: C => DBObject, ev$4: D => DBObject): Option[T] =
    _typedValue(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert,
      maxTime.length, maxTime.unit, writeConcern))

  /**
   * Finds the first document in the query and updates it.
   *
   * @note bypassDocumentValidation requires MongoDB 3.2 or greater
   * @param query       query to match
   * @param fields      fields to be returned
   * @param sort        sort to apply before picking first document
   * @param remove      if true, document found will be removed
   * @param update      update to apply
   * @param returnNew   if true, the updated document is returned, otherwise the old document is returned (or it would be lost forever)
   * @param upsert      do upsert (insert if document not present)
   * @param bypassDocumentValidation whether to bypass document validation.
   * @param maxTime the maximum duration that the server will allow this operation to execute before killing it
   * @throws WriteConcernException if the write failed due some other failure specific to the update command
   * @throws MongoException if the operation failed for some other reason
   * @since 3.1.0
   */
  def findAndModify[A, B, C, D](query: A, fields: B, sort: C,
                                                                                remove: Boolean, update: D,
                                                                                returnNew: Boolean, upsert: Boolean,
                                                                                bypassDocumentValidation: Boolean,
                                                                                maxTime:                  Duration)(implicit ev$1: A => DBObject, ev$2: B => DBObject, ev$3: C => DBObject, ev$4: D => DBObject): Option[T] =
    _typedValue(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert, bypassDocumentValidation,
      maxTime.length, maxTime.unit))

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
   * @param bypassDocumentValidation whether to bypass document validation.
   * @param maxTime the maximum duration that the server will allow this operation to execute before killing it
   * @param writeConcern the write concern to apply to this operation
   * @throws WriteConcernException if the write failed due some other failure specific to the update command
   * @throws MongoException if the operation failed for some other reason
   * @since 3.1.0
   */
  def findAndModify[A, B, C, D](query: A, fields: B, sort: C,
                                                                                remove: Boolean, update: D,
                                                                                returnNew: Boolean, upsert: Boolean,
                                                                                bypassDocumentValidation: Boolean,
                                                                                maxTime:                  Duration,
                                                                                writeConcern:             WriteConcern)(implicit ev$1: A => DBObject, ev$2: B => DBObject, ev$3: C => DBObject, ev$4: D => DBObject): Option[T] =
    _typedValue(underlying.findAndModify(query, fields, sort, remove, update, returnNew, upsert, bypassDocumentValidation,
      maxTime.length, maxTime.unit, writeConcern))

  /**
   * Finds the first document in the query and removes it.
   * @return the removed document
   */
  def findAndRemove[A](query: A)(implicit ev$1: A => DBObject): Option[T] =
    _typedValue(underlying.findAndRemove(query))

  /**
   * Find a collection that is prefixed with this collection's name.
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
  def getCollection(n: String): MongoCollection = underlying.getCollection(n).asScala

  /**
   * Find a collection that is prefixed with this collection's name.
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
  def collection(n: String): MongoCollection = underlying.getCollection(n).asScala

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
  def getCount[A, B](query: A = MongoDBObject.empty, fields: B = MongoDBObject.empty,
                                             limit: Long = 0, skip: Long = 0,
                                             readPrefs: ReadPreference = getReadPreference,
                                             maxTime:   Duration       = Duration(0, MILLISECONDS))(implicit ev$1: A => DBObject, ev$2: B => DBObject): Int = {
    underlying.find(query, fields)
      .skip(skip.toInt)
      .limit(limit.toInt)
      .setReadPreference(readPrefs)
      .maxTime(maxTime.length, maxTime.unit)
      .count()
  }

  /**
   * Returns the database this collection is a member of.
   * @return this collection's database
   */
  def getDB: MongoDB = underlying.getDB.asScala

  /**
   * Returns the full name of this collection, with the database name as a prefix.
   * @return  the name of this collection
   */
  def getFullName: String = underlying.getFullName

  /**
   * Returns the full name of this collection, with the database name as a prefix.
   * @return  the name of this collection
   */
  def fullName: String = getFullName

  /**
   * Return a list of the indexes for this collection.  Each object
   * in the list is the "info document" from MongoDB
   *
   * @return list of index documents
   */
  def getIndexInfo: mutable.Buffer[DBObject] = underlying.getIndexInfo.asScala

  /**
   * Return a list of the indexes for this collection.  Each object
   * in the list is the "info document" from MongoDB
   *
   * @return list of index documents
   */
  def indexInfo: mutable.Buffer[DBObject] = getIndexInfo

  def getName: String = underlying.getName

  def name: String = getName

  /**
   * Gets the default class for objects in the collection
   * @return the class
   */
  def getObjectClass: Class[_] = underlying.getObjectClass

  /**
   * Gets the default class for objects in the collection
   * @return the class
   */
  def objectClass: Class[_] = getObjectClass

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
  def setObjectClass[A <: DBObject: Manifest](c: Class[A]): MongoGenericTypedCollection[A] = {
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
  def objectClass_=[A <: DBObject: Manifest](c: Class[A]): MongoGenericTypedCollection[A] = setObjectClass(c)

  def stats: CommandResult = getStats

  def getStats: CommandResult = underlying.getStats

  // scalastyle:off null
  /**
   * Applies a group operation
   *
   * @param key the key to group <code>{ a : true }</code>
   * @param cond optional condition on query
   * @param reduce javascript reduce function
   * @param initial initial value for first match on a key
   * @param finalize An optional function that can operate on the result(s) of the reduce function.
   * @param readPrefs ReadPreferences for this command
   * @return The results of the group
   */
  def group[A, B, C](key: A, cond: B, initial: C,
                                                         reduce: String, finalize: String = null,
                                                         readPrefs: ReadPreference = getReadPreference)(implicit ev$1: A => DBObject, ev$2: B => DBObject, ev$3: C => DBObject): Iterable[T] = {
    underlying.group(key, cond, initial, reduce, finalize, readPrefs).map(_._2.asInstanceOf[T])
  }

  // scalastyle:on null

  /**
   * Saves document(s) to the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param doc  array of documents (<% DBObject) to save
   * @param concern the WriteConcern for the insert
   */
  def insert[A](doc: A, concern: com.mongodb.WriteConcern)(implicit dbObjView: A => DBObject): WriteResult =
    insert(doc)(dbObjView, concern = concern)

  /**
   * Saves document(s) to the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param docs  array of documents (<% DBObject) to save
   *              TODO - Wrapper for WriteResult?
   */
  def insert[A](docs: A*)(implicit dbObjView: A => DBObject, concern: com.mongodb.WriteConcern = writeConcern,
                          encoder: DBEncoder = customEncoderFactory.map(_.create).orNull): WriteResult =
    insert(new InsertOptions().writeConcern(writeConcern).dbEncoder(encoder), docs: _*)

  /**
   * Saves document(s) to the database.
   * if doc doesn't have an _id, one will be added
   * you can get the _id that was added from doc after the insert
   *
   * @param docs  array of documents (<% DBObject) to save
   * @param insertOptions the insertOptions
   */
  def insert[A](insertOptions: InsertOptions, docs: A*)(implicit dbObjView: A => DBObject): WriteResult = {
    val b = new scala.collection.mutable.ArrayBuilder.ofRef[DBObject]
    b.sizeHint(docs.size)
    for { x <- docs } b += dbObjView(x)
    underlying.insert(b.result().toList.asJava, insertOptions)
  }

  def isCapped: Boolean = underlying.isCapped

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
  def aggregate[A](pipeline: Iterable[A])(implicit ev$1: A => DBObject): AggregationOutput =
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
  def aggregate[A](pipeline: Iterable[A], options: AggregationOptions)(implicit ev$1: A => DBObject): Cursor =
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
  def aggregate[A](pipeline: Iterable[A], readPreference: ReadPreference)(implicit ev$1: A => DBObject): AggregationOutput =
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
  def aggregate[A](pipeline: Iterable[A], options: AggregationOptions, readPreference: ReadPreference)(implicit ev: A => DBObject): Cursor =
    underlying.aggregate(pipeline.map(_.asInstanceOf[DBObject]).toList.asJava, options, readPreference).asScala

  /**
   * Return the explain plan for the aggregation pipeline.
   *
   * @param pipeline the aggregation pipeline to explain
   * @param options the options to apply to the aggregation
   * @return the command result.  The explain output may change from release to
   *         release, so best to simply log this.
   */
  def explainAggregate[A](pipeline: Iterable[A], options: AggregationOptions)(implicit ev: A => DBObject): mutable.Map[String, AnyRef] =
    underlying.explainAggregate(pipeline.map(_.asInstanceOf[DBObject]).toList.asJava, options).asScala

  /**
   * Return a list of cursors over the collection that can be used to scan it in parallel.
   *
   * '''Note:''' As of MongoDB 2.6, this method will work against a mongod, but not a mongos.
   *
   * @param options the parallel scan options
   * @return a list of cursors, whose size may be less than the number requested
   * @since 2.7
   */
  def parallelScan(options: ParallelScanOptions): mutable.Buffer[Cursor] = {
    val builder = JavaParallelScanOptions.builder()
    builder.numCursors(options.numCursors)
    builder.batchSize(options.batchSize)
    builder.readPreference(options.readPreference.getOrElse(getReadPreference))

    underlying.parallelScan(builder.build()).asScala.map(_.asScala)
  }

  /**
   * Creates a builder for an ordered bulk operation.  Write requests included in the bulk operations will be executed in order,
   * and will halt on the first failure.
   *
   * @return the builder
   *
   * @since 2.7
   */
  def initializeOrderedBulkOperation: BulkWriteOperation = BulkWriteOperation(underlying.initializeOrderedBulkOperation())

  /**
   * Creates a builder for an unordered bulk operation. Write requests included in the bulk operation will be executed in an undefined
   * order, and all requests will be executed even if some fail.
   *
   * @return the builder
   *
   * @since 2.7
   */
  def initializeUnorderedBulkOperation: BulkWriteOperation = BulkWriteOperation(underlying.initializeUnorderedBulkOperation())

  // scalastyle:off parameter.number
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
  def mapReduce(
    mapFunction:      JSFunction,
    reduceFunction:   JSFunction,
    output:           MapReduceOutputTarget,
    query:            Option[DBObject]      = None,
    sort:             Option[DBObject]      = None,
    limit:            Option[Int]           = None,
    finalizeFunction: Option[JSFunction]    = None,
    jsScope:          Option[DBObject]      = None,
    verbose:          Boolean               = false,
    maxTime:          Option[Duration]      = None
  ): MapReduceResult = {
    val cmd = MapReduceCommand(name, mapFunction, reduceFunction, output, query, sort, limit,
      finalizeFunction, jsScope, verbose, maxTime)
    mapReduce(cmd)
  }

  // scalastyle:on parameter.number

  /**
   * mapReduce execute a mapReduce against this collection.
   *
   * Throws a MongoExecutionTimeoutException if exceeds max duration limit otherwise returns a MapReduceResult
   *
   * @param cmd the MapReduceCommand
   */
  def mapReduce(cmd: MapReduceCommand): MapReduceResult = {
    val result = getDB.command(cmd.toDBObject)

    // Normally we catch MapReduce Errors and return them into a MapReduceResult
    // However, we we now preserve a MongoExecutionTimeoutException to keep in line with other methods
    try {
      result.throwOnError()
    } catch {
      case e: MongoExecutionTimeoutException => throw e
      case t: Throwable                      => true
    }
    MapReduceResult(result)
  }

  /**
   * Removes objects from the database collection.
   * @param o the object that documents to be removed must match
   * @param concern WriteConcern for this operation
   *                TODO - Wrapper for WriteResult?
   */
  def remove[A](o: A, concern: com.mongodb.WriteConcern = getWriteConcern)(implicit
    dbObjView: A => DBObject,
                                                                           encoder: DBEncoder = customEncoderFactory.map(_.create).orNull): WriteResult =
    underlying.remove(dbObjView(o), concern, encoder)

  /**
   * Saves an object to this collection.
   * @param o the <code>DBObject</code> to save
   *          will add <code>_id</code> field to o if needed
   *          TODO - Wrapper for WriteResult?
   */
  def save[A](o: A, concern: com.mongodb.WriteConcern = getWriteConcern)(implicit dbObjView: A => DBObject): WriteResult =
    underlying.save(dbObjView(o), concern)

  /**
   * Set hint fields for this collection.
   * @param docs a list of <code>DBObject</code>s to be used as hints
   */
  def setHintFields[A](docs: List[A])(implicit ev$1: A => DBObject) {
    val b = List.newBuilder[DBObject]
    for { x <- docs } b += x
    underlying.setHintFields(b.result().asJava)
  }

  /**
   * Set hint fields for this collection.
   * @param docs a list of <code>DBObject</code>s to be used as hints
   */
  def hintFields_=[A](docs: List[A])(implicit ev$1: A => DBObject): Unit = setHintFields(docs)

  def setInternalClass[A <: DBObject](path: String, c: Class[A]): Unit = underlying.setInternalClass(path, c)

  def internalClass_=[A <: DBObject](path: String, c: Class[A]): Unit = setInternalClass(path, c)

  override def toString: String = underlying.toString

  /**
   * Performs an update operation.
   * @param q search query for old object to update
   * @param o object with which to update `q`
   */
  def update[A, B](q: A, o: B, upsert: Boolean = false, multi: Boolean = false,
                   concern:                  com.mongodb.WriteConcern = this.writeConcern,
                   bypassDocumentValidation: Option[Boolean]          = None)(implicit queryView: A => DBObject, objView: B => DBObject,
                                                                              encoder: DBEncoder = customEncoderFactory.map(_.create).orNull): WriteResult = {
    bypassDocumentValidation match {
      case None                   => underlying.update(queryView(q), objView(o), upsert, multi, concern, encoder)
      case Some(bypassValidation) => underlying.update(queryView(q), objView(o), upsert, multi, concern, bypassValidation, encoder)
    }
  }

  /**
   * Perform a multi update
   * @param q search query for old object to update
   * @param o object with which to update <tt>q</tt>
   */
  @deprecated("In the face of default arguments this is a bit silly. Please use `update(multi=True)`.", "2.3.0")
  def updateMulti[A, B](q: A, o: B)(implicit ev$1: A => DBObject, ev$2: B => DBObject): WriteResult = underlying.updateMulti(q, o)

  override def hashCode(): Int = underlying.hashCode

  /**
   * Checks if this collection is equal to another object.
   * @param obj object with which to compare this collection
   * @return if the two collections are the same object
   */
  override def equals(obj: Any): Boolean = obj match {
    case other: MongoCollectionBase => underlying.equals(other.underlying)
    case _                          => false
  }

  def count[A, B](query: A = MongoDBObject.empty, fields: B = MongoDBObject.empty,
                                          limit: Long = 0, skip: Long = 0, readPrefs: ReadPreference = getReadPreference,
                                          maxTime: Duration = Duration(0, MILLISECONDS))(implicit ev$1: A => DBObject, ev$2: B => DBObject): Int =
    getCount(query, fields, limit, skip, readPrefs, maxTime)

  // scalastyle:off method.name
  /**
   * Save an object to the Collection
   *
   * @param x object to save to the collection
   */
  def +=[A](x: A)(implicit ev$1: A => DBObject): WriteResult = save(x)

  /**
   * Remove a matching object from the collection
   *
   * @param x object to remove from the collection
   */
  def -=[A](x: A)(implicit ev$1: A => DBObject): WriteResult = remove(x)

  // scalastyle:on method.name

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
  def setWriteConcern(concern: WriteConcern): Unit = underlying.setWriteConcern(concern)

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
  def writeConcern_=(concern: WriteConcern): Unit = setWriteConcern(concern)

  /**
   *
   * get the write concern for this database,
   * which is used for writes to any collection in this database.
   * See the documentation for [[com.mongodb.WriteConcern]] for more info.
   *
   * @see WriteConcern
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def getWriteConcern: WriteConcern = underlying.getWriteConcern

  /**
   *
   * get the write concern for this database,
   * which is used for writes to any collection in this database.
   * See the documentation for [[com.mongodb.WriteConcern]] for more info.
   *
   * @see WriteConcern
   * @see http://www.thebuzzmedia.com/mongodb-single-server-data-durability-guide/
   */
  def writeConcern: WriteConcern = getWriteConcern

  /**
   * Sets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for [[com.mongodb.casbah.ReadPreference]] for more information.
   *
   * @param pref Read Preference to use
   */
  def readPreference_=(pref: ReadPreference): Unit = setReadPreference(pref)

  /**
   * Sets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for [[com.mongodb.casbah.ReadPreference]] for more information.
   *
   * @param pref Read Preference to use
   */
  def setReadPreference(pref: ReadPreference): Unit = underlying.setReadPreference(pref)

  /**
   * Gets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for [[com.mongodb.casbah.ReadPreference]] for more information.
   */
  def readPreference: ReadPreference = getReadPreference

  /**
   * Gets the read preference for this collection. Will be used as default for
   * reads from any collection in this collection. See the
   * documentation for [[com.mongodb.casbah.ReadPreference]] for more information.
   */
  def getReadPreference: ReadPreference = underlying.getReadPreference

  /**
   * Manipulate Network Options
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def addOption(option: Int): Unit = underlying.addOption(option)

  /**
   * Manipulate Network Options
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def resetOptions(): Unit = underlying.resetOptions() // use parens because this side-effects

  /**
   * Manipulate Network Options
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def getOptions: Int = underlying.getOptions

  /**
   * Manipulate Network Options
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def options: Int = getOptions

  /**
   * Sets queries to be OK to run on slave nodes.
   */
  @deprecated("Replaced with `ReadPreference.SECONDARY`", "2.3.0")
  @SuppressWarnings(Array("deprecation"))
  def slaveOk(): Unit = underlying.slaveOk() // use parens because this side-effects

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

  // scalastyle:off method.name
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

  // scalastyle:on method.name
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

  // scalastyle:off method.name
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
  def _newCursor(cursor: DBCursor): MongoCursor = new MongoCursor(cursor)

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

  // scalastyle:on method.name

  override def head: T = headOption.get

  override def headOption: Option[T] = findOne()

  override def tail: Iterable[DBObject] = find().skip(1).toIterable

  override def iterator: CursorType = find()

  override def size: Int = count()

  override def toString(): String = name

}

/**
 * Concrete collection implementation for typed Cursor operations via Collection.setObjectClass et al
 * This is a special case collection for typed operations
 *
 * @version 2.0, 12/23/10
 * @since 1.0
 *
 */
trait MongoTypedCollection extends MongoCollectionBase {}

class MongoGenericTypedCollection[A <: DBObject](val underlying: DBCollection) extends MongoTypedCollection {
  type T = A
  type CursorType = MongoGenericTypedCursor[A]

  // scalastyle:off method.name
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
  def _newCursor(cursor: DBCursor): MongoGenericTypedCursor[T] = new MongoGenericTypedCursor[T](cursor)

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
  def _newInstance(collection: DBCollection): MongoGenericTypedCollection[T] =
    new MongoGenericTypedCollection[T](collection)

  // scalastyle:on method.name
}

/**
 * Helper object for some static methods
 */
object MongoCollection extends Logging {

}
