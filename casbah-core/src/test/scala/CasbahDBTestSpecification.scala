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
 */

package com.mongodb.casbah.test.core

import scala.jdk.CollectionConverters._
import scala.util.Properties

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification

import org.specs2.execute.{ AsResult, Result }
import org.specs2.specification.Around

trait CasbahDBTestSpecification extends CasbahMutableSpecification with Around {
  sequential

  val DEFAULT_URI: String = "mongodb://localhost:27017"
  val MONGODB_URI_SYSTEM_PROPERTY_NAME: String = "org.mongodb.test.uri"
  val TEST_DB = "mongo-casbah-driver-test"

  def around[R: AsResult](r: => R): Result = {
    initialSetUp()
    try AsResult(r)
    finally finalTeardown()
  }

  def initialSetUp(): Unit = {}

  def finalTeardown(): Unit = {
    if (mongoDbOnline) {
      database.dropDatabase()
      mongoClient.close()
    }
  }

  val databaseName: String = TEST_DB
  val className = getClass.getName

  val mongoClientURI = {
    val mongoURIString = Properties.propOrElse(MONGODB_URI_SYSTEM_PROPERTY_NAME, DEFAULT_URI)
    MongoClientURI(mongoURIString)
  }

  val mongoClient = MongoClient(mongoClientURI)
  lazy val database = mongoClient.getDB(databaseName)
  lazy val collection = database(className)

  val mongoDbOnline = {
    try {
      mongoClient.databaseNames()
      true
    } catch {
      case t: Throwable => false
    }
  }

  lazy val mongoDbIsOnline = mongoDbOnline

  lazy val versionArray = mongoClient.getDB("admin")
    .command("buildInfo")
    .getAs[MongoDBList]("versionArray")
    .get

  /**
   *
   * @param minVersion version Array
   * @return true if server is at least specified version
   */
  def serverIsAtLeastVersion(minVersion: Int*): Boolean = {
    var retVal = 0
    for ((v, i) <- minVersion.zipWithIndex) {
      if (retVal == 0) retVal = versionArray(i).asInstanceOf[Int].compareTo(v)
    }
    retVal >= 0
  }

  def enableMaxTimeFailPoint() {
    if (serverIsAtLeastVersion(2, 5)) {
      mongoClient.getDB("admin").command(
        MongoDBObject("configureFailPoint" -> "maxTimeAlwaysTimeOut", "mode" -> "alwaysOn"),
        ReadPreference.Primary
      )
    }
  }

  def disableMaxTimeFailPoint() {
    if (serverIsAtLeastVersion(2, 5)) {
      mongoClient.getDB("admin").command(
        MongoDBObject("configureFailPoint" -> "maxTimeAlwaysTimeOut", "mode" -> "off"),
        ReadPreference.Primary
      )
    }
  }

  lazy val isStandalone = !runReplicaSetStatusCommand
  lazy val isReplicaSet = runReplicaSetStatusCommand
  lazy val isSharded: Boolean = {
    val isMasterResult = mongoClient.getDB("admin").command(MongoDBObject("ismaster" -> 1))
    Option(isMasterResult.get("msg")) match {
      case Some("isdbgrid") => true
      case _                => false
    }
  }

  def runReplicaSetStatusCommand: Boolean = {
    val result = mongoClient.getDB("admin").command(MongoDBObject("replSetGetStatus" -> 1))
    result.getErrorMessage match {
      case errorMsg if errorMsg != null && errorMsg.indexOf("--replSet") != -1 => false
      case _ => true
    }
  }

  lazy val getCommandLine = mongoClient.getDB("admin").command("getCmdLineOpts").asScala

  lazy val hasTestCommand: Boolean = getCommandLine("argv").asInstanceOf[BasicDBList] contains "enableTestCommands=1"

  skipAllUnless(mongoDbOnline)
}
