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

import scala.jdk.CollectionConverters._

import com.mongodb.{ MongoCredential => JavaMongoCredential }
import com.mongodb.casbah.Imports._

/**
 * MongoClientURI - representing the options to connect to MongoDB
 *
 * @since 2.0
 */
object MongoClientURI {

  /**
   * Create a new MongoURI with a URI String e.g.:
   *
   * <li> mongodb://localhost
   * <li> mongodb://fred:foobar@localhost/
   * <li> mongodb://server1,server2,server3
   *
   * See [[http://docs.mongodb.org/manual/reference/connection-string/]]
   *
   * @param  uri (String)
   */
  def apply(uri: String): MongoClientURI = new MongoClientURI(new com.mongodb.MongoClientURI(uri))
}

/**
 * Create a new MongoURI with a URI String e.g.:
 *
 * <li> mongodb://localhost
 * <li> mongodb://fred:foobar@localhost/
 * <li> mongodb://server1,server2,server3
 *
 * See [[http://docs.mongodb.org/manual/reference/connection-string/]]
 *
 * @since 2.5
 */
case class MongoClientURI(underlying: com.mongodb.MongoClientURI) {
  def username: Option[String] = Option(underlying.getUsername)

  def password: Option[Array[Char]] = Option(underlying.getPassword)

  def credentials: Option[JavaMongoCredential] = Option(underlying.getCredentials)

  def hosts: Seq[String] = underlying.getHosts.asScala.toSeq

  def database: Option[String] = Option(underlying.getDatabase)

  def collection: Option[String] = Option(underlying.getCollection)

  def options: MongoClientOptions = underlying.getOptions

  def getURI: String = underlying.getURI

  override def toString: String = underlying.toString
}

