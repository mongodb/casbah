/**
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
 *     http://bitbucket.org/novus/casbah
 * 
 * NOTICE: Portions of this work are derived from the Apache License 2.0 "mongo-scala-driver" work
 * by Alexander Azarov <azarov@osinka.ru>, available from http://github.com/alaz/mongo-scala-driver
 */

package com.novus.casbah
package mongodb

import query._

import com.mongodb._

import scalaj.collection.Imports._
import org.scala_tools.time.Imports._

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
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait Implicits extends FluidQueryBarewordOps {
  type JSFunction = String

  /**
   * Implicit extension methods for Mongo's connection object.
   * Capable of returning a Scala optimized wrapper object.
   * @param conn A <code>Mongo</code> object to wrap
   */
  implicit def mongoConnAsScala(conn: Mongo) = new {
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
  implicit def mongoDBAsScala(db: DB) = new {
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
  implicit def mongoCollAsScala(coll: DBCollection) = new {
    /**
     * Return a type-neutral Scala wrapper object for the DBCollection
     * @return MongoCollection An instance of the scala wrapper containing the collection object.
     */
    def asScala = new MongoCollection(coll)
    /**
     * Return a GENERIC Scala wrapper object for the DBCollection specific to a given Parameter type.
     * @return MongoCollection[A<:DBObject] An instance of the scala wrapper containing the collection object.
     */
    def asScalaTyped[A<:DBObject](implicit m: scala.reflect.Manifest[A]) = new MongoTypedCollection[A](coll)(m)
  }

  /**
   * Implicit extension methods for Mongo's DBCursor object.
   * Capable of returning a Scala optimized wrapper object.
   * @param cursor A <code>DBCursor</code> object to wrap
   */
  implicit def mongoCursorAsScala(cursor: DBCursor) = new {
    /**
     * Return a type-neutral Scala wrapper object for the DBCursor
     * @return MongoCursor An instance of the scala wrapper containing the cursor object.
     */
    def asScala = new MongoCursor(cursor)
   /**
    * Return a GENERIC Scala wrapper object for the DBCursor specific to a given Parameter type.
    * @return MongoCursor[A<:DBObject] An instance of the scala wrapper containing the cursor object.
    */
    def asScalaTyped[A <: DBObject : Manifest] = new MongoTypedCursor[A](cursor)
  }

  /**
   * Implicit extension methods for String values (e.g. a field name)
   * to add Mongo's query operators, minimizing the need to write long series'
   * of nested maps.
   *
   * Mixes in the QueryOperators defined in the QueryOperators mixin.
   * The NestedQuery implicit [Defined below] allows you to call chained operators on the return value of this
   * method.  Chained operators will place the subsequent operators within the same DBObject,
   * e.g. <code>"fooDate" $lte yesterday $gte tomorrow</code> maps to a Mongo query of:
   * <code>{"fooDate": {"$lte": <yesterday>, "$gte": <tomorrow>}}</code>
   * 
   * @param left A string which should be the field name, the left hand of the query
   * @return Tuple2[String, DBObject] A tuple containing the field name and the mapped operator value, suitable for instantiating a Map
   */
  implicit def mongoQueryStatements(left: String) = new {
    val field = left
  } with FluidQueryOperators


  /**
   * Implicit extension methods for Tuple2[String, DBObject] values
   * to add Mongo's query operators, minimizing the need to write long series'
   * of nested maps.
   *
   * Mixes in the QueryOperators defined in the QueryOperators mixin.
   * The NestedQuery implicits allows you to call chained operators on the return value of the
   * base String method method.  Chained operators will place the subsequent operators within the same DBObject,
   * e.g. <code>"fooDate" $lte yesterday $gte tomorrow</code> maps to a Mongo query of:
   * <code>{"fooDate": {"$lte": <yesterday>, "$gte": <tomorrow>}}</code>
   *
   * @param left A string which should be the field name, the left hand of the query
   * @return Tuple2[String, DBObject] A tuple containing the field name and the mapped operator value, suitable for instantiating a Map
   */
  implicit def mongoNestedQueryStatements(nested: Tuple2[String, DBObject]) = new {
    val field = nested._1
  } with FluidQueryOperators { 
    dbObj = Some(nested._2) 
/*    def ++[A <% DBObject](right: A): DBObject = wrapDBObj(nested) ++ wrapDBObj(right)
*/  }
  
  implicit def wrapDBFile(in: com.mongodb.gridfs.GridFSDBFile) = new GridFSDBFile(in)
  implicit def wrapInFile(in: com.mongodb.gridfs.GridFSInputFile) = new GridFSInputFile(in)

} 

object Implicits extends Implicits
object Imports extends Imports 
object BaseImports extends BaseImports
object MongoTypeImports extends MongoTypeImports

trait Imports extends BaseImports with MongoTypeImports with Implicits 

trait BaseImports {
  val MongoConnection = com.novus.casbah.mongodb.MongoConnection
  val MongoDBAddress = com.novus.casbah.mongodb.MongoDBAddress
  val GridFS = com.novus.casbah.mongodb.gridfs.GridFS
  val MapReduceCommand = com.novus.casbah.mongodb.map_reduce.MapReduceCommand
}

trait TypeImports {
  type MongoConnection = com.novus.casbah.mongodb.MongoConnection
  type MongoCollection = com.novus.casbah.mongodb.MongoCollection
  type MongoDB = com.novus.casbah.mongodb.MongoDB
  type MongoCursor = com.novus.casbah.mongodb.MongoCursor
  type MapReduceCommand = com.novus.casbah.mongodb.map_reduce.MapReduceCommand
  type MapReduceResult = com.novus.casbah.mongodb.map_reduce.MapReduceResult
  type DBAddress = com.mongodb.DBAddress
}
