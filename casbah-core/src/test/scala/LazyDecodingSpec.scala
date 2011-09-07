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
import com.mongodb.casbah.util.bson.decoding.OptimizedLazyDBObject
import java.util.{UUID , Date}
import org.bson.types.{MinKey, MaxKey, BSONTimestamp, Binary}
import java.util.regex.Pattern

class LazyDecodingSpec extends CasbahSpecification {
  implicit val mongoInt = MongoConnection()("casbahIntegration")
  implicit val mongoTest = MongoConnection()("casbahTest_Lazy")

  val x = 10

  "Lazy Collections" should {
    "Be fetchable, and return LazyCursors and LazyDBObjects" in {
      val coll = mongoInt.lazyCollection("books")
      coll must haveClass[LazyMongoCollection]
      coll.find() must haveClass[LazyMongoCursor]
      coll.find().next() must haveClass[OptimizedLazyDBObject]
    }

    "Perform better, overall, than Standard DBObjects in %d iterations".format(x) in {
      def fetchBook(obj: DBObject) ={
        val start = System.nanoTime()
        val id = obj.getAs[ObjectId]("_id")
        val isbn = obj.getAs[String]("isbn")
        val author = obj.getAs[String]("author")
        val discountPrice = obj.expand[Double]("price.discount")
        val msrpPrice = obj.expand[Double]("price.msrp")
        val publicationYear = obj.getAs[Int]("publicationYear")
        val tags = obj.getAs[Seq[String]]("tags")
        val title = obj.getAs[String]("title")
        ( System.nanoTime() - start ).toDouble / 1000000000
      }

      val stdColl = mongoInt("books")
      val lazyColl = mongoInt.lazyCollection("books")

      val stdCount = stdColl.count
      val lazyCount = lazyColl.count

      stdCount must beGreaterThan(0L)
      lazyCount must beGreaterThan(0L)

      stdCount must_== ( lazyCount )

      def runSum(c: MongoCollection) =
        c.find().map(doc => fetchBook(doc)).sum

      var stdTotal = 0.0
      for ( i <- 0 until x )
        stdTotal += runSum(stdColl)

      var lazyTotal = 0.0
      for ( i <- 0 until x )
        lazyTotal += runSum(lazyColl)

      val stdTime = ( stdTotal / stdCount ) / x
      val lazyTime = ( lazyTotal / lazyCount ) / x

      System.err.println("[Total: %12.6f seconds] Average Seconds Per Doc STD: %2.6f".format(stdTotal , stdTime))
      System.err.println("[Total: %12.6f seconds] Average Seconds Per Doc Lazy: %2.6f".format(lazyTotal , lazyTime))


      lazyTotal must beGreaterThan(0.0)
      stdTotal must beGreaterThan(0.0)

      //lazyTotal must beLessThan(stdTotal)

      //lazyTime must beLessThan(stdTime)
    }

    "Properly decode and read all supported/expected datatypes" in {
      val oid = new ObjectId
      val testOid = new ObjectId
      val testRefOid = mongoInt("books").findOne().get.getAs[ObjectId]("_id")
      val testDoc = MongoDBObject("abc" -> "12345")
      val testArr = Array[String]("foo" , "bar" , "baz" , "x" , "y" , "z")
      val testList = MongoDBList("foo" , "bar" , "baz" , "x" , "y" , "z")
      val testTsp = new BSONTimestamp
      val testDate = new Date
      val testBin = new Binary(Array[Byte]('x' , 'y' , 'z' , 5 , 4 , 3 , 2 , 1))
      val testUUID = UUID.randomUUID
      val testRE = "^test.*regex.*xyz$".r
      val inDoc = MongoDBObject("_id" -> oid ,
                                "null" -> null ,
                                "max" -> new MaxKey ,
                                "min" -> new MinKey ,
                                "booleanTrue" -> true ,
                                "booleanFalse" -> false ,
                                "int1" -> 1 ,
                                "int1500" -> 1500 ,
                                "int3753" -> 3753 ,
                                "tsp" -> testTsp ,
                                "date" -> testDate ,
                                "long5" -> 5L ,
                                "long3254525" -> 3254525L ,
                                "float324_582" -> 324.582F ,
                                "double245_6289" -> 245.6289 ,
                                "oid" -> testOid ,
                                /*
                                // Symbol Wonky in java driver
                                "symbol" -> new Symbol("foobar"),
                                // code wonky in java driver
                                "code" -> new Code("var x = 12345;"),
                                // Code w/ scope wonky in java driver
                                "code_scoped" -> new CodeWScope("return x * 500;", testDoc),
                                */
                                "str" -> "foobarbaz" ,
                                "ref" -> new DBRef(mongoInt.underlying, "books" , testRefOid) ,
                                "object" -> testDoc ,
                                "array" -> testArr ,
                                "list" -> testList , // same as array technically
                                "binary" -> testBin ,
                                "uuid" -> testUUID ,
                                "regex" -> testRE
                               )

      def readDoc(coll: MongoCollection) = {
        coll.drop()
        coll += inDoc
        coll.findOne(MongoDBObject("_id" -> oid)) match {
          case None =>
            throw new IllegalArgumentException("No matching object in database.")
          case Some(doc: DBObject) =>
            doc must haveEntry("_id" -> oid )
            doc.get("null") must beNull
            doc.getAs[MaxKey]("max") must beSome[MaxKey] // todo - Proper scala side matchers on MinKey/MaxKey
            doc.getAs[MinKey]("min") must beSome[MinKey]
            doc must haveEntry("booleanTrue" -> true )
            doc must haveEntry("booleanFalse" -> false )
            doc must haveEntry("int1" -> 1 )
            doc must haveEntry("int1500" -> 1500 )
            doc must haveEntry("int3753" -> 3753 )
            doc must haveEntry("tsp" -> testTsp ) 
            doc must haveEntry("date" -> testDate )
            doc must haveEntry("long5" -> 5L )
            doc must haveEntry("long3254525" -> 3254525L )
            doc must haveEntry("float324_582" -> 324.5820007324219 ) // how mongo actually ends up storing the previous float
            doc must haveEntry("double245_6289" -> 245.6289 )
            doc must haveEntry("oid" -> testOid )
            doc must haveEntry("str" -> "foobarbaz" )
            //doc.getAs[DBRef]("ref") must beSome( new DBRef(mongoInt.underlying, "books", testRefOid) )
            doc must haveEntry("object.abc" -> testDoc.get("abc") )
            doc.getAs[Pattern]("regex").get.pattern() must_==( testRE.pattern.pattern() )
        }
      }
      "Normal DBObjects decode properly" in {
        readDoc(mongoTest("readNormal"))
      }
      "Lazy DBObjects decode properly" in {
        readDoc(mongoTest("readLazy"))
      }
    }

    "Iterate their keysets correctly" in {
      import scalaj.collection.Imports._
      val coll = mongoInt.lazyCollection("books")
      for (doc <- coll; k <- doc.keySet) {
        k must not beNull
        val v = doc.get(k)
        v must not beNull
      }
      coll.size must beGreaterThan(0)
    }
  }
}
