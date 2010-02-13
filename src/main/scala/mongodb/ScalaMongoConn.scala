/**
 * Copyright (c) 2010, Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: Portions of this work are derived from the Apache License 2.0 "mongo-scala-driver" work
 * by Alexander Azarov <azarov@osinka.ru>, available from http://github.com/alaz/mongo-scala-driver
 */

package com.novus.mongodb

import com.mongodb._
import org.scala_tools.javautils.Imports._
import Implicits._

/**
 * Wrapper object for Mongo Connections, providing the static methods the Java driver gives.
 * Apply methods are called as ScalaMongoConn(<params>)
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
object ScalaMongoConn {
  def apply() = new ScalaMongoConn(new Mongo())
  def apply(addr: DBAddress) = new ScalaMongoConn(new Mongo(addr))
  def apply(left: DBAddress, right: DBAddress) = new ScalaMongoConn(new Mongo(left, right))
  def apply(left: DBAddress, right: DBAddress, options: MongoOptions) = new ScalaMongoConn(new Mongo(left, right, options))
  def apply(addr: DBAddress, options: MongoOptions) = new ScalaMongoConn(new Mongo(addr, options))
  def apply(host: String) = new ScalaMongoConn(new Mongo(host))
  def apply(host: String, port: Int) = new ScalaMongoConn(new Mongo(host, port))
  //def apply(host: String, options: MongoOptions) = new ScalaMongoConn(new Mongo(host, options))
}

/**
 * Wrapper class for the Mongo Connection object.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
class ScalaMongoConn(val underlying: Mongo) {
  /**
   * Apply method which proxies getDB, allowing you to call
   * <code>connInstance("dbName")</code>
   *
   * @param dbName A string for the database name
   * @return ScalaMongoDB A wrapped instance of a MongoDB Class.
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
