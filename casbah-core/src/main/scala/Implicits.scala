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

import scala.language.implicitConversions

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
 */
trait Implicits {
  // scalastyle:off public.methods.have.type
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
    def asScala: MongoCollection = new MongoCollection(coll)

    /**
     * Return a GENERIC Scala wrapper object for the DBCollection specific to a given Parameter type.
     * @return MongoCollection[A<:DBObject] An instance of the scala wrapper containing the collection object.
     */
    def asScalaTyped[A <: com.mongodb.DBObject](implicit m: scala.reflect.Manifest[A]): MongoGenericTypedCollection[A] =
      new MongoGenericTypedCollection[A](coll)
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
    def asScala: MongoCursor = new MongoCursor(cursor)

    /**
     * Return a GENERIC Scala wrapper object for the DBCursor specific to a given Parameter type.
     * @return MongoCursor[A<:DBObject] An instance of the scala wrapper containing the cursor object.
     */
    def asScalaTyped[A <: com.mongodb.DBObject: Manifest] = new MongoGenericTypedCursor[A](cursor)
  }

  /**
   * Implicit extension methods for Mongo's Aggregation MongoCursor object.
   * Capable of returning a Scala optimized wrapper object.
   * @param cursor A <code>Cursor</code> object to wrap
   */
  implicit def mongoCommandCursorAsScala(cursor: com.mongodb.Cursor) = new {
    /**
     * Return a type-neutral Scala wrapper object for the MongoCursor
     * @return Cursor An instance of the scala wrapper containing the cursor object.
     */
    def asScala: Cursor = Cursor(cursor)
  }

  implicit def stringAsNamedCollectionMROutput(name: String) = map_reduce.MapReduceStandardOutput(name)

  implicit def aggregationOutputAsScala(output: com.mongodb.AggregationOutput) = new {
    /**
     * Return a type-neutral Scala Wrapper object for the DB
     * @return MongoDB An instance of a scala wrapper containing the DB object
     */
    def asScala = new AggregationOutput(output)
  }
  // scalastyle:on public.methods.have.type
}

object Implicits extends Implicits with commons.Implicits with query.Implicits

object Imports extends Imports with commons.Imports with query.Imports with query.dsl.FluidQueryBarewordOps

object BaseImports extends BaseImports with commons.BaseImports with query.BaseImports

object TypeImports extends TypeImports with commons.TypeImports with query.TypeImports

trait Imports extends BaseImports with TypeImports with Implicits

@SuppressWarnings(Array("deprecation"))
trait BaseImports {
  val MongoClient = com.mongodb.casbah.MongoClient
  val MongoConnection = com.mongodb.casbah.MongoConnection
  val MongoDBAddress = com.mongodb.casbah.MongoDBAddress
  val MongoOptions = com.mongodb.casbah.MongoOptions
  val MongoClientOptions = com.mongodb.casbah.MongoClientOptions
  val MongoClientURI = com.mongodb.casbah.MongoClientURI
  val MongoCredential = com.mongodb.casbah.MongoCredential
  val ParallelScanOptions = com.mongodb.casbah.ParallelScanOptions
  val AggregationOptions = com.mongodb.casbah.AggregationOptions
  val AggregationOutput = com.mongodb.casbah.AggregationOutput
  val Cursor = com.mongodb.casbah.Cursor
  val WriteConcern = com.mongodb.casbah.WriteConcern
  val ReadPreference = com.mongodb.casbah.ReadPreference
  val MapReduceCommand = com.mongodb.casbah.map_reduce.MapReduceCommand
  val MapReduceInlineOutput = com.mongodb.casbah.map_reduce.MapReduceInlineOutput
  val MapReduceMergeOutput = com.mongodb.casbah.map_reduce.MapReduceMergeOutput
  val MapReduceReduceOutput = com.mongodb.casbah.map_reduce.MapReduceReduceOutput
}

trait TypeImports {
  type MongoConnection = com.mongodb.casbah.MongoConnection
  type MongoCollection = com.mongodb.casbah.MongoCollection
  type MongoDB = com.mongodb.casbah.MongoDB
  type MongoCursor = com.mongodb.casbah.MongoCursor
  type MongoURI = com.mongodb.casbah.MongoURI
  type MongoOptions = com.mongodb.MongoOptions
  type MongoClient = com.mongodb.casbah.MongoClient
  type MongoClientOptions = com.mongodb.MongoClientOptions
  type MongoCredential = com.mongodb.MongoCredential
  type MongoClientURI = com.mongodb.MongoClientURI
  type BulkWriteOperation = com.mongodb.casbah.BulkWriteOperation
  type BulkWriteResult = com.mongodb.casbah.BulkWriteResult
  type BulkWriteException = com.mongodb.casbah.BulkWriteException
  type AggregationOutput = com.mongodb.casbah.AggregationOutput
  type CommandCursor = com.mongodb.casbah.Cursor
  type AggregationOptions = com.mongodb.AggregationOptions
  type WriteConcern = com.mongodb.WriteConcern
  type ReadConcern = com.mongodb.ReadConcern
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
  type ReadPreference = com.mongodb.ReadPreference
  type ServerAddress = com.mongodb.ServerAddress
  type DBEncoder = com.mongodb.DBEncoder
  type DBDecoder = com.mongodb.DBDecoder
}
