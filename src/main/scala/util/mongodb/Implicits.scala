/**
 * Copyright (c) 2010, Novus Partners, Inc. <http://novus.com>
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
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

package com.novus.util.mongodb

import com.mongodb._
import org.scala_tools.javautils.Imports._

object Implicits {
  type JSFunction = String

  implicit def mongoConnAsScala(conn: Mongo) = new {
    def asScala = new ScalaMongoConn(conn)
  }

  implicit def mongoDBAsScala(db: DB) = new {
    def asScala = new ScalaMongoDB(db)
  }

  implicit def mongoCollAsScala(coll: DBCollection) = new {
    def asScala = new ScalaMongoCollection(coll)
    def asScalaTyped[A<:DBObject](implicit m: scala.reflect.Manifest[A]) = new ScalaTypedMongoCollection[A](coll)(m)
  }

  implicit def mongoCursorAsScala(cursor: DBCursor) = new {
    def asScala = new ScalaMongoCursor(cursor)
    def asScalaTyped[A<:DBObject](implicit m: scala.reflect.Manifest[A])  = new ScalaTypedMongoCursor[A](cursor)(m)
  }

  implicit def mapAsDBObject(map: Map[String, Any]) = new {
    def asDBObject = BasicDBObjectBuilder.start(map.asJava).get
  }

  implicit def mongoQueryStatements(left: String) = new {
    val field = left
  } with QueryOperators

  implicit def mongoNestedQueryStatements(nested: Tuple2[String, DBObject]) = new {
    val field = nested._1
  } with QueryOperators { dbObj = Some(nested._2) }

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
