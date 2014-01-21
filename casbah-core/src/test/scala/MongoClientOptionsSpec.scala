/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package com.mongodb.casbah.test.core

import com.mongodb.{DBDecoderFactory, DBEncoderFactory, MongoClientOptions => JavaMongoClientOptions}

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification

import javax.net.ssl.SSLSocketFactory


class MongoClientOptionsSpec extends CasbahMutableSpecification {

  "MongoClientOptions builder" should {

    "have the same defaults as the Java MongoClientOptions" in {

      val options = new MongoClientOptions.Builder().build
      val javaOptions = new JavaMongoClientOptions.Builder().build

      options.getDescription must beEqualTo(javaOptions.getDescription)
      options.getReadPreference must beEqualTo(javaOptions.getReadPreference)
      options.getWriteConcern must beEqualTo(javaOptions.getWriteConcern)
      options.isAutoConnectRetry must beEqualTo(javaOptions.isAutoConnectRetry)
      options.getConnectionsPerHost must beEqualTo(javaOptions.getConnectionsPerHost)
      options.getConnectTimeout must beEqualTo(javaOptions.getConnectTimeout)
      options.getMaxAutoConnectRetryTime must beEqualTo(javaOptions.getMaxAutoConnectRetryTime)
      options.getThreadsAllowedToBlockForConnectionMultiplier must beEqualTo(javaOptions.getThreadsAllowedToBlockForConnectionMultiplier)
      options.isSocketKeepAlive must beEqualTo(javaOptions.isSocketKeepAlive)
      options.isCursorFinalizerEnabled must beEqualTo(javaOptions.isCursorFinalizerEnabled)
      options.getSocketFactory must beEqualTo(javaOptions.getSocketFactory)
      options.getDbEncoderFactory must beEqualTo(javaOptions.getDbEncoderFactory)
      options.getDbDecoderFactory must beEqualTo(javaOptions.getDbDecoderFactory)

    }

    "act the same as the Java MongoClientOptions builder" in {

      val builder = new MongoClientOptions.Builder()
      builder.description("test")
      builder.readPreference(ReadPreference.Secondary)
      builder.writeConcern(WriteConcern.JournalSafe)
      builder.autoConnectRetry(true)
      builder.connectionsPerHost(500)
      builder.connectTimeout(100)
      builder.maxAutoConnectRetryTime(300)
      builder.threadsAllowedToBlockForConnectionMultiplier(1)
      builder.socketKeepAlive(true)
      builder.cursorFinalizerEnabled(true)

      val socketFactory = SSLSocketFactory.getDefault
      builder.socketFactory(socketFactory)

      val encoderFactory = new DBEncoderFactory() {
        def create = null
      }
      builder.dbEncoderFactory(encoderFactory)

      val decoderFactory = new DBDecoderFactory() {
        def create = null
      }
      builder.dbDecoderFactory(decoderFactory)

      val options = builder.build
      options.getDescription must beEqualTo("test")
      options.getReadPreference must beEqualTo(ReadPreference.Secondary)
      options.getWriteConcern must beEqualTo(WriteConcern.JournalSafe)
      options.isAutoConnectRetry must beEqualTo(true)
      options.getConnectionsPerHost must beEqualTo(500)
      options.getConnectTimeout must beEqualTo(100)
      options.getMaxAutoConnectRetryTime must beEqualTo(300)
      options.getThreadsAllowedToBlockForConnectionMultiplier must beEqualTo(1)
      options.isSocketKeepAlive must beEqualTo(true)
      options.isCursorFinalizerEnabled must beEqualTo(true)
      options.getSocketFactory must beEqualTo(socketFactory)
      options.getDbEncoderFactory must beEqualTo(encoderFactory)
      options.getDbDecoderFactory must beEqualTo(decoderFactory)

    }

    "throw validation errors if invalid settings are added" in {
      val builder = new MongoClientOptions.Builder()

      lazy val testDbDecoderFactory = builder.dbDecoderFactory(null)
      testDbDecoderFactory must throwA[IllegalArgumentException]

      lazy val testDbEncoderFactory = builder.dbEncoderFactory(null)
      testDbEncoderFactory must throwA[IllegalArgumentException]

      lazy val testSocketFactory = builder.socketFactory(null)
      testSocketFactory must throwA[IllegalArgumentException]

      lazy val testWriteConcern = builder.writeConcern(null)
      testWriteConcern must throwA[IllegalArgumentException]

      lazy val testreadPreference = builder.readPreference(null)
      testreadPreference must throwA[IllegalArgumentException]

      lazy val testConnectionsPerHost = builder.connectionsPerHost(0)
      testConnectionsPerHost must throwA[IllegalArgumentException]

      lazy val testConnectTimeout = builder.connectTimeout(-1)
      testConnectTimeout must throwA[IllegalArgumentException]

      lazy val testMaxAutoConnectRetryTime = builder.maxAutoConnectRetryTime(-1)
      testMaxAutoConnectRetryTime must throwA[IllegalArgumentException]

      lazy val testThreadsAllowedToBlockForConnectionMultiplier = builder.threadsAllowedToBlockForConnectionMultiplier(0)
      testThreadsAllowedToBlockForConnectionMultiplier must throwA[IllegalArgumentException]

    }

    "WriteConcern valueOf should return an Option" in {
      WriteConcern.valueOf("None") must beEqualTo(Option(WriteConcern.None))
      WriteConcern.valueOf("MadeUp") must beEqualTo(None)
    }

  }
}

