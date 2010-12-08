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
package query

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.conversions.scala._

import org.scala_tools.time.Imports._

import org.specs._
import org.specs.specification.PendingUntilFixed

@SuppressWarnings(Array("deprecation"))
class BarewordOperatorsSpec extends Specification with PendingUntilFixed with Logging {
  "Casbah's DSL $set Operator" should {
    "Accept one or many pairs of values" in {
      "A single pair" in {
        val set = $set ("foo" -> "bar")
        set must notBeNull
        set.toString must notBeNull
        set must haveSuperClass[DBObject]
        set must beEqualTo(MongoDBObject("$set" -> MongoDBObject("foo" -> "bar")))
      }
      "Multiple pairs" in {
        val set = $set ("foo" -> "bar", "x" -> 5.2, "y" -> 9, "a" -> ("b", "c", "d", "e"))
        set must notBeNull
        set.toString must notBeNull
        set must haveSuperClass[DBObject]
        set must beEqualTo(MongoDBObject("$set" -> MongoDBObject("foo" -> "bar", "x" -> 5.2, "y" -> 9, "a" -> ("b", "c", "d", "e"))))
      }
    }
  }

  "Casbah's DSL $unset Operator" should {
    "Accept one or many values" in {
      "A single item" in {
        val unset = $unset ("foo")
        unset must notBeNull
        unset.toString must notBeNull
        unset must haveSuperClass[DBObject]
        unset must beEqualTo(MongoDBObject("$unset" -> MongoDBObject("foo" -> 1)))
      }
      "Multiple items" in {
        val unset = $unset ("foo", "bar", "x", "y")
        unset must notBeNull
        unset.toString must notBeNull
        unset must haveSuperClass[DBObject]
        unset must beEqualTo(MongoDBObject("$unset" -> MongoDBObject("foo" -> 1, "bar" -> 1, "x" -> 1, "y" -> 1)))
      }
    }
  }


  "Casbah's DSL $inc Operator" should {
    "Accept one or many sets of values" in {
      "A single set" in {
        val inc = $inc ("foo" -> 5.0)
        inc must notBeNull
        inc.toString must notBeNull
        inc must haveSuperClass[DBObject]
        inc must beEqualTo(MongoDBObject("$inc" -> MongoDBObject("foo" -> 5.0)))
      }
      "Multiple sets" in {
        val inc = $inc ("foo" -> 5.0, "bar" -> -1.2)
        inc must notBeNull
        inc.toString must notBeNull
        inc must haveSuperClass[DBObject]
        inc must beEqualTo(MongoDBObject("$inc" -> MongoDBObject("foo" -> 5.0, "bar" -> -1.2)))
      }
    }
  }
  
  "Casbah's DSL $or Operator" should {
    "Accept multiple values" in {
      val or = $or ("foo" -> "bar", "x" -> "y")
      or must notBeNull
      or.toString must notBeNull
      or must haveSuperClass[DBObject]
      /*or must beEqualTo(MongoDBObject("$or" -> MongoDBList(MongoDBObject("foo" -> "bar", "x" -> "y"))))*/
    }
    /*"Work with nested operators" in {
      val or = $or { "foo" $lt 5 $gt 1 ++ "x" $gte 10 $lte 152 }
      or must notBeNull
      or.toString must notBeNull
      or must haveSuperClass[DBObject]
      [>or must beEqualTo(MongoDBObject("$or" -> MongoDBList(MongoDBObject("foo" -> "bar", "x" -> "y"))))<]
    }*/

  }

  "Casbah's DSL $rename Operator" should {
    "Accept one or many sets of renames" in {
      "A single set" in {
        val rename = $rename ("foo" -> "bar")
        rename must notBeNull
        rename.toString must notBeNull
        rename must haveSuperClass[DBObject]
        rename must beEqualTo(MongoDBObject("$rename" -> MongoDBObject("foo" -> "bar")))
      }
      "Multiple sets" in {
        val rename = $rename ("foo" -> "bar", "x" -> "y")
        rename must notBeNull
        rename.toString must notBeNull
        rename must haveSuperClass[DBObject]
        rename must beEqualTo(MongoDBObject("$rename" -> MongoDBObject("foo" -> "bar", "x" -> "y")))
      }
    }
  }

  "Casbah's DSL Array operators" should {
    "$push" in {
      "Accept a single value" in {
        val push = $push ("foo" -> "bar")
        push must notBeNull
        push.toString must notBeNull
        push must haveSuperClass[DBObject]
        push must beEqualTo(MongoDBObject("$push" -> MongoDBObject("foo" -> "bar")))
      }
      "Accept multiple values" in {
        val push = $push ("foo" -> "bar", "x" -> 5.2)
        push must notBeNull
        push.toString must notBeNull
        push must haveSuperClass[DBObject]
        push must beEqualTo(MongoDBObject("$push" -> MongoDBObject("foo" -> "bar", "x" -> 5.2)))
      }
    }
    "$pushAll" in {
      "Accept a single value list" in {
        val push = $pushAll ("foo" -> ("bar", "baz", "x", "y"))
        push must notBeNull
        push.toString must notBeNull
        push must haveSuperClass[DBObject]
        val tl = MongoDBObject("$pushAll" -> MongoDBObject("foo" -> MongoDBList("bar", "baz", "x", "y")))
        tl.getAs[DBObject]("$pushAll").get.getAs[BasicDBList]("foo").get must haveTheSameElementsAs(push.getAs[DBObject]("$pushAll").get.getAs[List[Any]]("foo").get)
      }
      "Not allow a non-list value" in {
        ($pushAll ("foo" -> "bar")) must throwA[IllegalArgumentException]
      }
      "Accept multiple value lists" in {
        val push = $pushAll ("foo" -> ("bar", "baz", "x", "y"), "n" -> (5, 10, 12, 238))
        push must notBeNull
        push.toString must notBeNull
        push must haveSuperClass[DBObject]
        val tl = MongoDBObject("$pushAll" -> MongoDBObject("foo" -> MongoDBList("bar", "baz", "x", "y"), "n" -> MongoDBList(5, 10, 12, 238)))
        tl.getAs[DBObject]("$pushAll").get.getAs[BasicDBList]("foo").get must haveTheSameElementsAs(push.getAs[DBObject]("$pushAll").get.getAs[List[Any]]("foo").get)
        tl.getAs[DBObject]("$pushAll").get.getAs[BasicDBList]("n").get must haveTheSameElementsAs(push.getAs[DBObject]("$pushAll").get.getAs[List[Any]]("n").get)
      }
    }
    "$addToSet" in {
      "Accept a single value" in {
        val addToSet = $addToSet ("foo" -> "bar")
        addToSet must notBeNull
        addToSet.toString must notBeNull
        addToSet must haveSuperClass[DBObject]
        addToSet must beEqualTo(MongoDBObject("$addToSet" -> MongoDBObject("foo" -> "bar")))
      }
      "Accept multiple values" in {
        val addToSet = $addToSet ("foo" -> "bar", "x" -> 5.2)
        addToSet must notBeNull
        addToSet.toString must notBeNull
        addToSet must haveSuperClass[DBObject]
        addToSet must beEqualTo(MongoDBObject("$addToSet" -> MongoDBObject("foo" -> "bar", "x" -> 5.2)))
      }
      "Function with the $each operator for multi-value updates" in {
        val addToSet = $addToSet ("foo") $each ("x", "y", "foo", "bar", "baz")
        addToSet must notBeNull
        addToSet.toString must notBeNull
        addToSet must haveSuperClass[DBObject]
        addToSet must beEqualTo(MongoDBObject("$addToSet" ->
          MongoDBObject("foo" -> MongoDBObject("$each" -> MongoDBList("x", "y", "foo", "bar", "baz")))))

      }
    }
    "$pop" in {
      "Accept a single value" in {
        val pop = $pop ("foo" -> 1)
        pop must notBeNull
        pop.toString must notBeNull
        pop must haveSuperClass[DBObject]
        pop must beEqualTo(MongoDBObject("$pop" -> MongoDBObject("foo" -> 1))) 
      }
      "Accept multiple values" in {
        val pop = $pop ("foo" -> 1, "x" -> -1)
        pop must notBeNull
        pop.toString must notBeNull
        pop must haveSuperClass[DBObject]
        pop must beEqualTo(MongoDBObject("$pop" -> MongoDBObject("foo" -> 1, "x" -> -1)))
      }
    }
    "$pull" in {
      "Accept a single value" in {
        val pull = $pull ("foo" -> "bar")
        pull must notBeNull
        pull.toString must notBeNull
        pull must haveSuperClass[DBObject]
        pull must beEqualTo(MongoDBObject("$pull" -> MongoDBObject("foo" -> "bar")))
      }
      "Allow Value Test Operators" in {
        "A simple $gt test" in {
          // Syntax oddity due to compiler confusion
          val pull = $pull { "foo" $gt 5 }
          pull must notBeNull
          pull.toString must notBeNull
          pull must haveSuperClass[DBObject]
          pull must beEqualTo(MongoDBObject("$pull" -> MongoDBObject("foo" -> MongoDBObject("$gt" -> 5))))
        }
        "A deeper chain test" in {
          // Syntax oddity due to compiler confusion
          val pull = $pull { "foo" $gt 5 $lte 52 }
          pull must notBeNull
          pull.toString must notBeNull
          pull must haveSuperClass[DBObject]
         pull must beEqualTo(MongoDBObject("$pull" -> MongoDBObject("foo" -> MongoDBObject("$gt" -> 5, "$lte" -> 52))))
        } 
      }
      "Accept multiple values" in {
        val pull = $pull ("foo" -> "bar", "x" -> 5.2) 
        pull must notBeNull
        pull.toString must notBeNull
        pull must haveSuperClass[DBObject]
        pull must beEqualTo(MongoDBObject("$pull" -> MongoDBObject("foo" -> "bar", "x" -> 5.2)))
      }
    }
    "$pullAll" in {
      "Accept a single value list" in {
        val pull = $pullAll ("foo" -> ("bar", "baz", "x", "y"))
        pull must notBeNull
        pull.toString must notBeNull
        pull must haveSuperClass[DBObject]
        val tl = MongoDBObject("$pullAll" -> MongoDBObject("foo" -> MongoDBList("bar", "baz", "x", "y")))
        tl.getAs[DBObject]("$pullAll").get.getAs[BasicDBList]("foo").get must haveTheSameElementsAs(pull.getAs[DBObject]("$pullAll").get.getAs[List[Any]]("foo").get)
      }
      "Not allow a non-list value" in {
        ($pullAll ("foo" -> "bar")) must throwA[IllegalArgumentException]
      }
      "Accept multiple value lists" in {
        val pull = $pullAll ("foo" -> ("bar", "baz", "x", "y"), "n" -> (5, 10, 12, 238))
        pull must notBeNull
        pull.toString must notBeNull
        pull must haveSuperClass[DBObject]
        val tl = MongoDBObject("$pullAll" -> MongoDBObject("foo" -> MongoDBList("bar", "baz", "x", "y"), "n" -> MongoDBList(5, 10, 12, 238)))
        tl.getAs[DBObject]("$pullAll").get.getAs[BasicDBList]("foo").get must haveTheSameElementsAs(pull.getAs[DBObject]("$pullAll").get.getAs[List[Any]]("foo").get)
        tl.getAs[DBObject]("$pullAll").get.getAs[BasicDBList]("n").get must haveTheSameElementsAs(pull.getAs[DBObject]("$pullAll").get.getAs[List[Any]]("n").get)
      }
    }


  } 

  "Casbah's DSL $nor operator" should {
    "Function as expected" in {
      val nor = $nor { "foo" $gte 15 $lt 35.2 $ne 16 }
      nor must notBeNull
      nor must haveSuperClass[DBObject]
      nor must beEqualTo(MongoDBObject("$nor" -> MongoDBList(MongoDBObject("foo" -> MongoDBObject("$gte" -> 15, "$lt" -> 35.2, "$ne" -> 16)))))
    } 
    "Work with multiples" in {
      val nor = $nor {  "foo" $gte 15 $lt 35  + ("x" -> "y")  }
      nor must notBeNull
      nor must haveSuperClass[DBObject]
      nor must beEqualTo(MongoDBObject("$nor" -> MongoDBList(MongoDBObject("foo" -> MongoDBObject("$gte" -> 15, "$lt" -> 35), ("x" -> "y")))))
    }
  }
}

// vim: set ts=2 sw=2 sts=2 et:
