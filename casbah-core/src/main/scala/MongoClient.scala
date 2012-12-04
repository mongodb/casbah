/**
 * Copyright (c) 2010 - 2012 10gen, Inc. <http://10gen.com>
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

import scalaj.collection.Imports._

import com.mongodb.ServerAddress
import com.mongodb.{ MongoClient => JavaMongoClient }

/**
 * Wrapper object for MongoClient connections, providing the static methods the
 * Java driver gives. Apply methods are called as MongoClient(<params>)
 *
 * @since 2.5
 */
object MongoClient {

  /**
   * Default connection method - connects to default host &amp; port
   *
   * @throws UnknownHostException
   * @throws MongoException
   */
  def apply() = new MongoClient(new JavaMongoClient())

  /**
   * Connects to a (single) mongodb node (default port)
   *
   * @param  host (String)  server to connect to
   * @throws UnknownHostException
   * @throws MongoException
   */
  def apply(host: String) = new MongoClient(new JavaMongoClient(host))

  /**
   * Creates a Mongo instance based on a (single) mongodb node (default port).
   *
   * @param host server to connect to in format host[:port]
   * @param options default query options
   * @throws UnknownHostException
   * @throws MongoException
   */
  def apply(host: String, options: MongoClientOptions) = new MongoClient(new JavaMongoClient(host, options))

  /**
   * Connects to a (single) mongodb node
   *
   * @param  host (String)  server to connect to
   * @param  port (Int) the port on which the database is running
   * @throws UnknownHostException
   * @throws MongoException
   */
  def apply(host: String, port: Int) = new MongoClient(new JavaMongoClient(host, port))

  /**
   * Connects to a (single) mongodb node.
   *
   * @param  addr (ServerAddress) the DatabaseAddress
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(addr: ServerAddress) = new MongoClient(new JavaMongoClient(addr))

  /**
   * Connects to a (single) mongodb node.
   *
   * @param  addr (ServerAddress) the DatabaseAddress
   * @param  options (MongoClientOptions) DB Options
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   * @see MongoClientOptions
   */
  def apply(addr: ServerAddress, options: MongoClientOptions) =
    new MongoClient(new JavaMongoClient(addr, options))

  /**
   * Replica Set connection
   * This works for a replica set or pair,
   * and finds all the members (the master is used by default)
   *
   * @param replicaSetSeeds (List[ServerAddress]) The servers to connect to
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(replicaSetSeeds: List[ServerAddress]) = new MongoClient(new JavaMongoClient(replicaSetSeeds.asJava))

  /**
   * Replica Set connection
   * This works for a replica set or pair,
   * and finds all the members (the master is used by default)
   * Takes a MongoClientOptions object
   *
   * @param replicaSetSeeds (List[ServerAddress]) The servers to connect to
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(replicaSetSeeds: List[ServerAddress], options: MongoClientOptions) =
    new MongoClient(new JavaMongoClient(replicaSetSeeds.asJava, options))

  /**
   * Connect via a MongoClientURI
   *
   * @param  uri (MongoClientURI)
   */
  def apply(uri: MongoClientURI) = new MongoClient(new JavaMongoClient(uri.underlying))

  /**
   * Connect via a com.mongodb.MongoClientURI
   *
   * @param  uri (com.mongodb.MongoClientURI)
   */
  def apply(uri: com.mongodb.MongoClientURI) = new MongoClient(new JavaMongoClient(uri))

}

/**
 * Wrapper class for the MongoClient object.
 *
 */
class MongoClient(val underlying: JavaMongoClient) {

  def apply(dbName: String) = underlying.getDB(dbName).asScala
  def getDB(dbName: String) = apply(dbName)

  /**
   * @throws MongoException
   */
  def dbNames = getDatabaseNames()

  /**
   * @throws MongoException
   */
  def databaseNames = getDatabaseNames()

  /**
   * @throws MongoException
   */
  def getDatabaseNames() = underlying.getDatabaseNames.asScala

  /**
   * Drops the database if it exists.
   *
   * @param dbName (String) the name of the database to drop
   * @throws MongoException
   */
  def dropDatabase(dbName: String) = underlying.dropDatabase(dbName)

  def version = getVersion()
  def getVersion() = underlying.getVersion

  def debugString = underlying.debugString

  def connectPoint = getConnectPoint()
  def getConnectPoint() = underlying.getConnectPoint

  /**
   * Gets the address of this database.
   *
   * @return (ServerAddress) The address of the DB
   */
  def address = getAddress()

  /**
   * Gets the address of this database.
   *
   * @return (ServerAddress) The address of the DB
   */
  def getAddress() = underlying.getAddress()

  def allAddress = getAllAddress()

  def getAllAddress() = underlying.getAllAddress()

  /**
   * Closes all open connections.
   * NOTE: This connection can't be reused after closing.
   */
  def close() = underlying.close() // use parens because this side-effects

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
   *
   * Set the write concern for this database.
   * Will be used for writes to any collection in this database.
   * See the documentation for {@link WriteConcern} for more info.
   *
   * @param concern (WriteConcern) The write concern to use
   * @see WriteConcern
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
  def getWriteConcern = underlying.getWriteConcern()

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
   * Sets the read preference for this database. Will be used as default for
   * reads from any collection in this database. See the
   * documentation for {@link ReadPreference} for more information.
   *
   * @param preference Read Preference to use
   */
  def readPreference_=(pref: ReadPreference) = setReadPreference(pref)

  /**
   * Sets the read preference for this database. Will be used as default for
   * reads from any collection in this database. See the
   * documentation for {@link ReadPreference} for more information.
   *
   * @param preference Read Preference to use
   */
  def setReadPreference(pref: ReadPreference) = underlying.setReadPreference(pref)

  /**
   * Gets the read preference for this database. Will be used as default for
   * reads from any collection in this database. See the
   * documentation for {@link ReadPreference} for more information.
   *
   * @param preference Read Preference to use
   */
  def readPreference = getReadPreference

  /**
   * Gets the read preference for this database. Will be used as default for
   * reads from any collection in this database. See the
   * documentation for {@link ReadPreference} for more information.
   *
   * @param preference Read Preference to use
   */
  def getReadPreference = underlying.getReadPreference
}