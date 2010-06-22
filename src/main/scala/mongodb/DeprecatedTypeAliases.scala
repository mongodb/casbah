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

import com.mongodb._

/** 
 * Type aliases for deprecated object names (aka the "Old" object naming)
 * These should not be used and are provided for backwards compatibility only.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0, 06/22/10
 * @since 1.0
 * @deprecated
 */
package object mongodb {
  /**
   * Wrapper object for Mongo Connections, providing the static methods the Java driver gives.
   * Apply methods are called as MongoConnectionection(<params>)
   *
   * @author Brendan W. McAdams <bmcadams@novus.com>
   * @version 1.0
   */
  @deprecated("ScalaMongoConn has been deprecated. Please use com.novus.casbah.mongodb.MongoConnection instead.")
  object ScalaMongoConn {
    def apply() = new MongoConnection(new Mongo())
    def apply(addr: DBAddress) = new MongoConnection(new Mongo(addr))
    def apply(left: DBAddress, right: DBAddress) = new MongoConnection(new Mongo(left, right))
    def apply(left: DBAddress, right: DBAddress, options: MongoOptions) = new MongoConnection(new Mongo(left, right, options))
    def apply(addr: DBAddress, options: MongoOptions) = new MongoConnection(new Mongo(addr, options))
    def apply(host: String) = new MongoConnection(new Mongo(host))
    def apply(host: String, port: Int) = new MongoConnection(new Mongo(host, port))
    //def apply(host: String, options: MongoOptions) = new MongoConnection(new Mongo(host, options))
  }

  @deprecated("ScalaMongoDB has been deprecated. Please use com.novus.casbah.mongodb.MongoDB instead.")
  type ScalaMongoDB = MongoDB
  @deprecated("ScalaDBObject has been deprecated. Please use com.novus.casbah.mongodb.MongoDBObject instead.")
  type ScalaDBObject = MongoDBObject
  @deprecated("ScalaMongoConn has been deprecated. Please use com.novus.casbah.mongodb.MongoConnection instead.")
  type ScalaMongoConn = MongoConnection
  @deprecated("ScalaMongoCollectionWrapper has been deprecated. Please use com.novus.casbah.mongodb.MongoCollectionWrapper instead.")
  type ScalaMongoCollectionWrapper = MongoCollectionWrapper
  @deprecated("ScalaMongoCollection has been deprecated. Please use com.novus.casbah.mongodb.MongoCollection instead.")
  type ScalaMongoCollection = MongoCollection
  @deprecated("ScalaTypedMongoCollection[A <: DBObject] has been deprecated. Please use com.novus.casbah.mongodb.MongoTypedCollection[A <: DBObject] instead.")
  type ScalaTypedMongoCollection[A <: DBObject] = MongoTypedCollection[A]
  @deprecated("ScalaMongoCursorWrapper[A <: DBObject] has been deprecated. Please use com.novus.casbah.mongodb.MongoCursorWrapper[A <: DBObject] instead.")
  type ScalaMongoCursorWrapper[A <: DBObject] = MongoCursorWrapper[A]
  @deprecated("ScalaMongoCursor has been deprecated. Please use com.novus.casbah.mongodb.MongoCursor instead.")
  type ScalaMongoCursor = MongoCursor
  @deprecated("ScalaTypedMongoCursor[A <: DBObject] has been deprecated. Please use com.novus.casbah.mongodb.MongoTypedCursor[A <: DBObject] instead.")
  type ScalaTypedMongoCursor[A <: DBObject] = MongoTypedCursor[A]
}


// vim: set ts=2 sw=2 sts=2 et:
