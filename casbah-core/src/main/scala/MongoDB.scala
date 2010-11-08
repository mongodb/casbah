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
import com.mongodb.casbah.commons.Logging

import scalaj.collection.Imports._


/**
 * Wrapper for the Mongo <code>DB</code> object providing scala-friendly functionality.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see com.mongodb.DB
 */
object MongoDB {
  def apply(connection: com.mongodb.Mongo, dbName: String) = 
    connection.asScala.apply(dbName)

  def apply(connection: MongoConnection, dbName: String) =
    connection(dbName)
}

/**
 * Wrapper for the Mongo <code>DB</code> object providing scala-friendly functionality.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 * @see com.mongodb.DB
 */
class MongoDB(val underlying: com.mongodb.DB) {
  /**
   * Apply method to proxy  getCollection, to allow invocation of
   * <code>dbInstance("collectionName")</code>
   * instead of getCollection
   *
   * @param collection a  string for the collection name
   * @return MongoCollection A wrapped instance of a Mongo DBCollection Class returning generic DBObjects
   */
  def apply(collection: String) = underlying.getCollection(collection).asScala
  /**
   * Parameterized apply method to proxy  getCollection, to allow invocation of
   * <code>dbInstance("collectionName")</code>
   * instead of getCollection
   *
   * This returns a Type-specific Collection wrapper, and requires the ability to either implicitly determine a manifest,
   * or that you explicitly pass it where necessary to use it.  It should find things on it's own in most cases
   * but the compiler will tell you if not.
   *
   * @param collection a  string for the collection name
   * @param clazz Class[A] where A is the Subclass of DBOBject you wish to work with for this collection
   * @return MongoCollection A wrapped instance of a Mongo DBCollection Class returning DBObject subclasses of type A
   */
  def apply[A <: DBObject](collection: String, clazz: Class[A])(implicit m: scala.reflect.Manifest[A]) = underlying.getCollection(collection).asScalaTyped(m)

  def addUser(username: String, passwd: String) = underlying.addUser(username, passwd.toArray)

  /**
   *  Authenticates connection/db with given name and password
   *
   * @param username  name of user for this database
   * @param passwd password of user for this database
   * @return true if authenticated, false otherwise
   * @dochub authenticate
   */
  def authenticate(username: String, passwd: String) = underlying.authenticate(username, passwd.toArray)

  /** Execute a database command directly.
   * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
   * @return the result of the command from the database
   * @dochub commands
   */
  def command(cmd: DBObject) = underlying.command(cmd)
  /** Execute a database command directly.
   * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
   * @return the result of the command from the database
   * @dochub commands
   */
  def command(cmd: String) = underlying.command(cmd)
  /** Execute a database command directly.
   * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
   * @return the result of the command from the database
   * @dochub commands
   */
  def command(cmd: DBObject, options: Int) = underlying.command(cmd, options)
  /** Creates a collection with a given name and options.
   * If the collection does not exist, a new collection is created.
   * Possible options:
   * <dl>
   * <dt>capped</dt><dd><i>boolean</i>: if the collection is capped</dd>
   * <dt>size</dt><dd><i>int</i>: collection size</dd>
   * <dt>max</dt><dd><i>int</i>: max number of documents</dd>
   * </dl>
   * @param name the name of the collection to return
   * @param o options
   * @return the collection
   */
  def createCollection(name: String, o: DBObject) = underlying.createCollection(name, o)
  def doEval(code: String, args: AnyRef*) = underlying.doEval(code, args: _*)
  /**
   *  Drops this database.  Removes all data on disk.  Use with caution.
   */
  def dropDatabase() = underlying.dropDatabase()
  def eval(code: String, args: AnyRef*) = underlying.eval(code, args: _*)
  /**
   *  For testing purposes only - this method forces an error to help test error handling
   */
  def forceError() = underlying.forceError

  /** Gets a collection with a given name.
   * If the collection does not exist, a new collection is created.
   * @param name (String) the name of the collection to return
   * @return the collection
   */
  def getCollection(name: String) = underlying.getCollection(name)
  /** Returns a collection matching a given string.
   * @param s the name of the collection
   * @return the collection
   */
  def getCollectionFromString(s: String) = underlying.getCollectionFromString(s)
  /** Returns a set of the names of collections in this database.
   * @return the names of collections in this database
   */
  def getCollectionNames() = underlying.getCollectionNames().asScala
  /** Returns a set of the names of collections in this database.
   * @return the names of collections in this database
   */
  def collectionNames = getCollectionNames()
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
  def getLastError() = underlying.getLastError
  def getLastError(writeConcern: WriteConcern) = 
    underlying.getLastError(writeConcern)
  def getLastError(w: Int, wTimeout: Int, fsync: Boolean) =
    underlying.getLastError(w, wTimeout, fsync)
  def name = getName
  def getName() = underlying.getName

  /**
   *  Returns the last error that occurred since start of database or a call to <code>resetError()</code>
   *
   *  The return object will look like
   *
   *  <pre>
   * { err : errorMessage, nPrev : countOpsBack, ok : 1 }
   *  </pre>
   *
   * The value for errormMessage will be null of no error has ocurred, or the message.  The value of
   * countOpsBack will be the number of operations since the error occurred.
   *
   * Care must be taken to ensure that calls to getPreviousError go to the same connection as that
   * of the previous operation. See com.mongodb.Mongo.requestStart for more information.
   *
   * @deprecated The getPreviousError() and resetError() commands are deprecated and may be removed in future versions of MongoDB
   * @return DBObject with error and status information
   */
  def getPreviousError() = underlying.getPreviousError
  /**
   *  Resets the error memory for this database.  Used to clear all errors such that getPreviousError()
   *  will return no error.
   *
   * @deprecated The getPreviousError() and resetError() commands are deprecated and may be removed in future versions of MongoDB
   */
  def resetError() = underlying.resetError

  /** Clears any indices that have not yet been applied to 
   * the collections in this database.
   */
  def resetIndexCache() = underlying.resetIndexCache

  def getSisterDB(name: String) = underlying.getSisterDB(name).asScala

  /**
   * Returns true if this DB is authenticated
   *
   * @return true if authenticated, false otherwise
   * @dochub authenticate
   */
  def isAuthenticated = underlying.isAuthenticated()

  def stats = getStats()
  def getStats() = underlying.getStats()

  def requestDone() = underlying.requestDone
  def requestEnsureConnection() = underlying.requestEnsureConnection
  def requestStart() = underlying.requestStart

  /** Makes this database read-only
   *
   * @param b if the database should be read-only
   */
  def setReadOnly(b: Boolean) = underlying.setReadOnly(b)

  /** Makes this database read-only
   *
   * @param b if the database should be read-only
   */
  def readOnly_=(b: Boolean) = setReadOnly(b)

  /** 
   * Sets queries to be OK to run on slave nodes.
   */
  def slaveOk() = underlying.slaveOk()   // use parens because this side-effects

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
   * Checks to see if a collection by name %lt;name&gt; exists.
   * @param collectionName The collection to test for existence
   * @return false if no collection by that name exists, true if a match to an existing collection was found
   */
  def collectionExists(collectionName: String) = 
    underlying.collectionExists(collectionName)

  /**
   * The Java Driver is a bit outdated and is missing the finalize option.
   * Additionally, it returns ZERO information about the actual results of the mapreduce,
   * just a cursor to the result collection.
   * This is less than ideal.  So I've wrapped it in something more useful.
   *
   * @param command An instance of MapReduceCommand representing the required MapReduce
   * @return MapReduceResult a wrapped result object.  This contains the returns success, counts etc, but implements iterator and can be iterated directly
   */
  def mapReduce(cmd: MapReduceCommand): MapReduceResult  = {
    val result = command(cmd.asDBObject)
    new MapReduceResult(result)(this)
  }

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
  def request(op: MongoDB => Unit) { 
    // Lock the connection handle (e.g. no pooled calls)
    requestStart
    
    // Exec the op
    op(this)

    // If anything failed, throw the exception 
    getLastError.throwOnError

    // Unlock the connection
    requestDone
  }

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


  override def toString() = underlying.toString

}
