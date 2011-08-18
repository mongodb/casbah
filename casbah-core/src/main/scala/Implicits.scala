/**
 * Copyright (c) 2010, 2011 10gen, Inc. <http://10gen.com>
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

import scalaj.collection.Imports._
import org.scala_tools.time.Imports._
import com.mongodb.WriteResult

/**
 * <code>Implicits</code> object to expose implicit conversions to implementing classes
 * which facilitate more Scala-like functionality in Mongo.
 *
 * For classes of <code>Mongo</code> (The connection class), <code>DB</code>, <code>DBCollection</code>,
 * and <code>DBCursor</code>, extension methods of asScala are added which will, when invoked,
 * return a Scala-ified wrapper class to replace the Java-driver class it was called on.
 *
 * These scala-ified wrappers do conversions to/from Java datatypes where necessary and will always return
 * Scala types.
 *
 * Additionally, Collection and Cursors can be called with <code>asScalaTyped</code> and a type (either an
 * implicit or explicitly passed <code>Manifest</code> object is used to determine type) to return
 * a Type optimized version of themselves.  The type must be a subclass of DBObject, and it is up to YOU the
 * programmer to determine that your underlying collection can be deserialized to objects of type A.
 *
 * Type oriented Collections and Cursors will ALWAYS try to deserialize DBObjects to their type where appropriate
 * (exceptions are things like group and mapReduce which return non-standard data and will be DBObjects)
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
trait Implicits {

  /**
   * Implicit extension methods for Mongo's connection object.
   * Capable of returning a Scala optimized wrapper object.
   * @param conn A <code>Mongo</code> object to wrap
   */
  implicit def mongoConnAsScala(conn: com.mongodb.Mongo) = new {
    /**
     * Return a type-neutral Scala Wrapper object for the Connection
     * @return MongoConnection An instance of a scala wrapper containing the connection object
     */
    def asScala = new MongoConnection(conn)
  }

  /**
   * Implicit extension methods for Mongo's DB object.
   * Capable of returning a Scala optimized wrapper object.
   * @param db A <code>DB</code> object to wrap
   */
  implicit def mongoDBAsScala(db: com.mongodb.DB) = new {
    /**
     * Return a type-neutral Scala Wrapper object for the DB
     * @return MongoDB An instance of a scala wrapper containing the DB object
     */
    def asScala = new MongoDB(db)
  }

  /**
   * Implicit extension methods for Mongo's Collection object.
   * Capable of returning a Scala optimized wrapper object.
   * @param coll A <code>DBCollection</code> object to wrap
   */
  implicit def mongoCollAsScala(coll: com.mongodb.DBCollection) = new {
    /**
     * Return a type-neutral Scala wrapper object for the DBCollection
     * @return MongoCollection An instance of the scala wrapper containing the collection object.
     */
    def asScala: MongoCollection = new ConcreteMongoCollection(coll)

    def asLazyScala: LazyMongoCollection = new LazyMongoCollection(coll)

  }

  /**
   * Implicit extension methods for Mongo's DBCursor object.
   * Capable of returning a Scala optimized wrapper object.
   * @param cursor A <code>DBCursor</code> object to wrap
   */
  implicit def mongoCursorAsScala(cursor: com.mongodb.DBCursor) = new {
    /**
     * Return a type-neutral Scala wrapper object for the DBCursor
     * @return MongoCursor An instance of the scala wrapper containing the cursor object.
     */
    def asScala: MongoCursor = new ConcreteMongoCursor(cursor)

  }

  implicit def stringAsNamedCollectionMROutput(name: String) = map_reduce.MapReduceStandardOutput(name)

}

object Implicits extends Implicits with commons.Implicits with query.Implicits
object Imports extends Imports with commons.Imports with query.Imports
object BaseImports extends BaseImports with commons.BaseImports with query.BaseImports
object TypeImports extends TypeImports with commons.TypeImports with query.TypeImports

trait Imports extends BaseImports with TypeImports with Implicits

trait BaseImports {
  val MongoConnection = com.mongodb.casbah.MongoConnection
  val MongoDBAddress = com.mongodb.casbah.MongoDBAddress
  val MongoOptions = com.mongodb.casbah.MongoOptions
  val WriteConcern = com.mongodb.casbah.WriteConcern
  val BSONDecodingStrategy = com.mongodb.casbah.BSONDecodingStrategy
  val MapReduceCommand = com.mongodb.casbah.map_reduce.MapReduceCommand
  val MapReduceInlineOutput = com.mongodb.casbah.map_reduce.MapReduceInlineOutput
  val MapReduceMergeOutput = com.mongodb.casbah.map_reduce.MapReduceMergeOutput
  val MapReduceReduceOutput = com.mongodb.casbah.map_reduce.MapReduceReduceOutput
}

trait TypeImports {
  type MongoConnection = com.mongodb.casbah.MongoConnection
  type MongoCollection = com.mongodb.casbah.MongoCollection
  type LazyMongoCollection = com.mongodb.casbah.LazyMongoCollection
  type LazyDBObject = com.mongodb.LazyDBObject
  type MongoDB = com.mongodb.casbah.MongoDB
  type MongoCursor = com.mongodb.casbah.MongoCursor
  type LazyMongoCursor = com.mongodb.casbah.LazyMongoCursor
  type MongoURI = com.mongodb.casbah.MongoURI
  type MongoOptions = com.mongodb.MongoOptions
  type WriteConcern = com.mongodb.WriteConcern
  type BSONDecodingStrategy = com.mongodb.casbah.BSONDecodingStrategy
  type WriteResult = com.mongodb.WriteResult
  type MapReduceCommand = com.mongodb.casbah.map_reduce.MapReduceCommand
  type MapReduceResult = com.mongodb.casbah.map_reduce.MapReduceResult
  type MapReduceError = com.mongodb.casbah.map_reduce.MapReduceError
  type MapReduceCollectionBasedResult = com.mongodb.casbah.map_reduce.MapReduceCollectionBasedResult
  type MapReduceInlineResult = com.mongodb.casbah.map_reduce.MapReduceInlineResult
  type MapReduceException = com.mongodb.casbah.map_reduce.MapReduceException
  type MapReduceOutputTarget = com.mongodb.casbah.map_reduce.MapReduceOutputTarget
  type MapReduceMergeOutput = com.mongodb.casbah.map_reduce.MapReduceMergeOutput
  type MapReduceReduceOutput = com.mongodb.casbah.map_reduce.MapReduceReduceOutput
  type DBAddress = com.mongodb.DBAddress
}

// vim: set ts=2 sw=2 sts=2 et:
