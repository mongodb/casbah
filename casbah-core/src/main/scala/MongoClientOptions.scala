/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
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

import com.mongodb.{DBDecoderFactory, DBEncoderFactory}

import javax.net.SocketFactory

/**
 * Helper class for creating MongoClientOptions instances
 *
 * @since 2.5
 * @see com.mongodb.MongoClientOptions
 */
object MongoClientOptions {

  val Defaults = new MongoClientOptionsBuilder().build()
  /**
   * Instantiate a new MongoClientOptions instance
   *
   *
   * @param autoConnectRetry Whether system autoretries on connection errors, default false
   * @param connectionsPerHost # of connections allowed per host (pool size, per host) default 100
   * @param connectTimeout Connection timeout in milliseconds default 10,000
   * @param cursorFinalizerEnabled Sets whether there is a finalize method created that cleans up instances of DBCursor default true
   * @param dbDecoderFactory override the default decoder factory
   * @param dbEncoderFactory override the default encoder factory
   * @param description the description of the MongoClient
   * @param maxAutoConnectRetryTime Sets the maximum auto connect retry time default 0
   * @param maxWaitTime the maximum time that a thread will block waiting for a connection, default 1000 * 60 * 2
   * @param readPreference the read preference to use for queries, map-reduce, aggregation, and count
   * @param socketFactory the socket factory for creating sockets to the mongo server
   * @param socketKeepAlive if socket keep alive is enabled, default false
   * @param socketTimeout socket timeout in milliseconds passed to Socket.setSoTimeout, default 0
   * @param threadsAllowedToBlockForConnectionMultiplier the multiplier for number of threads allowed to block waiting for a connection, default 5
   * @param writeConcern the write concern to use
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(
    autoConnectRetry: Boolean = Defaults.isAutoConnectRetry,
    connectionsPerHost: Int = Defaults.getConnectionsPerHost,
    connectTimeout: Int = Defaults.getConnectTimeout,
    cursorFinalizerEnabled: Boolean = Defaults.isCursorFinalizerEnabled,
    dbDecoderFactory: DBDecoderFactory = Defaults.getDbDecoderFactory,
    dbEncoderFactory: DBEncoderFactory = Defaults.getDbEncoderFactory,
    description: String = Defaults.getDescription,
    maxAutoConnectRetryTime: Long = Defaults.getMaxAutoConnectRetryTime,
    maxWaitTime: Int = Defaults.getMaxWaitTime,
    readPreference: ReadPreference = Defaults.getReadPreference,
    socketFactory: SocketFactory = Defaults.getSocketFactory,
    socketKeepAlive: Boolean = Defaults.isSocketKeepAlive,
    socketTimeout: Int = Defaults.getSocketTimeout,
    threadsAllowedToBlockForConnectionMultiplier: Int = Defaults.getThreadsAllowedToBlockForConnectionMultiplier,
    writeConcern: WriteConcern = Defaults.getWriteConcern
    ) = {
    val builder = new MongoClientOptionsBuilder();
    builder.autoConnectRetry(autoConnectRetry);
    builder.connectionsPerHost(connectionsPerHost);
    builder.connectTimeout(connectTimeout);
    builder.cursorFinalizerEnabled(cursorFinalizerEnabled);
    builder.dbDecoderFactory(dbDecoderFactory);
    builder.dbEncoderFactory(dbEncoderFactory);
    builder.description(description);
    builder.maxAutoConnectRetryTime(maxAutoConnectRetryTime);
    builder.maxWaitTime(maxWaitTime);
    builder.readPreference(readPreference);
    builder.socketFactory(socketFactory);
    builder.socketKeepAlive(socketKeepAlive);
    builder.socketTimeout(socketTimeout);
    builder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
    builder.writeConcern(writeConcern);
    builder.build()
  }
}

// vim: set ts=2 sw=2 sts=2 et:
