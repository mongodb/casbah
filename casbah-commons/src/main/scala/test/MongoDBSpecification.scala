/**
 * Copyright (c) 2010, 2011 10gen, Inc. <http://10gen.com>
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
 */

package com.mongodb.casbah
package commons
package test

import com.mongodb.casbah.commons.Imports._

import org.specs2.mutable._
import org.specs2.data.Sized
import org.specs2.matcher.{ Expectable, Matcher, MapMatchers }
import com.mongodb.casbah.commons.MongoDBObject

object `package` {

}

trait CasbahSpecification extends Specification with DBObjectMatchers with Logging {

  implicit val sizedOptDBObj = new Sized[Option[DBObject]] {
    def size(t: Option[DBObject]) = t.getOrElse(MongoDBObject.empty).size
  }

  implicit val sizedDBObj = new Sized[DBObject] {
    def size(t: DBObject) = t.size
  }

  implicit val sizedOptDBList = new Sized[Option[MongoDBList]] {
    def size(t: Option[MongoDBList]) = t.getOrElse(MongoDBList.empty).size
  }

  implicit val sizedDBList = new Sized[MongoDBList] {
    def size(t: MongoDBList) = t.size
  }
}

trait DBObjectMatchers extends DBObjectBaseMatchers

trait DBObjectBaseMatchers {

  protected def someField(map: Expectable[Option[DBObject]], k: String) = if (k.indexOf('.') < 0) {
    map.value.getOrElse(MongoDBObject.empty).expand[AnyRef](k)
  } else {
    map.value.getOrElse(MongoDBObject.empty).getAs[AnyRef](k)
  }

  protected def field(map: Expectable[DBObject], k: String) = if (k.indexOf('.') < 0) { map.value.expand[AnyRef](k) } else { map.value.getAs[AnyRef](k) }

  def haveSomeField(k: String) = new Matcher[Option[DBObject]] {
    def apply[S <: Option[DBObject]](map: Expectable[S]) = {
      result(someField(map, k).isDefined, map.description + " has the key " + k, map.description + " doesn't have the key " + k, map)
    }
  }

  /** matches if dbObject.contains(k) */
  def haveField(k: String) = new Matcher[DBObject] {
    def apply[S <: DBObject](map: Expectable[S]) = {
      result(field(map, k).isDefined, map.description + " has the key " + k, map.description + " doesn't have the key " + k, map)
    }
  }

  /** matches if a Some(map) contains a pair (key, value) == (k, v)
   * Will expand out dot notation for matching.
   **/
  def haveSomeEntry[V](p: (String, V)) = new Matcher[Option[DBObject]] {
    def apply[S <: Option[DBObject]](map: Expectable[S]) = {
      result(someField(map, p._1).exists(_ == p._2), // match only the value
      map.description + " has the pair " + p, map.description + " doesn't have the pair " + p, map)
    }
  }

  /** matches if map contains a pair (key, value) == (k, v)
   * Will expand out dot notation for matching.
   **/
  def haveEntry[V](p: (String, V)) = new Matcher[DBObject] {
    def apply[S <: DBObject](map: Expectable[S]) = {
      result(field(map, p._1) == p._2, // match only the value
        map.description + " has the pair " + p,
        map.description + " doesn't have the pair " + p,
        map)
    }
  }

  /** matches if Some(map) contains all the specified pairs
   * can expand dot notation to match specific sub-keys */
  def haveSomeEntries[V](pairs: (String, V)*) = new Matcher[Option[DBObject]] {
    def apply[S <: Option[DBObject]](map: Expectable[S]) = {
      result(pairs.forall(pair => someField(map, pair._1).exists(_ == pair._2) /* match only the value */),
             map.description + " has the pairs " + pairs.mkString(", "), map.description + " doesn't have the pairs " + pairs.mkString(", "), map)
    }
  }

  /** matches if map contains all the specified pairs
   * can expand dot notation to match specific sub-keys */
  def haveEntries[V](pairs: (String, V)*) = new Matcher[DBObject] {
    def apply[S <: DBObject](map: Expectable[S]) = {
      result(pairs.forall(pair => field(map, pair._1) == pair._2 /* match only the value */),
        map.description + " has the pairs " + pairs.mkString(", "),
        map.description + " doesn't have the pairs " + pairs.mkString(", "),
        map)
    }
  }

}
