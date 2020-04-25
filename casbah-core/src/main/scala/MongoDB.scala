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

import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.language.reflectiveCalls

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.map_reduce.{ MapReduceCommand, MapReduceResult }
import com.mongodb.{ CommandResult, DBCollection }

/**
 * Wrapper for the Mongo <code>DB</code> object providing scala-friendly functionality.
 *
 * @since 2.0
 * @see com.mongodb.DB
 */
object MongoDB {
  def apply(connection: com.mongodb.MongoClient, dbName: String): MongoDB =
    connection.asScala.apply(dbName)

  def apply(connection: MongoClient, dbName: String): MongoDB =
    connection(dbName)
}

// scalastyle:off number.of.methods
/**
 * Wrapper for the Mongo <code>DB</code> object providing scala-friendly functionality.
 *
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
  def apply(collection: String): MongoCollection = underlying.getCollection(collection).asScala

  /**
   * Execute a database command directly.
   * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
   * @return the result of the command from the database
   */
  def command(cmd: DBObject): CommandResult = underlying.command(cmd)

  /**
   * Execute a database command directly.
   * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
   * @return the result of the command from the database
   */
  def command(cmd: String): CommandResult = underlying.command(cmd)

  /**
   * Execute a database command directly.
   * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
   * @return the result of the command from the database
   */
  def command(cmd: DBObject, readPreference: ReadPreference): CommandResult = underlying.command(cmd, readPreference)

  /**
   * Creates a collection with a given name and options.
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
  def createCollection(name: String, o: DBObject): DBCollection = underlying.createCollection(name, o)

  def doEval(code: String, args: AnyRef*): CommandResult = underlying.doEval(code, args: _*)

  /**
   * Drops this database.  Removes all data on disk.  Use with caution.
   */
  def dropDatabase(): Unit = underlying.dropDatabase()

  def eval(code: String, args: AnyRef*): AnyRef = underlying.eval(code, args: _*)

  /**
   * Gets a collection with a given name.
   * If the collection does not exist, a new collection is created.
   * @param name (String) the name of the collection to return
   * @return the collection
   */
  def getCollection(name: String): DBCollection = underlying.getCollection(name)

  /**
   * Returns a collection matching a given string.
   * @param s the name of the collection
   * @return the collection
   */
  def getCollectionFromString(s: String): DBCollection = underlying.getCollectionFromString(s)

  /**
   * Returns a set of the names of collections in this database.
   * @return the names of collections in this database
   */
  def getCollectionNames(): mutable.Set[String] = underlying.getCollectionNames.asScala /* calls the db */

  /**
   * Returns a set of the names of collections in this database.
   * @return the names of collections in this database
   */
  def collectionNames(): mutable.Set[String] = getCollectionNames() /* calls the db */

  def name: String = getName

  def getName: String = underlying.getName

  def getSisterDB(name: String): MongoDB = underlying.getSisterDB(name).asScala

  def stats(): CommandResult = getStats()

  def getStats(): CommandResult = underlying.getStats() /* calls the db */

  /**
   * Sets queries to be OK to run on slave nodes.
   */
  @deprecated("Replaced with ReadPreference.SECONDARY.", "2.3.0")
  @SuppressWarnings(Array("deprecation"))
  def slaveOk(): Unit = underlying.slaveOk() // use parens because this side-effects

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
   * Sets the read preference for this database. Will be used as default for
   * reads from any collection in this database. See the
   * documentation for [[com.mongodb.ReadPreference]] for more information.
   *
   * @param pref Read Preference to use
   */
  def readPreference_=(pref: ReadPreference): Unit = setReadPreference(pref)

  /**
   * Sets the read preference for this database. Will be used as default for
   * reads from any collection in this database. See the
   * documentation for [[com.mongodb.ReadPreference]] for more information.
   *
   * @param pref Read Preference to use
   */
  def setReadPreference(pref: ReadPreference): Unit = underlying.setReadPreference(pref)

  /**
   * Gets the read preference for this database. Will be used as default for
   * reads from any collection in this database. See the
   * documentation for [[com.mongodb.ReadPreference]] for more information.
   */
  def readPreference: ReadPreference = getReadPreference

  /**
   * Gets the read preference for this database. Will be used as default for
   * reads from any collection in this database. See the
   * documentation for [[com.mongodb.ReadPreference]] for more information.
   */
  def getReadPreference: ReadPreference = underlying.getReadPreference

  /**
   * Checks to see if a collection by name %lt;name&gt; exists.
   * @param collectionName The collection to test for existence
   * @return false if no collection by that name exists, true if a match to an existing collection was found
   */
  def collectionExists(collectionName: String): Boolean =
    underlying.collectionExists(collectionName)

  /**
   * The Java Driver is a bit outdated and is missing the finalize option.
   * Additionally, it returns ZERO information about the actual results of the mapreduce,
   * just a cursor to the result collection.
   * This is less than ideal.  So I've wrapped it in something more useful.
   *
   * @param cmd An instance of MapReduceCommand representing the required MapReduce
   * @return MapReduceResult a wrapped result object.  This contains the returns success, counts etc, but implements iterator and can be iterated directly
   */
  def mapReduce(cmd: MapReduceCommand): MapReduceResult = MapReduceResult(command(cmd.toDBObject))(this)

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
   * @see com.mognodb.Bytes
   */
  def getOptions: Int = underlying.getOptions

  /**
   * Manipulate Network Options
   *
   * @see com.mongodb.Mongo
   * @see com.mognodb.Bytes
   */
  def options: Int = getOptions

  override def toString(): String = underlying.toString

}
