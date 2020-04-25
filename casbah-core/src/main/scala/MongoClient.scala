/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
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

import java.net.UnknownHostException

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.language.reflectiveCalls

import com.mongodb.casbah.Imports._
import com.mongodb.{ MongoClient => JavaMongoClient }
import com.mongodb.casbah.{MongoClientURI => SMongoClientURI}

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
   * @throws UnknownHostException if host cannot be contacted
   * @throws MongoException if problem connecting
   */
  def apply(): MongoClient = new MongoClient(new JavaMongoClient())

  /**
   * Connects to a (single) mongodb node (default port)
   *
   * @param  host (String)  server to connect to
   * @throws UnknownHostException if host cannot be contacted
   * @throws MongoException if problem connecting
   */
  def apply(host: String): MongoClient = new MongoClient(new JavaMongoClient(host))

  /**
   * Creates a Mongo instance based on a (single) mongodb node (default port).
   *
   * @param host server to connect to in format host[:port]
   * @param options default query options
   * @throws UnknownHostException if host cannot be contacted
   * @throws MongoException if problem connecting
   */
  def apply(host: String, options: MongoClientOptions): MongoClient = new MongoClient(new JavaMongoClient(host, options))

  /**
   * Connects to a (single) mongodb node
   *
   * @param  host (String)  server to connect to
   * @param  port (Int) the port on which the database is running
   * @throws UnknownHostException if host cannot be contacted
   * @throws MongoException if problem connecting
   */
  def apply(host: String, port: Int): MongoClient = new MongoClient(new JavaMongoClient(host, port))

  /**
   * Connects to a (single) mongodb node.
   *
   * @param  addr (ServerAddress) the DatabaseAddress
   * @throws MongoException if problem connecting
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(addr: ServerAddress): MongoClient = new MongoClient(new JavaMongoClient(addr))

  /**
   * Connects to a (single) mongodb node.
   *
   * @param  addr (ServerAddress) the DatabaseAddress
   * @param  options (MongoClientOptions) DB Options
   * @throws MongoException if problem connecting
   * @see ServerAddress
   * @see MongoDBAddress
   * @see MongoClientOptions
   */
  def apply(addr: ServerAddress, options: MongoClientOptions): MongoClient =
    new MongoClient(new JavaMongoClient(addr, options))

  /**
   * Creates a Mongo instance based on a (single) mongodb node and a list of credentials
   *
   * @param addr (ServerAddress) the DatabaseAddress
   * @param credentialsList (List[MongoCredential]) used to authenticate all connections
   * @throws MongoException if problem connecting
   * @see ServerAddress
   * @see MongoDBAddress
   * @see credentialsList
   * @since 2.6
   */
  def apply(addr: ServerAddress, credentialsList: List[MongoCredential]): MongoClient =
    new MongoClient(new JavaMongoClient(addr, credentialsList.asJava))

  /**
   * Creates a Mongo instance based on a (single) mongo node using a given ServerAddress and options.
   *
   * @param addr (ServerAddress) the DatabaseAddress
   * @param credentials (List[MongoCredential]) used to authenticate all connections
   * @param options (MongoClientOptions) DB Options
   * @throws MongoException if problem connecting
   * @see ServerAddress
   * @see MongoDBAddress
   * @see MongoClientOptions
   * @see CredentialsList
   * @since 2.6
   */
  def apply(addr: ServerAddress, credentials: List[MongoCredential], options: MongoClientOptions): MongoClient =
    new MongoClient(new JavaMongoClient(addr, credentials.asJava, options))

  /**
   * Replica Set connection
   * This works for a replica set or pair,
   * and finds all the members (the master is used by default)
   *
   * @param replicaSetSeeds (List[ServerAddress]) The servers to connect to
   * @throws MongoException if problem connecting
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(replicaSetSeeds: List[ServerAddress]): MongoClient =
    new MongoClient(new JavaMongoClient(replicaSetSeeds.asJava))

  /**
   * Replica Set connection
   * This works for a replica set or pair,
   * and finds all the members (the master is used by default)
   * Takes a MongoClientOptions object
   *
   * @param replicaSetSeeds (List[ServerAddress]) The servers to connect to
   * @param options (MongoClientOptions) DB Options
   * @throws MongoException if problem connecting
   * @see ServerAddress
   * @see MongoDBAddress
   * @see MongoClientOptions
   */
  def apply(replicaSetSeeds: List[ServerAddress], options: MongoClientOptions): MongoClient =
    new MongoClient(new JavaMongoClient(replicaSetSeeds.asJava, options))

  /**
   * Replica Set connection
   * This works for a replica set or pair,
   * and finds all the members (the master is used by default)
   * Takes a MongoClientOptions object
   *
   * @param replicaSetSeeds (List[ServerAddress]) The servers to connect to
   * @param credentials (List[MongoCredential]) used to authenticate all connections
   * @throws MongoException if problem connecting
   * @see ServerAddress
   * @see MongoDBAddress
   * @see CredentialsList
   * @since 2.6
   */
  def apply(replicaSetSeeds: List[ServerAddress], credentials: List[MongoCredential]): MongoClient =
    new MongoClient(new JavaMongoClient(replicaSetSeeds.asJava, credentials.asJava))

  /**
   * Replica Set connection
   * This works for a replica set or pair,
   * and finds all the members (the master is used by default)
   * Takes a MongoClientOptions object
   *
   * @param replicaSetSeeds (List[ServerAddress]) The servers to connect to
   * @param credentials (List[MongoCredential]) used to authenticate all connections
   * @param options (MongoClientOptions) DB Options
   * @throws MongoException if problem connecting
   * @see ServerAddress
   * @see MongoDBAddress
   * @see MongoClientOptions
   * @see CredentialsList
   * @since 2.6
   */
  def apply(replicaSetSeeds: List[ServerAddress], credentials: List[MongoCredential], options: MongoClientOptions): MongoClient =
    new MongoClient(new JavaMongoClient(replicaSetSeeds.asJava, credentials.asJava, options))

  /**
   * Connect via a MongoClientURI
   *
   * @param  uri (MongoClientURI)
   */
  def apply(uri: SMongoClientURI): MongoClient = new MongoClient(new JavaMongoClient(uri.underlying))

  /**
   * Connect via a com.mongodb.MongoClientURI
   *
   * @param  uri (com.mongodb.MongoClientURI)
   */
  def apply(uri: com.mongodb.MongoClientURI): MongoClient = new MongoClient(new JavaMongoClient(uri))

}

/**
 * Wrapper class for the MongoClient object.
 *
 */
class MongoClient(val underlying: JavaMongoClient) {

  def apply(dbName: String): MongoDB = underlying.getDB(dbName).asScala

  def getDB(dbName: String): MongoDB = apply(dbName)

  /**
   * @throws MongoException if problem connecting
   */
  def dbNames(): mutable.Buffer[String] = getDatabaseNames() /* calls the db */

  /**
   * @throws MongoException if problem connecting
   */
  def databaseNames(): mutable.Buffer[String] = getDatabaseNames() /* calls the db */

  /**
   * @throws MongoException if problem connecting
   */
  def getDatabaseNames(): mutable.Buffer[String] = underlying.getDatabaseNames().asScala /* calls the db */

  /**
   * Drops the database if it exists.
   *
   * @param dbName (String) the name of the database to drop
   * @throws MongoException if problem connecting
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

  /**
   * Gets the list of credentials that this client authenticates all connections with
   *
   * @return the list of credentials
   * @since 2.6.0
   */
  def credentialsList: mutable.Buffer[MongoCredential] = getCredentialsList

  /**
   * Gets the list of credentials that this client authenticates all connections with
   *
   * @return the list of credentials
   * @since 2.6.0
   */
  def getCredentialsList: mutable.Buffer[MongoCredential] = underlying.getCredentialsList.asScala
}
