/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
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
 */

package com.mongodb.casbah.commons
package test


import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.Imports._

import org.specs2._
import org.specs2.data.Sized
import org.specs2.matcher.{Expectable, Matcher}
import org.specs2.matcher.Matchers._

trait CasbahMutableSpecification extends mutable.Specification with CasbahSpecificationBase

trait CasbahSpecification extends Specification with CasbahSpecificationBase {}

trait CasbahSpecificationBase extends SpecsDBObjectMatchers with Logging {


  implicit val sizedOptDBObj = new Sized[Option[DBObject]] {
    def size(t: Option[DBObject]) = t.getOrElse(MongoDBObject.empty).size
  }

  implicit val sizedDBObj = new Sized[DBObject] {
    def size(t: DBObject) = t.size
  }

  implicit val sizedOptDBList = new Sized[Option[MongoDBList]] {
    def size(t: Option[MongoDBList]) = {
      val item: MongoDBList = t.getOrElse(MongoDBList.empty)
      item.size
    }
  }

  implicit val sizedDBList = new Sized[MongoDBList] {
    def size(t: MongoDBList) = t.size
  }
}

trait SpecsDBObjectMatchers extends SpecsDBObjectBaseMatchers

trait SpecsDBObjectBaseMatchers extends Logging {

  protected def someField(map: Expectable[Option[DBObject]], k: String) = if (k.indexOf('.') < 0) {
    map.value.getOrElse(MongoDBObject.empty).getAs[AnyRef](k)
  } else {
    map.value.getOrElse(MongoDBObject.empty).expand[AnyRef](k)
  }

  protected def field(map: Expectable[DBObject], k: String) = if (k.indexOf('.') < 0) {
    map.value.getAs[AnyRef](k)
  } else {
    map.value.expand[AnyRef](k)
  }

  protected def listField(map: Expectable[DBObject], k: String) = if (k.indexOf('.') < 0) {
    map.value.getAs[Seq[Any]](k)
  } else {
    map.value.expand[Seq[Any]](k)
  }

  def beDBObject: Matcher[AnyRef] = ((_: AnyRef).isInstanceOf[DBObject], " is a DBObject", " is not a DBObject")

  def beMongoDBObject: Matcher[AnyRef] = ((_: AnyRef).isInstanceOf[MongoDBObject], " is a MongoDBObject", " is not a MongoDBObject")

  def beMongoDBList: Matcher[AnyRef] = ((_: AnyRef).isInstanceOf[MongoDBList], " is a MongoDBList", " is not a MongoDBList")

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
    * */
  def haveSomeEntry[V](p: (String, V)) = new Matcher[Option[DBObject]] {
    def apply[S <: Option[DBObject]](map: Expectable[S]) = {
      result(someField(map, p._1).exists(_ == p._2), // match only the value
        map.description + " has the pair " + p, map.description + " doesn't have the pair " + p, map)
    }
  }

  /** Special version of "HaveEntry" that expects a list and then uses
    * "hasSameElements" on it.
    */
  def haveListEntry(k: String, l: => Traversable[Any]) = new Matcher[DBObject] {
    def apply[S <: DBObject](map: Expectable[S]) = {
      val objL = listField(map, k).getOrElse(Seq.empty[Any]).toSeq
      val _l = l.toSeq
      result(objL.sameElements(_l), // match only the value
        map.description + " has the pair " + k,
        map.description + " doesn't have the pair " + k,
        map)
    }
  }

  /** matches if map contains a pair (key, value) == (k, v)
    * Will expand out dot notation for matching.
    * */
  def haveEntry[V](p: (String, V)) = new Matcher[DBObject] {
    def apply[S <: DBObject](map: Expectable[S]) = {
      result(field(map, p._1).exists(_.equals(p._2)), // match only the value
        map.description + " has the pair " + p,
        map.description + "[" + field(map, p._1) + "] doesn't have the pair " + p + "[" + p._2 + "]",
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
      result(pairs.forall(pair => field(map, pair._1).exists(_ == pair._2) /* match only the value */),
        map.description + " has the pairs " + pairs.mkString(", "),
        map.description + " doesn't have the pairs " + pairs.mkString(", "),
        map)
    }
  }

}
