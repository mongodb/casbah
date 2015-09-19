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

import com.mongodb.casbah.Imports._

import com.mongodb.{DBDecoderFactory, DBEncoderFactory, MongoClientOptions => JavaMongoClientOptions}

import javax.net.SocketFactory

/**
 * Helper class for creating MongoClientOptions instances
 *
 * @since 2.5
 * @see com.mongodb.MongoClientOptions
 */
object MongoClientOptions {

  type Builder = JavaMongoClientOptions.Builder

  val Defaults = new JavaMongoClientOptions.Builder().build()

  // scalastyle:off parameter.number
  /**
   * Instantiate a new MongoClientOptions instance
   *
   * @param connectionsPerHost # of connections allowed per host (pool size, per host) default 100
   * @param connectTimeout Connection timeout in milliseconds default 10,000
   * @param cursorFinalizerEnabled Sets whether there is a finalize method created that cleans up instances of DBCursor default true
   * @param dbDecoderFactory override the default decoder factory
   * @param dbEncoderFactory override the default encoder factory
   * @param description the description of the MongoClient
   * @param maxWaitTime the maximum time that a thread will block waiting for a connection, default 1000 * 60 * 2
   * @param readPreference the read preference to use for queries, map-reduce, aggregation, and count
   * @param socketFactory the socket factory for creating sockets to the mongo server
   * @param socketKeepAlive if socket keep alive is enabled, default false
   * @param socketTimeout socket timeout in milliseconds passed to Socket.setSoTimeout, default 0
   * @param threadsAllowedToBlockForConnectionMultiplier the multiplier for number of threads allowed to block waiting for a connection, default 5
   * @param writeConcern the write concern to use
   * @param alwaysUseMBeans true if driver should always use MBeans, regardless of VM version
   * @param heartbeatConnectTimeout the heartbeat connect timeout, in milliseconds
   * @param heartbeatFrequency the heartbeat frequency, in milliseconds
   * @param heartbeatSocketTimeout the heartbeat socket timeout, in milliseconds
   * @param maxConnectionIdleTime the maximum idle time, in milliseconds
   * @param maxConnectionLifeTime  the maximum life time, in milliseconds
   * @param minConnectionsPerHost maximum number of connections
   * @param requiredReplicaSetName the required replica set name for the replica set.
   * @param minHeartbeatFrequency the minimum heartbeat frequency, in milliseconds, which must be &gt; 0
   *
   * @throws MongoException()
   * @see ServerAddress
   * @see MongoDBAddress
   */
  def apply(
             connectionsPerHost: Int = Defaults.getConnectionsPerHost,
             connectTimeout: Int = Defaults.getConnectTimeout,
             cursorFinalizerEnabled: Boolean = Defaults.isCursorFinalizerEnabled,
             dbDecoderFactory: DBDecoderFactory = Defaults.getDbDecoderFactory,
             dbEncoderFactory: DBEncoderFactory = Defaults.getDbEncoderFactory,
             description: String = Defaults.getDescription,
             maxWaitTime: Int = Defaults.getMaxWaitTime,
             readPreference: ReadPreference = Defaults.getReadPreference,
             socketFactory: SocketFactory = Defaults.getSocketFactory,
             socketKeepAlive: Boolean = Defaults.isSocketKeepAlive,
             socketTimeout: Int = Defaults.getSocketTimeout,
             threadsAllowedToBlockForConnectionMultiplier: Int = Defaults.getThreadsAllowedToBlockForConnectionMultiplier,
             writeConcern: WriteConcern = Defaults.getWriteConcern,
             alwaysUseMBeans: Boolean = Defaults.isAlwaysUseMBeans,
             heartbeatConnectTimeout: Int = Defaults.getHeartbeatConnectTimeout,
             heartbeatFrequency: Int = Defaults.getHeartbeatFrequency,
             heartbeatSocketTimeout: Int = Defaults.getHeartbeatSocketTimeout,
             maxConnectionIdleTime: Int = Defaults.getMaxConnectionIdleTime,
             maxConnectionLifeTime: Int = Defaults.getMaxConnectionLifeTime,
             minConnectionsPerHost: Int = Defaults.getMinConnectionsPerHost,
             requiredReplicaSetName: String = Defaults.getRequiredReplicaSetName,
             minHeartbeatFrequency: Int = Defaults.getMinHeartbeatFrequency

             ): JavaMongoClientOptions = {
    val builder = new JavaMongoClientOptions.Builder()
    builder.connectionsPerHost(connectionsPerHost)
    builder.connectTimeout(connectTimeout)
    builder.cursorFinalizerEnabled(cursorFinalizerEnabled)
    builder.dbDecoderFactory(dbDecoderFactory)
    builder.dbEncoderFactory(dbEncoderFactory)
    builder.description(description)
    builder.maxWaitTime(maxWaitTime)
    builder.readPreference(readPreference)
    builder.socketFactory(socketFactory)
    builder.socketKeepAlive(socketKeepAlive)
    builder.socketTimeout(socketTimeout)
    builder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier)
    builder.writeConcern(writeConcern)
    builder.alwaysUseMBeans(alwaysUseMBeans)
    builder.heartbeatConnectTimeout(heartbeatConnectTimeout)
    builder.heartbeatFrequency(heartbeatFrequency)
    builder.heartbeatSocketTimeout(heartbeatSocketTimeout)
    builder.maxConnectionIdleTime(maxConnectionIdleTime)
    builder.maxConnectionLifeTime(maxConnectionLifeTime)
    builder.minConnectionsPerHost(minConnectionsPerHost)
    builder.requiredReplicaSetName(requiredReplicaSetName)

    if (minHeartbeatFrequency != Defaults.getMinHeartbeatFrequency) {
      builder.minHeartbeatFrequency(minHeartbeatFrequency)
    }
    builder.build()
  }


  // scalastyle:on parameter.number
}

