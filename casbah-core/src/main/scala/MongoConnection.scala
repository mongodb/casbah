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

/**
 * Wrapper object for Mongo Connections, providing the static methods the Java driver gives.
 * Apply methods are called as MongoConnection(<params>)
 */
@deprecated("Please use MongoClient", "2.7")
object MongoConnection {

  /**
   * Default connection method - connects to default host &amp; port
   *
   * @throws UnknownHostException if cannot connect to the host
   * @throws MongoException on error
   */
  def apply(): MongoConnection = new MongoConnection(new com.mongodb.Mongo())

  /**
   * Replica Set connection
   * This works for a replica set or pair,
   * and finds all the members (the master is used by default)
   *
   * @param replicaSetSeeds (List[ServerAddress]) The servers to connect to
   * @throws MongoException on error
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(replicaSetSeeds: List[ServerAddress]): MongoConnection =
    new MongoConnection(new com.mongodb.Mongo(replicaSetSeeds.asJava))

  /**
   * Replica Set connection
   * This works for a replica set or pair,
   * and finds all the members (the master is used by default)
   * Takes a MongoOptions object
   *
   * @param replicaSetSeeds (List[ServerAddress]) The servers to connect to
   * @throws MongoException on error
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(replicaSetSeeds: List[ServerAddress], options: MongoOptions): MongoConnection =
    new MongoConnection(new com.mongodb.Mongo(replicaSetSeeds.asJava, options))

  /**
   * Connect via a MongoURI
   *
   * @param  uri (MongoURI)
   */
  def apply(uri: MongoURI): MongoConnection = new MongoConnection(new com.mongodb.Mongo(uri.underlying))

  /**
   * Connect via a com.mongodb.MongoURI
   *
   * @param  uri (com.mongodb.MongoURI)
   */
  def apply(uri: com.mongodb.MongoURI): MongoConnection = new MongoConnection(new com.mongodb.Mongo(uri))

  /**
   * Connects to a (single) mongodb node.
   *
   * @param  addr (ServerAddress) the DatabaseAddress
   * @throws MongoException on error
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(addr: ServerAddress): MongoConnection = new MongoConnection(new com.mongodb.Mongo(addr))

  /**
   * Connects to a (single) mongodb node.
   *
   * @param  addr (ServerAddress) the DatabaseAddress
   * @param  options (MongoOptions) DB Options
   * @throws MongoException on error
   * @see ServerAddress
   * @see MongoDBAddress
   * @see MongoOptions
   */
  def apply(addr: ServerAddress, options: MongoOptions): MongoConnection =
    new MongoConnection(new com.mongodb.Mongo(addr, options))

  /**
   * Creates a Mongo connection in paired mode.
   * This will also work for a replica set and will find
   * all the members (the master will be used by default)
   *
   * @param  left (ServerAddress) the left side of the pair
   * @param  right (ServerAddress) The right side of the pair
   * @throws MongoException on error
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(left: ServerAddress, right: ServerAddress): MongoConnection =
    new MongoConnection(new com.mongodb.Mongo(left, right))

  /**
   * Creates a Mongo connection in paired mode.
   * This will also work for a replica set and will find
   * all the members (the master will be used by default)
   *
   * @param  left (ServerAddress) the left side of the pair
   * @param  right (ServerAddress) The right side of the pair
   * @param  options (MongoOptions) the MongoDB Options for the connection
   * @throws MongoException on error
   * @see ServerAddress
   * @see MongoDBAddress
   * @see MongoOptions
   */
  def apply(left: ServerAddress, right: ServerAddress,
            options: com.mongodb.MongoOptions): MongoConnection =
    new MongoConnection(new com.mongodb.Mongo(left, right, options))

  /**
   * Connects to a (single) mongodb node (default port)
   *
   * @param  host (String)  server to connect to
   * @throws UnknownHostException if cannot connect to the host
   * @throws MongoException on error
   */
  def apply(host: String): MongoConnection = new MongoConnection(new com.mongodb.Mongo(host))

  /**
   * Connects to a (single) mongodb node
   *
   * @param  host (String)  server to connect to
   * @param  port (Int) the port on which the database is running
   * @throws MongoException on error
   */
  def apply(host: String, port: Int): MongoConnection = new MongoConnection(new com.mongodb.Mongo(host, port))

}

/**
 * Wrapper class for the Mongo Connection object.
 */
@deprecated("Please use MongoClient", "2.7")
class MongoConnection(val underlying: com.mongodb.Mongo) {
  /**
   * Apply method which proxies getDB, allowing you to call
   * <code>connInstance("dbName")</code>
   *
   * @param dbName (String) A string for the database name
   * @return MongoDB A wrapped instance of a Mongo 'DB Class.
   */
  def apply(dbName: String): MongoDB = underlying.getDB(dbName).asScala

  def getDB(dbName: String): MongoDB = apply(dbName)

  /**
   * @throws MongoException on error
   */
  def dbNames(): mutable.Buffer[String] = getDatabaseNames() /* calls the db */

  /**
   * @throws MongoException on error
   */
  def databaseNames(): mutable.Buffer[String] = getDatabaseNames() /* calls the db */

  /**
   * @throws MongoException on error
   */
  def getDatabaseNames(): mutable.Buffer[String] = underlying.getDatabaseNames.asScala /* calls the db */

  /**
   * Drops the database if it exists.
   *
   * @param dbName (String) the name of the database to drop
   * @throws MongoException on error
   */
  def dropDatabase(dbName: String): Unit = underlying.dropDatabase(dbName)

  def connectPoint: String = getConnectPoint

  def getConnectPoint: String = underlying.getConnectPoint

  /**
   * Gets the address of this database.
   *
   * @return (ServerAddress) The address of the DB
   */
  def address: ServerAddress = getAddress

  /**
   * Gets the address of this database.
   *
   * @return (ServerAddress) The address of the DB
   */
  def getAddress: ServerAddress = underlying.getAddress

  def allAddress: mutable.Buffer[ServerAddress] = getAllAddress

  def getAllAddress: mutable.Buffer[ServerAddress] = underlying.getAllAddress.asScala

  /**
   * Closes all open connections.
   * NOTE: This connection can't be reused after closing.
   */
  def close(): Unit = underlying.close() // use parens because this side-effects

  /**
   * Sets queries to be OK to run on slave nodes.
   */
  @deprecated("Replaced with `ReadPreference.SECONDARY`", "2.3.0")
  def slaveOk(): Unit = underlying.slaveOk() // use parens because this side-effects

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
}
