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
import com.mongodb.casbah.util.Logging

import scalaj.collection.Imports._


/**
 * Wrapper for the Mongo <code>DB</code> object providing scala-friendly functionality.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
class MongoDB(val underlying: com.mongodb.DB) {
  /**
   * Apply method to proxy  getCollection, to allow invocation of
   * <code>dbInstance("collectionName")</code>
   * instead of getCollection
   *
   * @param collection a  string for the collection name
   * @return MongoCollection A wrapped instance of a Mongo DBCollection Class returning generic DBObjects
   */
  def apply(collection: String) = underlying.getCollection(collection).asScala
  /**
   * Parameterized apply method to proxy  getCollection, to allow invocation of
   * <code>dbInstance("collectionName")</code>
   * instead of getCollection
   *
   * This returns a Type-specific Collection wrapper, and requires the ability to either implicitly determine a manifest,
   * or that you explicitly pass it where necessary to use it.  It should find things on it's own in most cases
   * but the compiler will tell you if not.
   *
   * @param collection a  string for the collection name
   * @param clazz Class[A] where A is the Subclass of DBOBject you wish to work with for this collection
   * @return MongoCollection A wrapped instance of a Mongo DBCollection Class returning DBObject subclasses of type A
   */
  def apply[A <: DBObject](collection: String, clazz: Class[A])(implicit m: scala.reflect.Manifest[A]) = underlying.getCollection(collection).asScalaTyped(m)
  def addUser(username: String, passwd: String) = underlying.addUser(username, passwd.toArray)
  def authenticate(username: String, passwd: String) = underlying.authenticate(username, passwd.toArray)
  def command(cmd: DBObject) = underlying.command(cmd)
  def createCollection(name: String, o: DBObject) = underlying.createCollection(name, o)
  def doEval(code: String, args: AnyRef*) = underlying.doEval(code, args: _*)
  def dropDatabase() = underlying.dropDatabase()
  def eval(code: String, args: AnyRef*) = underlying.eval(code, args: _*)
  def forceError() = underlying.forceError
  def getCollection(name: String) = underlying.getCollection(name)
  def getCollectionFromString(s: String) = underlying.getCollectionFromString(s)
  def getCollectionNames() = underlying.getCollectionNames().asScala
  def getLastError() = underlying.getLastError
  def name = getName
  def getName() = underlying.getName
  def getPreviousError() = underlying.getPreviousError
  def getSisterDB(name: String) = underlying.getSisterDB(name)
  def getWriteConcern() = underlying.getWriteConcern
  def requestDone() = underlying.requestDone
  def requestEnsureConnection() = underlying.requestEnsureConnection
  def requestStart() = underlying.requestStart
  def resetError() = underlying.resetError
  def resetIndexCache() = underlying.resetIndexCache
  def setReadOnly(b: Boolean) = underlying.setReadOnly(b)
  def setWriteConcern(concern: com.mongodb.WriteConcern) = underlying.setWriteConcern(concern)

  /**
   * The Java Driver is a bit outdated and is missing the finalize option.
   * Additionally, it returns ZERO information about the actual results of the mapreduce,
   * just a cursor to the result collection.
   * This is less than ideal.  So I've wrapped it in something more useful.
   *
   * @param command An instance of MapReduceCommand representing the required MapReduce
   * @return MapReduceResult a wrapped result object.  This contains the returns success, counts etc, but implements iterator and can be iterated directly
   */
  def mapReduce(cmd: MapReduceCommand): MapReduceResult  = {
    val result = command(cmd.asDBObject)
    new MapReduceResult(result)(this)
  }

  /**
   * write concern aware write op block.
   *
   * Guarantees that the operations in the passed
   * block are executed in the same connection
   * via requestStart() and requestDone().
   * 
   * Calls &amp; throws getLastError afterwards,
   * so if you run multiple ops you'll only get the final 
   * error.
   * 
   * Your op function gets a copy of this MongoDB.
   * 
   * This is for update ops only - you cannot return data from it.
   * 
   * 
   * @throws MongoException
   */
  def request(op: MongoDB => Unit) { 
    // Lock the connection handle (e.g. no pooled calls)
    requestStart
    
    // Exec the op
    op(this)

    // If anything failed, throw the exception 
    getLastError.throwOnError

    // Unlock the connection
    requestDone
  }

  override def toString() = underlying.toString
}
