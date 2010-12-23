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

import com.mongodb.DBCursor 

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging

import scalaj.collection.Imports._

/** 
 * Scala wrapper for Mongo DBCursors,
 * including ones which return custom DBObject subclasses.
 * 
 * This is a rewrite of the Casbah 1.0 approach which was rather
 * naive and unnecessarily complex.... formerly was MongoCursorWrapper
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 2.0, 12/23/10
 * @since 2.0
 * 
 * @tparam T (DBObject or subclass thereof)
 */
trait MongoCursorBase[T <: DBObject] extends Iterator[T] with Logging {

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
  def next() = underlying.next.asInstanceOf[T]

  /** 
   * hasNext
   * 
   * Is there another element in the cursor?
   *
   * {@inheritDoc}
   *
   * @return (Boolean Next)
   */
  def hasNext = underlying.hasNext


  /** 
   * copy
   *
   * Creates a new copy of an existing database cursor.
   * The new cursor is an iterator even if the original 
   * was an array.
   * 
   * @return The new cursor
   */
  def copy() = _newInstance(underlying.copy()) // parens for side-effects

  /** 
   * sort
   * 
   * Sort this cursor's elements
   *
   * @param  orderBy (A) The fields on which to sort
   * @tparam A  A view of DBObject to sort by
   * @return A cursor pointing to the first element of the sorted results
   */
  def sort[A <% DBObject](orderBy: A) = {
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
   * @throws MongoException
   */
  def count = underlying.count

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
   * @throws MongoException
   */
  override def size = underlying.size

  /** 
   * Manipulate Query Options
   *
   * Adds an option - see Bytes.QUERYOPTION_* for list
   * TODO - Create Scala version of Bytes.QUERYOPTION_* 
   *
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def option_=(option: Int) = underlying.addOption(option)

  /** 
   * Manipulate Query Options
   *
   * Resets options to default.
   * 
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def resetOptions() = underlying.resetOptions() // use parens because this side-effects

  /** 
   * Manipulate Query Options
   *
   * Gets current option settings - see Bytes.QUERYOPTION_* for list
   * 
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def options = underlying.getOptions

  /** 
   * Manipulate Query Options
   *
   * Sets current option settings - see Bytes.QUERYOPTION_* for list
   * 
   * @see com.mongodb.Mongo
   * @see com.mongodb.Bytes
   */
  def options_=(opts: Int) = underlying.setOptions(opts)


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
  def hint[A <% DBObject](indexKeys: A) = {
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
  def hint(indexName: String) = {
    underlying.hint(indexName)
    this
  }

  /** 
   * snapshot
   * 
   * Use snapshot mode for the query.
   * Snapshot mode assures no duplicates are returned, or objects missed,
   * which were present at both the start and end of the query's
   * execution (if an object is new during the query, or deleted during the query,
   * it may or may not be returned, even with snapshot mode).
   *
   * <b>NOTE:</b> Short query responses (&lt; 1MB) are always effectively snapshotted.
   * <b>NOTE:</b> Currently, snapshot mode may not be used with sorting or explicit hints.
   *  
   * @return the same DBCursor, useful for chaining operations
   */
  def snapshot() = {
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
   *     - cursor = Cursor Type
   *     - nscanned = Number of items examined by Mongo for this query
   *     - nscannedObjects = Number of objects examined by Mongo
   *     - n = the number of records which Mongo returned
   *     - millis = how long it took Mongo to execute the query
   *     - nYields = number of times this query yielded the read lock to let writes in
   *     - indexBounds = the index boundaries used.
   *
   * CursorExplanation provides utility methods to access these fields.
   *
   * @see http://dochub.mongodb.org/core/explain
   * @return an instance of CursorExplanation
   */
  def explain = new CursorExplanation(underlying.explain)


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
  def limit(n: Int) = { 
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
  def skip(n: Int) = { 
    underlying.skip(n)
    this
  }


  /** 
   * cursorId
   * 
   * @return A long representing the cursorID on the server; 0 = no cursor
   *
   */
  def cursorId = underlying.getCursorId()


  /** 
   * close
   * 
   * Kill the current cursor on the server
   */
  def close() = underlying.close() // parens for side-effect


  /** 
   * slaveOk
   * 
   * Makes this query OK to run on a non-master node.
   *
   */
  def slaveOk() = underlying.slaveOk() // parens for side-effect

  def numGetMores = underlying.numGetMores

  /** 
   * numSeen
   * 
   * Returns the number of objects through which this cursor has iterated,
   * as tracked by the java driver.
   *
   * @return The number of objects seen
   */
  def numSeen = underlying.numSeen

  def sizes = underlying.getSizes.asScala

  /** 
   * batchSize
   * 
   * Limits the number of elements returned in one batch.
   *
   * @param  n (Int) The number of elements to return in a batch
   * @return the same DBCursor, useful for chaining operations
   */
  def batchSize(n: Int) = {
    underlying.batchSize(n)
    this
  }

  def keysWanted = underlying.getKeysWanted

  def query = underlying.getQuery

  /**
   * "Special" Operators for cursors
   *
   * adds a special operator like $maxScan or $returnKey 
   * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-Specialoperators
   * {@inheritDoc}
   * @return the same DBCursor, useful for chaining operations
   * @example addSpecial( "$returnKey" , 1 ) 
   * @example addSpecial( "$maxScan" , 100 )
   */
  def addSpecial(name: String, o: Any) = {
    // The Java code returns a copy of itself (via _this_) so no clone/_newInstance
    underlying.addSpecial(name, o.asInstanceOf[AnyRef])
    this
  }
  

  /** 
   * $returnKey
   * 
   * Sets a special operator of $returnKey
   * If true, returns ONLY the index key.
   * Defaults to true if you just call $returnKey
   *
   * @param  bool (Boolean = true) 
   * @return the same DBCursor, useful for chaining operations
   */
  def $returnKey(bool: Boolean = true) = addSpecial("$returnKey", bool)

  /** 
   * $maxScan
   *
   * Sets a special operator of $maxScan
   * Which defines the max number of items to scan.
   * 
   * @param  max (A) 
   * @tparam A : Numeric 
   * @return the same DBCursor, useful for chaining operations
   */
  def $maxScan[A : Numeric](max: T) = addSpecial("$maxScan", max)


  /** 
   * $query
   *
   * Sets a special operator of $query
   * Which defines the query for this cursor.
   * 
   * This is the same as running find() on a Collection with the query.
   *
   * @param  q (DBObject) 
   * @return the same DBCursor, useful for chaining operations
   */
  def $query[A <% DBObject](q: A) = addSpecial("$query", q)

  /** 
   * $orderby
   *
   * Sets a special operator of $orderby
   * which defines the sort spec for this cursor.
   *
   * This is the same as calling sort on the cursor.
   *
   * @param  obj (DBObject) 
   * @return the same DBCursor, useful for chaining operations
   */
  def $orderby[A <% DBObject](obj: A) = addSpecial("$orderby", obj)

  /** 
   * $explain
   * 
   * Sets a special operator of $explain
   * which, if true, explains the query instead of returning results.
   *
   * This is the same as calling the explain() method on the cursor.
   *
   * @param  bool (Boolean = true) 
   * @return the same DBCursor, useful for chaining operations
   */
  def $explain(bool: Boolean = true) = addSpecial("$explain", bool)

  /**
   * $snapshot
   * 
   * Sets a special operator of $snapshot
   * which, if True, sets snapshot mode on the query.
   *
   * This is the same as calling the snapshot() method on the cursor.
   * 
   * @param  bool (Boolean = true) 
   * @return the same DBCursor, useful for chaining operations
   */
  def $snapshot(bool: Boolean = true) = addSpecial("$snapshot", bool)

  /** 
   * $min
   * 
   * Sets minimum index bounds - commonly paired with $max
   *
   * @param  obj (DBObject) 
   * @see http://www.mongodb.org/display/DOCS/min+and+max+Query+Specifiers
   *
   * @return the same DBCursor, useful for chaining operations
   */
  def $min[A <% DBObject](obj: A) = addSpecial("$min", obj)

  /** 
   * $max
   * 
   * Sets maximum index bounds - commonly paired with $max
   *
   * @param  obj (DBObject) 
   * @see http://www.mongodb.org/display/DOCS/max+and+max+Query+Specifiers
   *
   * @return the same DBCursor, useful for chaining operations
   */
  def $max[A <% DBObject](obj: A) = addSpecial("$max", obj)

  /** 
   * $showDiskLoc
   * 
   * Sets a special operator $showDiskLoc which, if true,
   * shows the disk location of results.
   *
   * @param  bool (Boolean = true) 
   * @return the same DBCursor, useful for chaining operations
   */
  def $showDiskLoc(bool: Boolean = true) = addSpecial("$showDiskLoc", bool)

  /**
   * $hint
   * 
   * Sets a special operator $hint which
   * forces the query to use a given index. 
   *
   * This is the same as calling hint() on the cursor.
   *
   * @param obj (DBObject)
   * @return the same DBCursor, useful for chaining operations
   */
  def $hint[A <% DBObject](obj: A) = addSpecial("$hint", obj)


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
  def _newInstance(cursor: DBCursor): MongoCursorBase[T] 
}

/** 
 * Concrete cursor implementation expecting standard DBObject operation
 * This is the version of MongoCursorBase you should expect to use in most cases.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 2.0, 12/23/10
 * @since 1.0
 * 
 * @param  val underlying (com.mongodb.DBCollection) 
 * @tparam DBObject 
 */
class MongoCursor(val underlying: DBCursor) extends MongoCursorBase[DBObject]  {

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
  def _newInstance(cursor: DBCursor) = new MongoCursor(cursor)
  
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
  def apply[T <: DBObject : Manifest](collection: MongoCollectionBase[T], query: DBObject, 
                                      keys: DBObject) = {
    val cursor = new DBCursor(collection.underlying, query, keys)

    if (manifest[T] == manifest[DBObject]) 
      new MongoCursor(cursor)
    else
      new MongoTypedCursor[T](cursor)

  }
}
/** 
 * Concrete cursor implementation for typed Cursor operations via Collection.setObjectClass
 * This is a special case cursor for typed operations.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 2.0, 12/23/10
 * @since 1.0
 * 
 * @param  val underlying (com.mongodb.DBCollection) 
 * @tparam T A Subclass of DBObject 
 */
class MongoTypedCursor[T <: DBObject : Manifest](val underlying: DBCursor) extends MongoCursorBase[T] {

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
  def _newInstance(cursor: DBCursor) = new MongoTypedCursor[T](cursor)

}

/** 
 * 
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0, 12/15/10
 * @since 2.0
 * 
 * @param  val underlying (DBObject) 
 * @see http://dochub.mongodb.org/core/explain
 */
sealed class CursorExplanation(val underlying: DBObject) extends MongoDBObject {
  
  /** 
   * cursor
   * 
   * The cursor type for the query
   * TODO - look at making this an enum?
   */
  def cursor = getAs[String]("cursor")

  /** 
   * nscanned
   * 
   *  Number of items examined by Mongo for this query.
   *  Items could be objects or index keys---if a "covered" index
   *  is involved, nscanned may be higher than nscannedObjects
   * 
   * @return a Long value indicating 'nscanned'
   */
  def nscanned = getAs[Long]("nscanned")

  /** 
   * nscannedObjects
   * 
   * @return a Long value of # objects examined by Mongo for this query.
   */
  def nscannedObjects = getAs[Long]("nscannedObjects")

  /** 
   * nYields
   * 
   * @return A long value of the number of times the query yielded the read lock to let writes in
   */
  def nYields = getAs[Long]("nYields")

  /**
   * n
   *
   * @return a Long value of the number of objects which Mongo returned
   */
  def n = getAs[Long]("n")

  /** 
   * millis
   * 
   * @return The number of milliseconds the query took to execute
   */
  def millis = getAs[Long]("millis")

  /**
   * indexBounds
   * 
   * @return the index boundaries this query used.
   */
  def indexBounds = getAs[MongoDBList]("indexBounds")

}
