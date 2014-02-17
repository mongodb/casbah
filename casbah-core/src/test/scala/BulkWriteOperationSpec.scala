/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
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
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/mongodb/casbah
 *
 */

package com.mongodb.casbah.test.core

import scala.collection.mutable

import com.mongodb.{BulkWriteException, BulkWriteUpsert}

import com.mongodb.casbah.BulkWriteOperation
import com.mongodb.casbah.Imports._


@SuppressWarnings(Array("deprecation"))
class BulkWriteOperationSpec extends CasbahDBTestSpecification {

  "The BulkWriteOperation" should {
    "insert a document" in {
      collection.drop()
      val builder = collection.initializeOrderedBulkOperation
      builder.insert(MongoDBObject("_id" -> 1))

      val result = builder.execute()
      result.insertedCount must beEqualTo(1)
      result.upserts.size must beEqualTo(0)
      collection.findOne() must beSome(MongoDBObject("_id" -> 1))
    }

    "remove a document" in {
      collection.drop()
      collection += MongoDBObject("x" -> true)
      collection += MongoDBObject("x" -> true)

      val builder = collection.initializeOrderedBulkOperation
      builder.find(MongoDBObject("x" -> true)).removeOne()

      val result = builder.execute()
      result.removedCount must beEqualTo(1)
      result.upserts.size must beEqualTo(0)
      collection.count() must beEqualTo(1)
    }

    "remove documents" in {
      collection.drop()
      collection += MongoDBObject("x" -> true)
      collection += MongoDBObject("x" -> true)
      collection += MongoDBObject("x" -> false)

      val builder = collection.initializeOrderedBulkOperation
      builder.find(MongoDBObject("x" -> true)).remove()

      val result = builder.execute()
      result.removedCount must beEqualTo(2)
      result.upserts.size must beEqualTo(0)
      collection.count() must beEqualTo(1)
    }

    "update a document" in {
      collection.drop()
      collection += MongoDBObject("x" -> true)
      collection += MongoDBObject("x" -> true)

      val builder = collection.initializeOrderedBulkOperation
      builder.find(MongoDBObject("x" -> true)).updateOne($set("y" -> 1))

      val result = builder.execute()
      result.matchedCount must beEqualTo(1)
      result.upserts.size must beEqualTo(0)
      collection.find(MongoDBObject("y" -> 1)).count() must beEqualTo(1)
    }

    "upsert a document with custom _id" in {
      serverIsAtLeastVersion(2, 5) must beTrue.orSkip("Needs server >= 2.5")
      collection.drop()

      val query = MongoDBObject("_id" -> 101)
      val builder = collection.initializeOrderedBulkOperation
      builder.find(query).upsert().updateOne($set("x" -> 2))

      val result = builder.execute()
      result.upserts.size must beEqualTo(1)
      result.upserts must beEqualTo(mutable.Buffer(new BulkWriteUpsert(0, 101)))
      collection.findOne() must beSome(MongoDBObject("_id" -> 101, "x" -> 2))
    }

    "upsert a document" in {
      collection.drop()

      val id = new ObjectId()
      val query = MongoDBObject("_id" -> id)
      val builder = collection.initializeOrderedBulkOperation
      builder.find(query).upsert().updateOne($set("x" -> 2))

      val result = builder.execute()
      result.upserts.size must beEqualTo(1)
      result.upserts must beEqualTo(mutable.Buffer(new BulkWriteUpsert(0, id)))
      collection.findOne() must beSome(MongoDBObject("_id" -> id, "x" -> 2))
    }

    "replace a document" in {
      collection.drop()
      collection += MongoDBObject("_id" -> 101)

      val builder = collection.initializeOrderedBulkOperation
      builder.find(MongoDBObject("_id" -> 101)).upsert().replaceOne(MongoDBObject("_id" -> 101, "x" -> 2))

      val result = builder.execute()
      result.matchedCount must beEqualTo(1)
      result.upserts.size must beEqualTo(0)
      collection.count() must beEqualTo(1)
      collection.findOne() must beSome(MongoDBObject("_id" -> 101, "x" -> 2))
    }

    "handle multi-length runs of ordered insert, update, replace, and remove" in {
      collection.drop()
      collection.insert(testInserts: _*)

      val builder = collection.initializeOrderedBulkOperation
      addWritesToBuilder(builder)
      builder.execute()

      collection.findOne(MongoDBObject("_id" -> 1)) must beSome(MongoDBObject("_id" -> 1, "x" -> 2))
      collection.findOne(MongoDBObject("_id" -> 2)) must beSome(MongoDBObject("_id" -> 2, "x" -> 3))
      collection.findOne(MongoDBObject("_id" -> 3)) must beNone
      collection.findOne(MongoDBObject("_id" -> 4)) must beNone
      collection.findOne(MongoDBObject("_id" -> 5)) must beSome(MongoDBObject("_id" -> 5, "x" -> 4))
      collection.findOne(MongoDBObject("_id" -> 6)) must beSome(MongoDBObject("_id" -> 6, "x" -> 5))
      collection.findOne(MongoDBObject("_id" -> 7)) must beSome(MongoDBObject("_id" -> 7))
      collection.findOne(MongoDBObject("_id" -> 8)) must beSome(MongoDBObject("_id" -> 8))
    }

    "handle multi-length runs of unacknowledged insert, update, replace, and remove" in {
      collection.drop()
      collection.insert(testInserts: _*)

      val builder = collection.initializeOrderedBulkOperation
      addWritesToBuilder(builder)

      val result = builder.execute(WriteConcern.Unacknowledged)
      result.isAcknowledged must beFalse
      collection.insert(MongoDBObject("_id" -> 9))

      collection.findOne(MongoDBObject("_id" -> 1)) must beSome(MongoDBObject("_id" -> 1, "x" -> 2))
      collection.findOne(MongoDBObject("_id" -> 2)) must beSome(MongoDBObject("_id" -> 2, "x" -> 3))
      collection.findOne(MongoDBObject("_id" -> 3)) must beNone
      collection.findOne(MongoDBObject("_id" -> 4)) must beNone
      collection.findOne(MongoDBObject("_id" -> 5)) must beSome(MongoDBObject("_id" -> 5, "x" -> 4))
      collection.findOne(MongoDBObject("_id" -> 6)) must beSome(MongoDBObject("_id" -> 6, "x" -> 5))
      collection.findOne(MongoDBObject("_id" -> 7)) must beSome(MongoDBObject("_id" -> 7))
      collection.findOne(MongoDBObject("_id" -> 8)) must beSome(MongoDBObject("_id" -> 8))
    }

    "error details should have correct index on ordered write failure" in {
      collection.drop()

      val builder = collection.initializeOrderedBulkOperation
      builder.insert(MongoDBObject("_id" -> 1))
      builder.find(MongoDBObject("_id" -> 1)).updateOne($set("x" -> 3))
      builder.insert(MongoDBObject("_id" -> 1))

      try {
        builder.execute()
      } catch {
        case ex: BulkWriteException =>
          ex.getWriteErrors.size() must beEqualTo(1)
          ex.getWriteErrors.get(0).getIndex must beEqualTo(2)
          ex.getWriteErrors.get(0).getCode must beEqualTo(11000)
      }
      true
    }

    "handle multi-length runs of unordered insert, update, replace, and remove" in {

      collection.drop()
      collection.insert(testInserts: _*)

      val builder = collection.initializeUnorderedBulkOperation
      addWritesToBuilder(builder)

      val result = builder.execute()
      result.insertedCount must beEqualTo(2)
      result.matchedCount must beEqualTo(4)
      result.removedCount must beEqualTo(2)
      result.modifiedCount must beEqualTo(4)
      result.upserts.size must beEqualTo(0)

      collection.findOne(MongoDBObject("_id" -> 1)) must beSome(MongoDBObject("_id" -> 1, "x" -> 2))
      collection.findOne(MongoDBObject("_id" -> 2)) must beSome(MongoDBObject("_id" -> 2, "x" -> 3))
      collection.findOne(MongoDBObject("_id" -> 3)) must beNone
      collection.findOne(MongoDBObject("_id" -> 4)) must beNone
      collection.findOne(MongoDBObject("_id" -> 5)) must beSome(MongoDBObject("_id" -> 5, "x" -> 4))
      collection.findOne(MongoDBObject("_id" -> 6)) must beSome(MongoDBObject("_id" -> 6, "x" -> 5))
      collection.findOne(MongoDBObject("_id" -> 7)) must beSome(MongoDBObject("_id" -> 7))
      collection.findOne(MongoDBObject("_id" -> 8)) must beSome(MongoDBObject("_id" -> 8))
    }

    "error details should have correct index on unordered write failure" in {
      collection.drop()
      collection.insert(testInserts: _*)

      val builder = collection.initializeUnorderedBulkOperation
      builder.insert(MongoDBObject("_id" -> 1))
      builder.find(MongoDBObject("_id" -> 2)).updateOne(new BasicDBObject("$set", new BasicDBObject("x", 3)))
      builder.insert(MongoDBObject("_id" -> 3))

      try {
        builder.execute()
      } catch {
        case ex: BulkWriteException =>
          ex.getWriteErrors.size must beEqualTo(2)
          ex.getWriteErrors.get(0).getIndex must beEqualTo(0)
          ex.getWriteErrors.get(0).getCode must beEqualTo(11000)
          ex.getWriteErrors.get(1).getIndex must beEqualTo(2)
          ex.getWriteErrors.get(1).getCode must beEqualTo(11000)
      }
      success
    }

    "test write concern exceptions" in {
      val mongoClient = MongoClient(List(new ServerAddress()))
      isReplicaSet must beTrue.orSkip("Testing writeConcern on ReplicaSet")
      try {
        val builder: BulkWriteOperation = collection.initializeUnorderedBulkOperation
        builder.insert(MongoDBObject())
        builder.execute(new WriteConcern(5, 1, false, false))
        failure("Execute should have failed")
      } catch {
        case e: BulkWriteException => (e.getWriteConcernError must not).beNull
        case _: Throwable => failure("Unexpected exception")
      }
      success
    }
  }

  def testInserts = {
    List(MongoDBObject("_id" -> 1),
         MongoDBObject("_id" -> 2),
         MongoDBObject("_id" -> 3),
         MongoDBObject("_id" -> 4),
         MongoDBObject("_id" -> 5),
         MongoDBObject("_id" -> 6)
    )
  }

  def addWritesToBuilder(builder: BulkWriteOperation) {
    builder.find(MongoDBObject("_id" -> 1)).updateOne($set("x" -> 2))
    builder.find(MongoDBObject("_id" -> 2)).updateOne($set("x" -> 3))
    builder.find(MongoDBObject("_id" -> 3)).removeOne()
    builder.find(MongoDBObject("_id" -> 4)).removeOne()
    builder.find(MongoDBObject("_id" -> 5)).replaceOne(MongoDBObject("_id" -> 5, "x" -> 4))
    builder.find(MongoDBObject("_id" -> 6)).replaceOne(MongoDBObject("_id" -> 6, "x" -> 5))
    builder.insert(MongoDBObject("_id" -> 7))
    builder.insert(MongoDBObject("_id" -> 8))
  }
}
