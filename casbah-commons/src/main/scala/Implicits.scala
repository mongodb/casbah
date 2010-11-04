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
package commons

import scalaj.collection.Imports._

trait Implicits {
  import com.mongodb.{DBObject, BasicDBObject, BasicDBList}

  /*
   * Placeholder Type Alias 
   *
   * TODO - Make me a Type Class to define boundaries
   */
  type JSFunction = String
  
  /**
   * Implicit extension methods for Scala <code>Map[String, Any]</code>
   * to convert to Mongo DBObject instances.
   * Does not currently convert nested values.
   * @param map A map of [String, Any]
   */
  implicit def mapAsDBObject(map: scala.collection.Map[String, Any]) = new {
    /**
     * Return a Mongo <code>DBObject</code> containing the Map values
     * @return DBObject 
     */
    def asDBObject = map2MongoDBObject(map)
  }

  implicit def map2MongoDBObject(map: scala.collection.Map[String, Any]): DBObject = new BasicDBObject(map.asJava)

  
  /**
   * Hacky mildly absurd method for converting a <code>Product</code> (Example being any <code>Tuple</code>) to
   * a  Mongo <code>DBObject</code> on the fly to minimize spaghetti code from long builds of Maps or DBObjects.
   *
   * Intended to facilitate fluid code but may be dangerous.
   *
   * Note that due to built in limits on the base max arity of <code>Product</code> in Scala, 
   * you are limited to
   * a MAXIMUM of 22 arguments. Scala only ships with Tuple1 -> Tuple 22 ... Tuple23 won't work.
   *
   * Currently doesn't use Manifests to check the nested type but looks at it on the fly - 
   * you're responsible for
   * passing a Product containing nested Tuple2[] where _1 is your Key
  * (a string but no manifest used so type erasure wins)
   * and _2 is your Value which can be anything Mongo can serialize 
   * (e.g. you need to explicitly convert nested products for now)
   *
   * @param p A Product such as a Tuple containing Tuple2's of Key/Value query pairs for MongoDB
   * @return DBOBject a Proper mongoDB <code>DBObject</code> representative of the passed-in data
   * @throws IllegalArgumentException This will be thrown if nested values do not conform to Tuple2
   */
  def productToMongoDBObject(p: Product): DBObject = {
    val builder = MongoDBObject.newBuilder
    val arityRange = 0.until(p.productArity)
    for (i <- arityRange) {
      val x = p.productElement(i)
      if (x.isInstanceOf[Tuple2[_,_]]) {
        val t = x.asInstanceOf[Tuple2[String, Any]]
        builder += t._1 -> t._2
      } else if (p.productArity == 2 && p.productElement(0).isInstanceOf[String]) {
        val t = p.asInstanceOf[Tuple2[String, Any]]
        builder += t._1 -> t._2
        return builder.result
      } else {
        throw new IllegalArgumentException("Products to convert to DBObject must contain Tuple2's.")
      }
    }
    builder.result
  }

  /*
   * Implicit extension methods to convert Products to Mongo DBObject instances.
   */ 
  implicit def productPimp(p: Product) = new {
    /*
     * Return a Mongo <code>DBObject</code> containing the Map values
     * @return DBObject 
     */ 
    def asDBObject = productToMongoDBObject(p)
    def ++[A <% DBObject : Manifest](right: A): DBObject =
      asDBObject ++ wrapDBObj(right)
  }
  
  // This may cause misbehavor ... aka "HERE BE DRAGONS"
  implicit def pairToDBObject(pair: (String, DBObject)): DBObject = 
    pair.asDBObject

  // A few hacks for defining straight off conversions
  implicit def tuplePairUtils(pair: (String, Any)) = new {
    def ++[A <% DBObject : Manifest](right: A): DBObject = 
      pair.asDBObject ++ wrapDBObj(right)
    def ++(right: (String, Any)): DBObject = 
      pair.asDBObject ++ right.asDBObject
  }

  implicit def wrapDBObj(in: DBObject): MongoDBObject = 
    new MongoDBObject { val underlying = in }

  implicit def unwrapDBObj(in: MongoDBObject): DBObject = 
    in.underlying

  implicit def wrapDBList(in: BasicDBList): MongoDBList = 
    new MongoDBList { val underlying = in }

  implicit def unwrapDBList(in: MongoDBList): BasicDBList =
    in.underlying

}

object Implicits extends Implicits
object Imports extends Imports
object BaseImports extends BaseImports
object TypeImports extends TypeImports

trait Imports extends BaseImports with TypeImports with Implicits

trait BaseImports {
  val MongoDBObject = com.mongodb.casbah.commons.MongoDBObject
  val MongoDBList = com.mongodb.casbah.commons.MongoDBList
}

trait TypeImports {
  type MongoDBObject = com.mongodb.casbah.commons.MongoDBObject
  type MongoDBList = com.mongodb.casbah.commons.MongoDBList
  type DBObject = com.mongodb.DBObject
  type BasicDBObject = com.mongodb.BasicDBObject
  type BasicDBList = com.mongodb.BasicDBList
  type ObjectId = org.bson.types.ObjectId
  type DBRef = com.mongodb.DBRef
}

abstract class ValidBSONType[T]

object ValidBSONType {
  implicit object BasicBSONList extends ValidBSONType[org.bson.types.BasicBSONList]
  implicit object BasicDBList extends ValidBSONType[com.mongodb.BasicDBList]
  implicit object Binary extends ValidBSONType[org.bson.types.Binary]
  implicit object BSONTimestamp extends ValidBSONType[org.bson.types.BSONTimestamp]
  implicit object Code extends ValidBSONType[org.bson.types.Code]
  implicit object CodeWScope extends ValidBSONType[org.bson.types.CodeWScope]
  implicit object ObjectId extends ValidBSONType[org.bson.types.ObjectId]
  implicit object Symbol extends ValidBSONType[org.bson.types.Symbol]
  implicit object BSONObject extends ValidBSONType[org.bson.BSONObject]
  implicit object BasicDBObject extends ValidBSONType[com.mongodb.BasicDBObject]
  implicit object DBObject extends ValidBSONType[com.mongodb.DBObject]
}
// vim: set ts=2 sw=2 sts=2 et:
