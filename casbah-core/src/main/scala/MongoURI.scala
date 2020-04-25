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

import com.mongodb.casbah.Imports._

import scala.jdk.CollectionConverters._
import scala.language.reflectiveCalls

/**
 *
 *
 * @since 2.0
 *
 */
@deprecated("Please use MongoClient and MongoClientURI", "2.7")
object MongoURI {

  /**
   * Create a new MongoURI with a URI String e.g.:
   *
   * <li> mongodb://localhost
   * <li> mongodb://fred:foobar@localhost/
   * <li> mongodb://server1,server2,server3
   *
   * See [[http://www.mongodb.org/display/DOCS/Connections]]
   *
   * @param  uri (String)
   */
  def apply(uri: String): MongoURI = new MongoURI(new com.mongodb.MongoURI(uri))
}

/**
 * Create a new MongoURI with a URI String e.g.:
 *
 * <li> mongodb://localhost
 * <li> mongodb://fred:foobar@localhost/
 * <li> mongodb://server1,server2,server3
 *
 * See [[http://www.mongodb.org/display/DOCS/Connections]]
 *
 * @since 2.0
 */
@deprecated("Please use MongoClient and MongoClientURI", "2.8")
@SuppressWarnings(Array("deprecation"))
class MongoURI(val underlying: com.mongodb.MongoURI) {
  def username: Option[String] = Option(underlying.getUsername)

  def password: Option[Array[Char]] = Option(underlying.getPassword)

  def hosts: Seq[String] = underlying.getHosts.asScala.toSeq

  def database: Option[String] = Option(underlying.getDatabase)

  def collection: Option[String] = Option(underlying.getCollection)

  def options: MongoOptions = underlying.getOptions

  def connect: Either[Throwable, MongoConnection] = try {
    Right(underlying.connect.asScala)
  } catch {
    case t: Throwable => Left(t)
  }

  def connectDB: Either[Throwable, MongoDB] = {
    try {
      require(database.isDefined, "Cannot connect to Database as none is defined.")
      Right(underlying.connectDB.asScala)
    } catch {
      case t: Throwable => Left(t)
    }
  }

  def connectCollection: Either[Throwable, MongoCollection] = {
    try {
      require(collection.isDefined, "Cannot connect to Collection as none is defined.")
      connectDB match {
        case Right(db) =>
          Right(db(collection.get))
        case Left(t) => Left(t)
      }
    } catch {
      case t: Throwable => Left(t)
    }
  }

  override def toString: String = underlying.toString
}
