/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
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

import scala.collection.JavaConverters._

import com.mongodb.{DBDecoderFactory, DBEncoderFactory}

import javax.net.SocketFactory

/**
 * Helper class for creating MongoOptions instances
 *
 * @since 2.0
 * @see com.mongodb.MongoOptions
 *
 */
@deprecated("Please use MongoClient and MongoClientOptions", "2.7")
object MongoOptions {

  val Defaults = new MongoOptions()

  // scalastyle:off parameter.number

  /**
   * Instantiate a new MongoOptions instance
   *
   * @param autoConnectRetry Whether system autoretries on connection errors (default: False)
   * @param connectionsPerHost # of connections allowed per host (pool size, per host)
   * @param threadsAllowedToBlockForConnectionMultiplier Multiplier for connectiosnPerHost at # of threads that can block, default 5
   * @param maxWaitTime wait time for a blocking thread for a connection from the pool, default 1000 * 60 * 2
   * @param connectTimeout timeout in milliseconds, for establishing the socket connections, default 0 (infinite)
   * @param socketTimeout timeout, passed to Socket.setSoTimeout, default 0
   * @param socketKeepAlive if socket keep alive is enabled, default false
   * @param maxAutoConnectRetryTime Sets the maximum auto connect retry time default 0
   * @param slaveOk Whether a driver connected to a replica set will send reads to slaves/secondaries
   * @param safe if true use a WriteConcern of WriteConcern.SAFE for all operations
   * @param w The "w" value, (number of writes), of the global WriteConcern
   * @param wTimeout The "wtimeout" value of the global WriteConcern
   * @param fsync The "fsync" value of the global WriteConcern, true indicates writes should wait for data to be
   *              written to server data file
   * @param j The "j" value of the global WriteConcern, true indicates writes should wait for a journaling group commit
   * @param dbDecoderFactory the default decoder factory
   * @param dbEncoderFactory the default encoder factory
   * @param socketFactory sets the socket factory for creating sockets to mongod
   * @param description The description for <code>Mongo</code> instances created with these options
   * @return
   */
  def apply(autoConnectRetry: Boolean = Defaults.autoConnectRetry,
            connectionsPerHost: Int = Defaults.connectionsPerHost,
            threadsAllowedToBlockForConnectionMultiplier: Int = Defaults.threadsAllowedToBlockForConnectionMultiplier,
            maxWaitTime: Int = Defaults.maxWaitTime,
            connectTimeout: Int = Defaults.connectTimeout,
            socketTimeout: Int = Defaults.socketTimeout,
            socketKeepAlive: Boolean = Defaults.socketKeepAlive,
            maxAutoConnectRetryTime: Long = Defaults.maxAutoConnectRetryTime,
            slaveOk: Boolean = Defaults.slaveOk,
            safe: Boolean = Defaults.safe,
            w: Int = Defaults.w,
            wTimeout: Int = Defaults.wtimeout,
            fsync: Boolean = Defaults.fsync,
            j: Boolean = Defaults.j,
            dbDecoderFactory: DBDecoderFactory = Defaults.dbDecoderFactory,
            dbEncoderFactory: DBEncoderFactory = Defaults.dbEncoderFactory,
            socketFactory: SocketFactory = Defaults.socketFactory,
            description: String = Defaults.description): MongoOptions = {
    val options = new MongoOptions

    options.autoConnectRetry = autoConnectRetry
    options.connectionsPerHost = connectionsPerHost
    options.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier
    options.maxWaitTime = maxWaitTime
    options.connectTimeout = connectTimeout
    options.socketTimeout = socketTimeout
    options.socketKeepAlive = socketKeepAlive
    options.maxAutoConnectRetryTime = maxAutoConnectRetryTime
    options.slaveOk = slaveOk
    options.safe = safe
    options.w = w
    options.wtimeout = wTimeout
    options.fsync = fsync
    options.j = j
    options.dbDecoderFactory = dbDecoderFactory
    options.dbEncoderFactory = dbEncoderFactory
    options.socketFactory = socketFactory
    options.description = description
    options
  }

}

