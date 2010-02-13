/**
 *  Copyright (c) 2010, Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: Portions of this work are derived from the Apache License 2.0 "mongo-scala-driver" work
 * by Alexander Azarov <azarov@osinka.ru>, available from http://github.com/alaz/mongo-scala-driver
 */

package com.novus.mongodb

import com.mongodb._
import org.scala_tools.javautils.Imports._

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
object Implicits {
  type JSFunction = String

  /**
   * Implicit extension methods for Mongo's connection object.
   * Capable of returning a Scala optimized wrapper object.
   * @param conn A <code>Mongo</code> object to wrap
   */
  implicit def mongoConnAsScala(conn: Mongo) = new {
   /**
    * Return a type-neutral Scala Wrapper object for the Connection
    * @return ScalaMongoConn An instance of a scala wrapper containing the connection object
    */
    def asScala = new ScalaMongoConn(conn)
  }

  /**
   * Implicit extension methods for Mongo's DB object.
   * Capable of returning a Scala optimized wrapper object.
   * @param db A <code>DB</code> object to wrap
   */
  implicit def mongoDBAsScala(db: DB) = new {
    /**
     * Return a type-neutral Scala Wrapper object for the DB
     * @return ScalaMongoDB An instance of a scala wrapper containing the DB object
     */
    def asScala = new ScalaMongoDB(db)
  }

  /**
   * Implicit extension methods for Mongo's Collection object.
   * Capable of returning a Scala optimized wrapper object.
   * @param coll A <code>DBCollection</code> object to wrap
   */
  implicit def mongoCollAsScala(coll: DBCollection) = new {
    /**
     * Return a type-neutral Scala wrapper object for the DBCollection
     * @return ScalaMongoCollection An instance of the scala wrapper containing the collection object.
     */
    def asScala = new ScalaMongoCollection(coll)
    /**
     * Return a GENERIC Scala wrapper object for the DBCollection specific to a given Parameter type.
     * @return ScalaMongoCollection[A<:DBObject] An instance of the scala wrapper containing the collection object.
     */
    def asScalaTyped[A<:DBObject](implicit m: scala.reflect.Manifest[A]) = new ScalaTypedMongoCollection[A](coll)(m)
  }

  /**
   * Implicit extension methods for Mongo's DBCursor object.
   * Capable of returning a Scala optimized wrapper object.
   * @param cursor A <code>DBCursor</code> object to wrap
   */
  implicit def mongoCursorAsScala(cursor: DBCursor) = new {
    /**
     * Return a type-neutral Scala wrapper object for the DBCursor
     * @return ScalaMongoCursor An instance of the scala wrapper containing the cursor object.
     */
    def asScala = new ScalaMongoCursor(cursor)
   /**
    * Return a GENERIC Scala wrapper object for the DBCursor specific to a given Parameter type.
    * @return ScalaMongoCursor[A<:DBObject] An instance of the scala wrapper containing the cursor object.
    */
    def asScalaTyped[A<:DBObject](implicit m: scala.reflect.Manifest[A])  = new ScalaTypedMongoCursor[A](cursor)(m)
  }

  /**
   * Implicit extension methods for Scala <code>Map[String, Any]</code>
   * to convert to Mongo DBObject instances.
   * Does not currently convert nested values.
   * @param map A map of [String, Any]
   */
  implicit def mapAsDBObject(map: Map[String, Any]) = new {
    /**
     * Return a Mongo <code>DBObject</code> containing the Map values
     * @return DBObject 
     */
    def asDBObject = BasicDBObjectBuilder.start(map.asJava).get
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
  } with QueryOperators

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
  } with QueryOperators { dbObj = Some(nested._2) }

  /**
   * Hacky mildly absurd method for converting a <code>Product</code> (Example being any <code>Tuple</code>) to
   * a  Mongo <code>DBObject</code> on the fly to minimize spaghetti code from long builds of Maps or DBObjects.
   *
   * Intended to facilitate fluid code but may be dangerous.
   *
   * Note that due to built in limits on the base max arity of <code>Product</code> in Scala 2.7.x, you are limited to
   * a MAXIMUM of 22 arguments.  Scala will presumably handle this for you (There is no Tuple23 object...).
   *
   * Currently doesn't use Manifests to check the nested type but looks at it on the fly - you're responsible for
   * passing a Product containing nested Tuple2[] where _1 is your Key (a string but no manifest used so type erasure wins)
   * and _2 is your Value which can be anything Mongo can serialize (e.g. you need to explicitly convert nested products for now)
   *
   * @param p A Product such as a Tuple containing Tuple2's of Key/Value query pairs for MongoDB
   * @return DBOBject a Proper mongoDB <code>DBObject</code> representative of the passed-in data
   * @throws IllegalArgumentException This will be thrown if nested values do not conform to Tuple2
   */
  implicit def productToMongoDBObject(p: Product): DBObject = {
    val builder = BasicDBObjectBuilder.start
    val arityRange =  0.until(p.productArity)
    //println("Converting Product P %s with an Arity range of %s to a MongoDB Object".format(p, arityRange))
    for (i <- arityRange) {
      val x = p.productElement(i)
      //println("\tI: %s X: %s".format(i, x))
      if (x.isInstanceOf[Tuple2[_,_]]) {
        val t = x.asInstanceOf[Tuple2[String, Any]]
        //println("\t\tT: %s".format(t))
        builder.add(t._1, t._2)
      } else {
        throw new IllegalArgumentException("Products to convert to DBObject must contain Tuple2's.")
      }
    }
    builder.get
  }

}
