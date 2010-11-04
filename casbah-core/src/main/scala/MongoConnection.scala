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

import com.mongodb.Mongo

/**
 * Wrapper object for Mongo Connections, providing the static methods the Java driver gives.
 * Apply methods are called as MongoConnectionection(<params>)
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
object MongoConnection {
  def apply() = new MongoConnection(new Mongo())
  def apply(addr: DBAddress) = new MongoConnection(new Mongo(addr))
  def connect(addr: DBAddress) = new MongoDB(Mongo.connect(addr))
  def apply(left: DBAddress, right: DBAddress) = new MongoConnection(new Mongo(left, right))
  def apply(left: DBAddress, right: DBAddress, options: com.mongodb.MongoOptions) = new MongoConnection(new Mongo(left, right, options))
  def apply(addr: DBAddress, options: com.mongodb.MongoOptions) = new MongoConnection(new Mongo(addr, options))
  def apply(host: String) = new MongoConnection(new Mongo(host))
  def apply(host: String, port: Int) = new MongoConnection(new Mongo(host, port))
  //def apply(host: String, options: MongoOptions) = new MongoConnection(new Mongo(host, options))
}

/**
 * Wrapper class for the Mongo Connection object.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
class MongoConnection(val underlying: Mongo) {
  // Register the core Serialization helpers.
  conversions.scala.RegisterConversionHelpers()
  /**
   * Apply method which proxies getDB, allowing you to call
   * <code>connInstance("dbName")</code>
   *
   * @param dbName A string for the database name
   * @return MongoDB A wrapped instance of a Mongo 'DB Class.
   */
  def apply(dbName: String) = underlying.getDB(dbName).asScala
  def getDB(dbName: String) = apply(dbName)
  def getDatabaseNames() = underlying.getDatabaseNames.asScala
  def dropDatabase(dbName: String) = underlying.dropDatabase(dbName)
  def getVersion() = underlying.getVersion
  def debugString() = underlying.debugString
  def getConnectPoint = underlying.getConnectPoint
  def getAddress = underlying.getAddress
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
}

