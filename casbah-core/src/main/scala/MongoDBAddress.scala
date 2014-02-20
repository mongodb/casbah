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

/**
 *
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
   * @throws UnknownHostException if cannot connect to the host
   */
  def apply(other: DBAddress, dbName: String): DBAddress = new DBAddress(other, dbName)

  /**
   * Creates a new DBAddress... acceptable formats:
   *
   * <pre>
   * name ("myDB")
   * <host>/name ("127.0.0.1/myDB")
   * <host>:<port>/name ("127.0.0.1:8080/myDB")
   * </pre>
   *
   * @param  urlFormat String
   * @return com.mongodb.DBAddress
   *
   * @throws UnknownHostException if cannot connect to the host
   *
   */
  def apply(urlFormat: String): DBAddress = new DBAddress(urlFormat)

  /**
   * Connects to a database with a given name at a given host.
   *
   * @param  host   String
   * @param  dbName String
   * @return com.mongodb.DBAddress
   * @throws UnknownHostException if cannot connect to the host
   */
  def apply(host: String, dbName: String): DBAddress = new DBAddress(host, dbName)

  /**
   * Connects to a database with a given host, port &amp; name at a given host.
   *
   * @param  host   String
   * @param  port   Int
   * @param  dbName String
   * @return com.mongodb.DBAddress
   * @throws UnknownHostException if cannot connect to the host
   */
  def apply(host: String, port: Int, dbName: String): DBAddress =
    new DBAddress(host, port, dbName)

  /**
   * Connects to a database with a given InetAddress, port &amp; name at a given host.
   *
   * @param  addr   java.net.InetAddress
   * @param  port   Int
   * @param  dbName String
   * @return com.mongodb.DBAddress
   * @throws UnknownHostException if cannot connect to the host
   * @see java.net.InetAddress
   */
  def apply(addr: java.net.InetAddress, port: Int, dbName: String): DBAddress =
    new DBAddress(addr, port, dbName)
}
