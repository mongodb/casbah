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

object `package` {

}

trait CasbahSpecification extends Specification with DBObjectMatchers with Logging {

  implicit val sizedDBObj = new Sized[DBObject] {
    def size(t: DBObject) = t.size
  }

}

trait DBObjectMatchers extends DBObjectBaseMatchers

trait DBObjectBaseMatchers {

  def haveSomeField[K](k: K) = new Matcher[Option[DBObject]] {
    def apply[S <: Option[DBObject]](map: Expectable[S]) = {
      result(map.value.getOrElse(MongoDBObject.empty).exists(_._1 == k),
        map.description + " has the key " + k,
        map.description + " doesn't have the key " + k,
        map)
    }
  }

  /** matches if dbObject.contains(k) */
  def haveField[K](k: K) = new Matcher[DBObject] {
    def apply[S <: DBObject](map: Expectable[S]) = {
      result(map.value.exists(_._1 == k),
        map.description + " has the key " + k,
        map.description + " doesn't have the key " + k,
        map)
    }
  }

  def haveSomeEntry[V](p: (String, V)) = new Matcher[Option[DBObject]] {
    def apply[S <: Option[DBObject]](map: Expectable[S]) = {
      result(map.value.getOrElse(MongoDBObject.empty).exists(_ == p),
        map.description + " has the pair " + p,
        map.description + " doesn't have the pair " + p,
        map)
    }
  }

  /** matches if map contains a pair (key, value) == (k, v) */
  def haveEntry[V](p: (String, V)) = new Matcher[DBObject] {
    def apply[S <: DBObject](map: Expectable[S]) = {
      result(map.value.exists(_ == p),
        map.description + " has the pair " + p,
        map.description + " doesn't have the pair " + p,
        map)
    }
  }

  /** matches if map contains all the specified pairs */
  def haveSomeEntries[V](pairs: (String, V)*) = new Matcher[Option[DBObject]] {
    def apply[S <: Option[DBObject]](map: Expectable[S]) = {
      result(pairs.forall(pair => map.value.getOrElse(MongoDBObject.empty).exists(_ == pair)),
        map.description + " has the pairs " + pairs.mkString(", "),
        map.description + " doesn't have the pairs " + pairs.mkString(", "),
        map)
    }
  }

  /** matches if map contains all the specified pairs */
  def haveEntries[V](pairs: (String, V)*) = new Matcher[DBObject] {
    def apply[S <: DBObject](map: Expectable[S]) = {
      result(pairs.forall(pair => map.value.exists(_ == pair)),
        map.description + " has the pairs " + pairs.mkString(", "),
        map.description + " doesn't have the pairs " + pairs.mkString(", "),
        map)
    }
  }

}
