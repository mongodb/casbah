/**
 * Copyright (c) 2008 - 2011 10gen, Inc. <http://10gen.com>
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
package com.mongodb.casbah.util.bson.primitive

import scala.util.matching.Regex
import java.util.regex.Pattern
import org.scala_tools.time.Imports._
import org.bson.BSON

trait Builtins {

  /**
   * Scala RegEx
   */
  trait ScalaRegexPrimitive extends BSONRegexPrimitive[Regex] {
    def rawValue(bson: (String /* pattern */, String /* flags */)): scala.util.matching.Regex = {
      // Construct a RegEx
      "(?%s)%s".format(bson._2, bson._1).r
    }

    def bsonValue(raw: Regex): (String /* pattern */, String /* flags */) = {
      // Reduce a Scala Regex to a raw BSON tuple
      val jPat = raw.pattern
      (jPat.pattern, BSON.regexFlags(jPat.flags))
    }
  }

  /**
   * Java Regex
   */
  trait JavaRegexPrimitive extends BSONRegexPrimitive[java.util.regex.Pattern] {
    def rawValue(bson: (String /* pattern */, String /* flags */)): java.util.regex.Pattern = {
      // Construct a RegEx
      Pattern.compile("(?%s)%s".format(bson._2, bson._1))
    }

    def bsonValue(raw: Regex): (String /* pattern */, String /* flags */) = {
      // Reduce a Scala Regex to a raw BSON tuple
      (raw.pattern.pattern, BSON.regexFlags(raw.pattern.flags))
    }
  }

  /**
   * 'Option' can't be done ... normally.  Might need special logic
   */

  /**
   * Some Scala collections for fun and profit.
   *
   * TODO - Immutable versions
   */
  def typedMutableBufferPrimitive[A]: BSONArrayPrimitive[A,  scala.collection.mutable.Buffer[A]] = new MutableBufferPrimitive[A] {}

  trait MutableBufferPrimitive[A] extends BSONArrayPrimitive[A, scala.collection.mutable.Buffer[A]] {
    def rawValue(bson: Seq[A]): scala.collection.mutable.Buffer[A] = {
      bson.toBuffer
/*      // TODO - This is mostly utterly useless and a bit wasteful, ya?
      scala.collection.mutable.Buffer(bson: _*)*/
    }

    def bsonValue(raw: scala.collection.mutable.Buffer[A]): Seq[A] = {
      raw.toSeq
    }  
  }

  def typedMutableSeqPrimitive[A]: BSONArrayPrimitive[A, scala.collection.mutable.Seq[A]] = new MutableSeqPrimitive[A] {}

  trait MutableSeqPrimitive[A] extends BSONArrayPrimitive[A, scala.collection.mutable.Seq[A]] {
    def rawValue(bson: Seq[A]): scala.collection.mutable.Seq[A] = {
      // TODO - This is mostly utterly useless and a bit wasteful, ya?
      scala.collection.mutable.Seq(bson: _*)
    }

    def bsonValue(raw: scala.collection.mutable.Seq[A]): Seq[A] = {
      raw.toSeq
    }
  }

  def typedMutableSetPrimitive[A]: BSONArrayPrimitive[A, scala.collection.mutable.Set[A]] = new MutableSetPrimitive[A] {}

  trait MutableSetPrimitive[A] extends BSONArrayPrimitive[A, scala.collection.mutable.Set[A]] {
    def rawValue(bson: Seq[A]): scala.collection.mutable.Set[A] = {
      // TODO - This is mostly utterly useless and a bit wasteful, ya?
      scala.collection.mutable.Set(bson: _*)
    }

    def bsonValue(raw: scala.collection.mutable.Set[A]): Seq[A] = {
      raw.toSeq
    }
  }

  def typedIterablePrimitive[A]: BSONArrayPrimitive[A, scala.collection.Iterable[A]] = new IterablePrimitive[A] {}

  trait IterablePrimitive[A] extends BSONArrayPrimitive[A, scala.collection.Iterable[A]] {
    def rawValue(bson: Seq[A]): scala.collection.Iterable[A] = {
      bson.toIterable
    }

    def bsonValue(raw: scala.collection.Iterable[A]): Seq[A] = {
      raw.toSeq
    }
  }

  def typedIteratorPrimitive[A]: BSONArrayPrimitive[A, scala.collection.Iterator[A]] = new IteratorPrimitive[A] {}

  trait IteratorPrimitive[A] extends BSONArrayPrimitive[A, scala.collection.Iterator[A]] {
    def rawValue(bson: Seq[A]): scala.collection.Iterator[A] = {
      bson.iterator
    }

    def bsonValue(raw: scala.collection.Iterator[A]): Seq[A] = {
      raw.toSeq
    }
  }

  trait JodaDateTimePrimitive extends BSONDatePrimitive[DateTime] {
    def rawValue(bson: Long): DateTime = {
        new DateTime(bson)
    }

    def bsonValue(raw: DateTime): Long = {
      raw.getMillis
    }
  }

  trait JDKDatePrimitive extends BSONDatePrimitive[java.util.Date] {
    def rawValue(bson: Long): java.util.Date = {
        new java.util.Date(bson)
    }

    def bsonValue(raw: java.util.Date): Long = {
      raw.getTime
    }
  }



}


