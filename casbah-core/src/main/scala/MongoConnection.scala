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

import scalaj.collection.Imports._

import com.mongodb.{Mongo, ServerAddress}

/**
 * Wrapper object for Mongo Connections, providing the static methods the Java driver gives.
 * Apply methods are called as MongoConnection(<params>)
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
object MongoConnection {

  /**
   * Default connection method - connects to default host &amp; port
   *
   * @throws UnknownHostException
   * @throws MongoException
   */
  def apply() = new MongoConnection(new Mongo())

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
  def apply(replicaSetSeeds: List[ServerAddress]) = new MongoConnection(new Mongo(replicaSetSeeds.asJava))

  /**
   * Replica Set connection
   * This works for a replica set or pair,
   * and finds all the members (the master is used by default)
   * Takes a MongoOptions object
   * 
   * @param replicaSetSeeds (List[ServerAddress]) The servers to connect to
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(replicaSetSeeds: List[ServerAddress], options: MongoOptions) = 
    new MongoConnection(new Mongo(replicaSetSeeds.asJava, options))

  /** 
   * Connect via a MongoURI
   * 
   * @param  uri (MongoURI) 
   */
  def apply(uri: MongoURI) = new MongoConnection(new Mongo(uri.underlying))

  /** 
   * Connect via a com.mongodb.MongoURI
   * 
   * @param  uri (com.mongodb.MongoURI) 
   */
  def apply(uri: com.mongodb.MongoURI) = new MongoConnection(new Mongo(uri))

  /** 
   * Connects to a (single) mongodb node.
   * 
   * @param  addr (ServerAddress) the DatabaseAddress
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(addr: ServerAddress) = new MongoConnection(new Mongo(addr))

  /** 
   * Connects to a (single) mongodb node.
   * 
   * @param  addr (ServerAddress) the DatabaseAddress
   * @param  options (MongoOptions) DB Options
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   * @see MongoOptions
   */
  def apply(addr: ServerAddress, options: MongoOptions) = 
    new MongoConnection(new Mongo(addr, options))

 
  /** 
   * Creates a Mongo connection in paired mode.
   * This will also work for a replica set and will find
   * all the members (the master will be used by default)
   * 
   * @param  left (ServerAddress) the left side of the pair
   * @param  right (ServerAddress) The right side of the pair
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(left: ServerAddress, right: ServerAddress) = 
    new MongoConnection(new Mongo(left, right))

  /** 
   * Creates a Mongo connection in paired mode.
   * This will also work for a replica set and will find
   * all the members (the master will be used by default)
   * 
   * @param  left (ServerAddress) the left side of the pair
   * @param  right (ServerAddress) The right side of the pair
   * @param  options (MongoOptions) the MongoDB Options for the connection
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   * @see MongoOptions
   */
  def apply(left: ServerAddress, right: ServerAddress, 
            options: com.mongodb.MongoOptions) = 
    new MongoConnection(new Mongo(left, right, options))


  /** 
   * Connects to a (single) mongodb node (default port)
   * 
   * @param  host (String)  server to connect to
   * @throws UnknownHostException
   * @throws MongoException
   */
  def apply(host: String) = new MongoConnection(new Mongo(host))
  /** 
   * Connects to a (single) mongodb node
   * 
   * @param  host (String)  server to connect to
   * @param  port (Int) the port on which the database is running
   * @throws UnknownHostException
   * @throws MongoException
   */
  def apply(host: String, port: Int) = new MongoConnection(new Mongo(host, port))


  def connect(addr: DBAddress) = new MongoDB(Mongo.connect(addr))

}

/**
 * Wrapper class for the Mongo Connection object.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
class MongoConnection(val underlying: Mongo) {
  /**
   * Apply method which proxies getDB, allowing you to call
   * <code>connInstance("dbName")</code>
   *
   * @param dbName (String) A string for the database name
   * @return MongoDB A wrapped instance of a Mongo 'DB Class.
   */
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
  def getAddress()= underlying.getAddress()


  def allAddress = getAllAddress()
  def getAllAddress() = underlying.getAllAddress()

  /** 
   * Closes all open connections.
   * NOTE: This connection can't be reused after closing.
   */
  def close() = underlying.close // use parens because this side-effects

  /** 
   * Sets queries to be OK to run on slave nodes.
   */
  def slaveOk() = underlying.slaveOk()   // use parens because this side-effects

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

}

/**
 * 
 * @author  Brendan W. McAdams <brendan@10gen.com>
 * @since   1.0.1
 */
object MongoDBAddress { 
  
  /**
   * Connects to a given database using the host/port info from an existing
   * DBAddress instance.
   * 
   * @param  other  DBAddress the existing DBAddress
   * @param  dbName String the database to which to connect
   * @return com.mongodb.DBAddress       
   * @throws java.net.UnknownHostException
   */
  def apply(other: DBAddress, dbName: String) = new DBAddress(other, dbName)
  
  /**
   * Creates a new DBAddress... acceptable formats:
   *
   * <pre>
   *   name ("myDB")
   *   <host>/name ("127.0.0.1/myDB")
   *   <host>:<port>/name ("127.0.0.1:8080/myDB")
   * </pre>
   *
   * @param  urlFormat String
   * @return com.mongodb.DBAddress       
   *
   * @throws java.net.UnknownHostException
   *
   */
  def apply(urlFormat: String) = new DBAddress(urlFormat)

  /**
   * Connects to a database with a given name at a given host.
   *
   * @param  host   String
   * @param  dbName String
   * @return com.mongodb.DBAddress       
   * @throws java.net.UnknownHostException
   */
  def apply(host: String, dbName: String) = new DBAddress(host, dbName)

  /**
   * Connects to a database with a given host, port &amp; name at a given host.
   *
   * @param  host   String
   * @param  port   Int
   * @param  dbName String
   * @return com.mongodb.DBAddress       
   * @throws java.net.UnknownHostException
   */
  def apply(host: String, port: Int, dbName: String) = 
    new DBAddress(host, port, dbName)

  /**
   * Connects to a database with a given InetAddress, port &amp; name at a given host.
   *
   * @param  addr   java.net.InetAddress
   * @param  port   Int
   * @param  dbName String
   * @return com.mongodb.DBAddress       
   * @throws java.net.UnknownHostException
   * @see java.net.InetAddress
   */
  def apply(addr: java.net.InetAddress, port: Int, dbName: String) = 
    new DBAddress(addr, port, dbName)
}

