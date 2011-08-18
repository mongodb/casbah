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
package com.mongodb.casbah.test

import com.mongodb.casbah.Imports._

import com.mongodb.casbah.commons.test.CasbahSpecification

class LazyDecodingSpec extends CasbahSpecification {
  implicit val mongoDB = MongoConnection()("casbahIntegration")

  "Lazy Collections" should {
    "Be fetchable, and return LazyCursors and LazyDBObjects" in {
      val coll = mongoDB.lazyCollection("books")
      coll must haveClass[LazyMongoCollection]
      coll.find() must haveClass[LazyMongoCursor]
      coll.find().next() must haveClass[LazyDBObject]
    }

    "Perform better, overall, than Standard DBObjects in 500 iterations" in {
      def fetchBook(obj: DBObject) = {
        val start = System.nanoTime()
        val id = obj.getAs[ObjectId]("_id")
        val isbn = obj.getAs[String]("ISBN")
        val author = obj.getAs[String]("author")
        val discountPrice = obj.expand[Double]("price.discount")
        val msrpPrice = obj.expand[Double]("price.msrp")
        val publicationYear = obj.getAs[Int]("publicationYear")
        val tags = obj.getAs[Seq[String]]("tags")
        val title = obj.getAs[String]("title")
        (System.nanoTime() - start).toDouble / 1000000000
      }

      val stdColl = mongoDB("books")
      val lazyColl = mongoDB.lazyCollection("books")

      val stdCount = stdColl.count
      val lazyCount = lazyColl.count

      stdCount must beGreaterThan(0L)
      lazyCount must beGreaterThan(0L)

      stdCount must_==(lazyCount)

      def runSum(c: MongoCollection) =
        c.find().map(doc => fetchBook(doc)).sum

      val stdTotal = runSum(stdColl)
      val lazyTotal = runSum(lazyColl)

      lazyTotal must beGreaterThan(0.0)
      stdTotal must beGreaterThan(0.0)

      lazyTotal must beLessThan(stdTotal)

      val stdTime = stdTotal / stdCount
      val lazyTime = lazyTotal / lazyCount

      System.err.println("Average Seconds Per Doc STD: %2.6f".format(stdTime))
      System.err.println("Average Seconds Per Doc Lazy: %2.6f".format(lazyTime))

      lazyTime must beLessThan(stdTime)
    }

  }

}