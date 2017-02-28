/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
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

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.util.{ MongoNoOperation, MongoOpLog, NoOp }
import org.bson.types.BSONTimestamp
import org.specs2.mock.Mockito

class MongoOpLogSpec extends CasbahDBTestSpecification with Mockito {

  "MongoOpLog" should {
    "support no op types" in {

      val cursor = mock[MongoCursor]
      cursor.hasNext returns true

      val x: DBObject = MongoDBObject(
        "ts" -> new BSONTimestamp(10101, 0),
        "h" -> 0,
        "v" -> 2,
        "op" -> "n",
        "ns" -> "",
        "o" -> Map.empty
      )

      cursor.next() returns x

      val oplog = spy(new MongoOpLog(
        mongoClient = mongoClient,
        replicaSet = false,
        namespace = Some("%s.%s".format(database.name, collection.name))
      ))

      oplog.cursor returns cursor

      oplog.next() must not(throwA[scala.MatchError])

      oplog.next() must beAnInstanceOf[MongoNoOperation]
      val event = oplog.next()
      event.op.typeCode === "n"
      event.op === NoOp

    }

    "iterate over new OpLog entries when .next() is called" in {
      isReplicaSet must beTrue.orSkip("Testing OpLogs requires a ReplicaSet")

      collection.drop()

      val secondsSinceEpoch: Int = (System.currentTimeMillis / 1000).toInt
      val startTime = new BSONTimestamp(secondsSinceEpoch, 0).getTime

      collection.insert(MongoDBObject("_id" -> 1))

      val oplog =
        new MongoOpLog(
          mongoClient = mongoClient,
          namespace = Some("%s.%s".format(database.name, collection.name))
        )

      var latest = false
      while (!latest && oplog.hasNext) {
        latest = oplog.next().timestamp.getTime >= startTime
      }

      val insertDoc = MongoDBObject("_id" -> 2)
      collection.insert(insertDoc)

      val opLogDoc = oplog.next().document
      opLogDoc must beEqualTo(insertDoc)

      oplog.close()
      oplog.hasNext should throwA[IllegalStateException]
    }
  }
}
