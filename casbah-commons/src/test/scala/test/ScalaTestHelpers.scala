/**
 * Copyright (c) 2010, 2011 10gen, Inc. <http://10gen.com>
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
package scalatest


import com.mongodb.casbah.commons.Imports._

import org.scalatest.matchers.Matcher
import org.scalatest.matchers.MatchResult

trait ScalaTestDBObjectMatchers {

  protected def someField(map: Option[DBObject], k: String) = if (k.indexOf('.') < 0) {
    map.getOrElse(MongoDBObject.empty).getAs[AnyRef](k)
  } else {
    map.getOrElse(MongoDBObject.empty).expand[AnyRef](k)
  }

  protected def field(map: DBObject, k: String) = if (k.indexOf('.') < 0) {
    map.getAs[AnyRef](k)
  } else {
    map.expand[AnyRef](k)
  }

  protected def listField(map: DBObject, k: String) = if (k.indexOf('.') < 0) {
    map.getAs[Seq[Any]](k)
  } else {
    map.expand[Seq[Any]](k)
  }

  val beDBObject = Matcher {
    left: AnyRef =>
      MatchResult(left.isInstanceOf[DBObject], left + " is a DBObject", left + " is not a DBObject")
  }

  def containSomeField(k: String) = Matcher {
    map: Option[DBObject] =>
      MatchResult(someField(map, k).isDefined, map.toString + " contains the key " + k, map.toString + " doesn't contain the key " + k)
  }

  /** matches if dbObject.contains(k) */
  def containField(k: String) = Matcher {
    map: DBObject =>
      MatchResult(field(map, k).isDefined, map.toString + " contains the key " + k, map.toString + " doesn't contain the key " + k)
  }

  /** matches if a Some(map) contains a pair (key, value) == (k, v)
    */
  def containSomeEntry[V](p: (String, V)) = Matcher {
    map: Option[DBObject] =>
      MatchResult(
        someField(map, p._1).exists(_ == p._2), // match only the value
        map.toString + " contain the pair " + p,
        map.toString + " doesn't contain the pair " + p
      )
  }

  /** Special version of "containEntry" that expects a list and then uses
    * "hasSameElements" on it.
    */
  def containListEntry(k: String, l: => scala.Traversable[Any]) = Matcher {
    map: DBObject =>
      val objL = listField(map, k).getOrElse(Seq.empty[Any]).toSeq
      val _l = l.toSeq
      MatchResult(
        objL.sameElements(_l), // match only the value
        map.toString + " has the pair " + k,
        map.toString + " doesn't have the pair " + k
      )
  }

  /** matches if map contains a pair (key, value) == (k, v)
    */
  // If a DBObject is a Map, can use contain already
  def containEntry[V](p: (String, V)) = Matcher {
    map: DBObject =>
      MatchResult(
        field(map, p._1).exists(_.equals(p._2)), // match only the value
        map.toString + " has the pair " + p,
        map.toString + "[" + field(map, p._1) + "] doesn't have the pair " + p + "[" + p._2 + "]"
      )
  }

  /** matches if Some(map) contains all the specified pairs
    * can expand dot notation to match specific sub-keys */
  def containSomeEntries[V](pairs: (String, V)*) = Matcher {
    map: Option[DBObject] =>
      MatchResult(
        pairs.forall(pair => someField(map, pair._1).exists(_ == pair._2) /* match only the value */),
        map.toString + " has the pairs " + pairs.mkString(", "),
        map.toString + " doesn't have the pairs " + pairs.mkString(", ")
      )
  }

  /** matches if map contains all the specified pairs
    * can expand dot notation to match specific sub-keys */
  def containEntries[V](pairs: (String, V)*) = Matcher {
    map: DBObject =>
      MatchResult(
        pairs.forall(pair => field(map, pair._1).exists(_ == pair._2) /* match only the value */),
        map.toString + " has the pairs " + pairs.mkString(", "),
        map.toString + " doesn't have the pairs " + pairs.mkString(", ")
      )
  }
}
