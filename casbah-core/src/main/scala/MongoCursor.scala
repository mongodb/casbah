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

import com.mongodb.DBCursor
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging

import scala.concurrent.duration.Duration

// scalastyle:off number.of.methods

/**
 * Scala wrapper for Mongo DBCursors,
 * including ones which return custom DBObject subclasses.
 *
 * This is a rewrite of the Casbah 1.0 approach which was rather
 * naive and unnecessarily complex.... formerly was MongoCursorWrapper
 *
 * @version 2.0, 12/23/10
 * @since 2.0
 *
 * @tparam T (DBObject or subclass thereof)
 */
trait MongoCursorBase extends Logging {

  type T <: DBObject

  val underlying: DBCursor

  /**
   * next
   *
   * Iterator increment.
   *
   * TODO: The cast to T should be examined for sanity/safety.
   *
   * {@inheritDoc}
   *
   * @return The next element in the cursor
   */
  def next(): T = underlying.next.asInstanceOf[T]

  /**
   * hasNext
   *
   * Is there another element in the cursor?
   *
   * {@inheritDoc}
   *
   * @return (Boolean Next)
   */
  def hasNext: Boolean = underlying.hasNext

  /**
   * sort
   *
   * Sort this cursor's elements
   *
   * @param  orderBy (A) The fields on which to sort
   * @tparam A  A view of DBObject to sort by
   * @return A cursor pointing to the first element of the sorted results
   */
  def sort[A](orderBy: A)(implicit ev$1: A => DBObject): this.type = {
    // The Java code returns a copy of itself (via _this_) so no clone/_newInstance
    underlying.sort(orderBy)
    this
  }

  /**
   * count
   *
   * The DBCursor's count of elements in the query, passed through
   * from the Java object.  Note that Scala Iterator[_] base trait
   * has a count method which tests predicates and you should
   * be careful about using that.
   *
   * <b>NOTE:</b> count() ignores any skip/limit settings on a cursor;
   * it is the count of the ENTIRE query results.
   * If you want to get a count post-skip/limit
   * you must use size()
   *
   * @see size()
   *
   * @return Int indicating the number of elements returned by the query
   * @throws MongoException()
   */
  def count(): Int = underlying.count() /* calls the db */

  /**
   * Manipulate Query Options
   *
   * Adds an option - see Bytes.QUERYOPTION_* for list
   * TODO - Create Scala version of Bytes.QUERYOPTION_*
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def option_=(option: Int): Unit = underlying.addOption(option)

  /**
   * Manipulate Query Options
   *
   * Gets current option settings - see Bytes.QUERYOPTION_* for list
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def option: Int = underlying.getOptions

  /**
   * Manipulate Query Options
   *
   * Resets options to default.
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def resetOptions(): DBCursor = underlying.resetOptions() // use parens because this side-effects

  /**
   * Manipulate Query Options
   *
   * Gets current option settings - see Bytes.QUERYOPTION_* for list
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def options: Int = underlying.getOptions

  /**
   * Manipulate Query Options
   *
   * Sets current option settings - see Bytes.QUERYOPTION_* for list
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def options_=(opts: Int): Unit = underlying.setOptions(opts)

  /**
   * hint
   *
   * Provide the Database a hint of which indexed fields of a collection to use
   * to improve performance.
   *
   * @param  indexKeys (A) A DBObject of the index names as keys
   * @tparam A A view of DBObject to use for the indexKeys
   * @return the same DBCursor, useful for chaining operations
   */
  def hint[A](indexKeys: A)(implicit ev$1: A => DBObject): this.type = {
    underlying.hint(indexKeys)
    this
  }

  /**
   * hint
   *
   * Provide the Database a hint of an indexed field of a collection to use
   * to improve performance.
   *
   * @param  indexName (String) The name of an index
   * @return the same DBCursor, useful for chaining operations
   */
  def hint(indexName: String): this.type = {
    underlying.hint(indexName)
    this
  }

  /**
   * Use snapshot mode for the query. Snapshot mode prevents the cursor from returning a document more than once because an intervening
   * write operation results in a move of the document. Even in snapshot mode, documents inserted or deleted during the lifetime of the
   * cursor may or may not be returned.  Currently, snapshot mode may not be used with sorting or explicit hints.
   *
   * @return the same DBCursor, useful for chaining operations
   */
  def snapshot(): this.type = {
    // The Java code returns a copy of itself (via _this_) so no clone/_newInstance
    underlying.snapshot() // parens for side-effecting
    this
  }

  /**
   * explain
   *
   * Returns an object containing basic information about the execution
   * of the query that created this cursor.
   * This creates an instance of CursorExplanation which is a custom
   * dbObject with the key/value pairs:
   * - cursor = Cursor Type
   * - nscanned = Number of items examined by Mongo for this query
   * - nscannedObjects = Number of objects examined by Mongo
   * - n = the number of records which Mongo returned
   * - millis = how long it took Mongo to execute the query
   * - nYields = number of times this query yielded the read lock to let writes in
   * - indexBounds = the index boundaries used.
   *
   * CursorExplanation provides utility methods to access these fields.
   *
   * @see http://dochub.mongodb.org/core/explain
   * @return an instance of CursorExplanation
   */
  def explain: CursorExplanation = new CursorExplanation(underlying.explain)

  /**
   * limit
   *
   * Limits the number of elements returned.
   *
   * <b>NOTE:</b> Specifying a <em>negative number</em> instructs
   * the server to retrun that number of items and close the cursor.
   * It will only return what can fit into a <em>single 4MB response</em>
   *
   * @param  n (Int)  The number of elements to return
   * @return A cursor pointing to the first element of the limited results
   *
   * @see http://dochub.mongodb.org/core/limit
   */
  def limit(n: Int): this.type = {
    underlying.limit(n)
    this
  }

  /**
   * skip
   *
   * Discards a given number of elements at the beginning of the cursor.
   *
   * @param  n (Int)  The number of elements to skip
   * @return A cursor pointing to the first element of the results
   *
   * @see http://dochub.mongodb.org/core/skip
   */
  def skip(n: Int): this.type = {
    underlying.skip(n)
    this
  }

  /**
   * cursorId
   *
   * @return A long representing the cursorID on the server; 0 = no cursor
   *
   */
  def cursorId: Long = underlying.getCursorId

  /**
   * close
   *
   * Kill the current cursor on the server
   */
  def close(): Unit = underlying.close() // parens for side-effect

  /**
   * slaveOk
   *
   * Makes this query OK to run on a non-master node.
   */
  @deprecated("Replaced with `ReadPreference.SECONDARY`", "2.3.0")
  @SuppressWarnings(Array("deprecation"))
  def slaveOk(): DBCursor = underlying.slaveOk() // parens for side-effect

  /**
   * numSeen
   *
   * Returns the number of objects through which this cursor has iterated,
   * as tracked by the java driver.
   *
   * @return The number of objects seen
   */
  def numSeen: Int = underlying.numSeen

  /**
   * batchSize
   *
   * Limits the number of elements returned in one batch.
   *
   * @param  n (Int) The number of elements to return in a batch
   * @return the same DBCursor, useful for chaining operations
   */
  def batchSize(n: Int): MongoCursorBase = {
    underlying.batchSize(n)
    this
  }

  def keysWanted: DBObject = underlying.getKeysWanted

  def query: DBObject = underlying.getQuery

  /**
   * "Special" Operators for cursors
   *
   * adds a special operator like \$maxScan or \$returnKey
   * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-Specialoperators
   *      { @inheritDoc}
   * @return the same DBCursor, useful for chaining operations
   */
  def addSpecial(name: String, o: Any): this.type = {
    // The Java code returns a copy of itself (via _this_) so no clone/_newInstance
    underlying.addSpecial(name, o.asInstanceOf[AnyRef])
    this
  }

  // scalastyle:off method.name
  /**
   * \$returnKey
   *
   * Sets a special operator of \$returnKey
   * If true, returns ONLY the index key.
   * Defaults to true if you just call \$returnKey
   *
   * @param  bool (Boolean = true)
   * @return the same DBCursor, useful for chaining operations
   */
  def $returnKey(bool: Boolean = true): this.type = addSpecial("$returnKey", bool)

  /**
   * \$maxScan
   *
   * Sets a special operator of \$maxScan
   * Which defines the max number of items to scan.
   *
   * @param  max (A)
   * @tparam A : Numeric
   * @return the same DBCursor, useful for chaining operations
   */
  def $maxScan[A: Numeric](max: T): this.type = addSpecial("$maxScan", max)

  /**
   * \$query
   *
   * Sets a special operator of \$query
   * Which defines the query for this cursor.
   *
   * This is the same as running find() on a Collection with the query.
   *
   * @param  q (DBObject)
   * @return the same DBCursor, useful for chaining operations
   */
  def $query[A](q: A)(implicit ev$1: A => DBObject): this.type = addSpecial("$query", q)

  /**
   * \$orderby
   *
   * Sets a special operator of \$orderby
   * which defines the sort spec for this cursor.
   *
   * This is the same as calling sort on the cursor.
   *
   * @param  obj (DBObject)
   * @return the same DBCursor, useful for chaining operations
   */
  def $orderby[A](obj: A)(implicit ev$1: A => DBObject): this.type = addSpecial("$orderby", obj)

  /**
   * \$explain
   *
   * Sets a special operator of \$explain
   * which, if true, explains the query instead of returning results.
   *
   * This is the same as calling the explain() method on the cursor.
   *
   * @param  bool (Boolean = true)
   * @return the same DBCursor, useful for chaining operations
   */
  def $explain(bool: Boolean = true): this.type = addSpecial("$explain", bool)

  /**
   * \$snapshot
   *
   * Sets a special operator of \$snapshot
   * which, if True, sets snapshot mode on the query.
   *
   * This is the same as calling the snapshot() method on the cursor.
   *
   * @param  bool (Boolean = true)
   * @return the same DBCursor, useful for chaining operations
   */
  def $snapshot(bool: Boolean = true): this.type = addSpecial("$snapshot", bool)

  /**
   * \$min
   *
   * Sets minimum index bounds - commonly paired with \$max
   *
   * @param  obj (DBObject)
   * @see http://www.mongodb.org/display/DOCS/min+and+max+Query+Specifiers
   *
   * @return the same DBCursor, useful for chaining operations
   */
  def $min[A](obj: A)(implicit ev$1: A => DBObject): this.type = addSpecial("$min", obj)

  /**
   * \$max
   *
   * Sets maximum index bounds - commonly paired with \$max
   *
   * @param  obj (DBObject)
   * @see http://www.mongodb.org/display/DOCS/max+and+max+Query+Specifiers
   *
   * @return the same DBCursor, useful for chaining operations
   */
  def $max[A](obj: A)(implicit ev$1: A => DBObject): this.type = addSpecial("$max", obj)

  /**
   * \$showDiskLoc
   *
   * Sets a special operator \$showDiskLoc which, if true,
   * shows the disk location of results.
   *
   * @param  bool (Boolean = true)
   * @return the same DBCursor, useful for chaining operations
   */
  def $showDiskLoc(bool: Boolean = true): this.type = addSpecial("$showDiskLoc", bool)

  /**
   * \$hint
   *
   * Sets a special operator \$hint which
   * forces the query to use a given index.
   *
   * This is the same as calling hint() on the cursor.
   *
   * @param obj (DBObject)
   * @return the same DBCursor, useful for chaining operations
   */
  def $hint[A](obj: A)(implicit ev$1: A => DBObject): this.type = addSpecial("$hint", obj)

  /**
   * _newInstance
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of THIS concrete implementation from a
   * Java cursor.  Good with cursor calls that return a new cursor.
   *
   * @param  cursor (DBCursor)
   * @return (this.type)
   */
  def _newInstance(cursor: DBCursor): MongoCursorBase

  // scalastyle:on method.name

  /**
   * copy
   *
   * Creates a new copy of an existing database cursor.
   * The new cursor is an iterator even if the original
   * was an array.
   *
   * @return The new cursor
   */
  def copy(): MongoCursorBase = _newInstance(underlying.copy()) // parens for side-effects

}

/**
 * Concrete cursor implementation expecting standard DBObject operation
 * This is the version of MongoCursorBase you should expect to use in most cases.
 *
 * @version 2.0, 12/23/10
 * @since 1.0
 *
 * @param  underlying (com.mongodb.DBCursor)
 * @tparam DBObject
 */
class MongoCursor(val underlying: DBCursor) extends MongoCursorBase with Iterator[DBObject] {

  type T = DBObject

  /**
   * size
   *
   * The DBCursor's count of elements in the query,
   * AFTER the application of skip/limit, passed through
   * from the Java object.
   *
   * <b>NOTE:</b> size() takes into account any skip/limit settings on a cursor;
   * it is the size of just the window.
   * If you want to get a count of the entire query ignoring skip/limit
   * you must use count()
   *
   * @see count()
   *
   * @return Int indicating the number of elements returned by the query after skip/limit
   * @throws MongoException if errors
   */
  override def size: Int = underlying.size

  // scalastyle:off method.name
  /**
   * _newInstance
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of THIS concrete implementation from a
   * Java cursor.  Good with cursor calls that return a new cursor.
   *
   * @param  cursor (DBCursor)
   * @return (this.type)
   */
  def _newInstance(cursor: DBCursor): MongoCursor = new MongoCursor(cursor)
  // scalastyle:off method.name

  /**
   * copy
   *
   * Creates a new copy of an existing database cursor.
   * The new cursor is an iterator even if the original
   * was an array.
   *
   * @return The new cursor
   */
  override def copy(): MongoCursor = _newInstance(underlying.copy()) // parens for side-effects

  /**
   * Set the maximum execution time for operations on this cursor.
   *
   * @param maxTime  the maximum time that the server will allow the query to run, before killing the operation. A non-zero value
   *                 requires a server version >= 2.6
   * @return the new cursor
   *
   * @since 2.7
   */
  def maxTime(maxTime: Duration): MongoCursor = _newInstance(underlying.maxTime(maxTime.length, maxTime.unit))

  /**
   * @return the first matching document
   *
   * @since 2.7
   */
  def one(): DBObject = underlying.one()
}

object MongoCursor extends Logging {
  /**
   * Initialize a new cursor with your own custom settings
   *
   * @param  collection (MongoCollection)  collection to use
   * @param  query (Q) Query to perform
   * @param  keys (K) Keys to return from the query
   * @return (instance) A new MongoCursor
   */
  def apply[T <: DBObject: Manifest](collection: MongoCollectionBase, query: DBObject, keys: DBObject): MongoCursorBase =
    apply(collection, query, keys, collection.readPreference)

  /**
   * Initialize a new cursor with your own custom settings
   *
   * @param  collection (MongoCollection)  collection to use
   * @param  query (Q) Query to perform
   * @param  keys (K) Keys to return from the query
   * @param  readPref the read preference for the cursor
   * @return (instance) A new MongoCursor
   */
  def apply[T <: DBObject: Manifest](collection: MongoCollectionBase, query: DBObject, keys: DBObject, readPref: ReadPreference): MongoCursorBase = {
    val cursor = new DBCursor(collection.underlying, query, keys, readPref)

    if (manifest[T] == manifest[DBObject]) new MongoCursor(cursor)
    else new MongoGenericTypedCursor[T](cursor)
  }
}

/**
 * Concrete cursor implementation for typed Cursor operations via Collection.setObjectClass
 * This is a special case cursor for typed operations.
 *
 * @version 2.0, 12/23/10
 * @since 1.0
 *
 * @param  underlying (com.mongodb.DBCollection)
 * @tparam A A Subclass of DBObject
 */
class MongoGenericTypedCursor[A <: DBObject](val underlying: DBCursor) extends MongoCursorBase {
  type T = A

  // scalastyle:off method.name
  /**
   * _newInstance
   *
   * Utility method which concrete subclasses
   * are expected to implement for creating a new
   * instance of THIS concrete implementation from a
   * Java cursor.  Good with cursor calls that return a new cursor.
   *
   * @param  cursor (DBCursor)
   * @return (this.type)
   */
  def _newInstance(cursor: DBCursor): MongoGenericTypedCursor[T] = new MongoGenericTypedCursor[T](cursor)
  // scalastyle:on method.name

  /**
   * copy
   *
   * Creates a new copy of an existing database cursor.
   * The new cursor is an iterator even if the original
   * was an array.
   *
   * @return The new cursor
   */
  override def copy(): MongoGenericTypedCursor[T] = _newInstance(underlying.copy()) // parens for side-effects
}

/**
 * A wrapper for the new com.mongodb.Cursor - which is used when returning a
 * cursor from the aggregation framework.
 *
 * @version 2.7
 *
 * @param  underlying (com.mongodb.Cursor)
 * @tparam T DBObject
 */
case class Cursor(underlying: com.mongodb.Cursor) extends Iterator[DBObject] {

  type T = DBObject

  def hasNext: Boolean = underlying.hasNext

  def next(): DBObject = underlying.next()
}

/**
 *
 *
 * @version 1.0, 12/15/10
 * @since 2.0
 *
 * @param  underlying (DBObject)
 * @see http://dochub.mongodb.org/core/explain
 */
sealed class CursorExplanation(override val underlying: DBObject) extends MongoDBObject {

  /**
   * cursor
   *
   * The cursor type for the query
   */
  def cursor: Option[String] = getAs[String]("cursor")

  /**
   * nscanned
   *
   * Number of items examined by Mongo for this query.
   * Items could be objects or index keys---if a "covered" index
   * is involved, nscanned may be higher than nscannedObjects
   *
   * @return a Long value indicating 'nscanned'
   */
  def nscanned: Option[Long] = getAs[Long]("nscanned")

  /**
   * nscannedObjects
   *
   * @return a Long value of # objects examined by Mongo for this query.
   */
  def nscannedObjects: Option[Long] = getAs[Long]("nscannedObjects")

  /**
   * nYields
   *
   * @return A long value of the number of times the query yielded the read lock to let writes in
   */
  def nYields: Option[Long] = getAs[Long]("nYields")

  /**
   * n
   *
   * @return a Long value of the number of objects which Mongo returned
   */
  def n: Option[Long] = getAs[Long]("n")

  /**
   * millis
   *
   * @return The number of milliseconds the query took to execute
   */
  def millis: Option[Long] = getAs[Long]("millis")

  /**
   * indexBounds
   *
   * @return the index boundaries this query used.
   */
  def indexBounds: Option[MongoDBList] = getAs[MongoDBList]("indexBounds")

}
