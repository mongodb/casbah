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

/** 
 * Helper class for creating MongoOptions instances
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see com.mongodb.MongoOptions
 */
object MongoOptions {

  /**
   * Instantiate a new MongoOptions instance
   *
   * @param autoConnectRetry Whether system autoretries on connection errors (default: False)
   * @param connectionsPerHost # of connections allowed per host (pool size, per host)
   * @param threadsAllowedToBlockForConnectionMultiplier Multiplier for connectiosnPerHost at # of threads that can block, default 5
   * @param Max wait time for a blocking thread for a connection from the pool, default 1000 * 60 * 2
   * @param Connection timeout in milliseconds, for establishing the socket connections, default 0 (infinite)
   * @param Socket timeout, passed to Socket.setSoTimeout, default 0
   * @throws MongoException
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(autoConnectRetry: Boolean = new MongoOptions().autoConnectRetry,
            connectionsPerHost: Int = new MongoOptions().connectionsPerHost,
            threadsAllowedToBlockForConnectionMultiplier: Int = new MongoOptions().threadsAllowedToBlockForConnectionMultiplier,
            maxWaitTime: Int = new MongoOptions().maxWaitTime,
            connectTimeout: Int = new MongoOptions().connectTimeout,
            socketTimeout: Int = new MongoOptions().socketTimeout) = { 
    val options = new MongoOptions;

    options.autoConnectRetry = autoConnectRetry
    options.connectionsPerHost = connectionsPerHost
    options.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier
    options.maxWaitTime = maxWaitTime
    options.connectTimeout = connectTimeout
    options.socketTimeout = socketTimeout
    options
  }

}

// vim: set ts=2 sw=2 sts=2 et:
