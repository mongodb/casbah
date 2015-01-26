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

package com.mongodb.casbah.test.query

import com.mongodb.casbah.commons.test.CasbahMutableSpecification
import com.mongodb.casbah.query.Imports._


@SuppressWarnings(Array("deprecation"))
class BarewordOperatorsSpec extends CasbahMutableSpecification {

  "Casbah's DSL $set Operator" should {
    "Accept one or many pairs of values" in {
      "A single pair" in {
        val set = $set("foo" -> "bar")
        set must haveEntry("$set.foo" -> "bar")
      }
      "Multiple pairs" in {
        val set = $set("foo" -> "bar", "x" -> 5.2, "y" -> 9, "a" ->("b", "c", "d", "e"))
        set must haveEntry("$set.foo" -> "bar")
        set must haveEntry("$set.x" -> 5.2)
        set must haveEntry("$set.y" -> 9)
        set must haveEntry("$set.a" ->("b", "c", "d", "e"))
      }
    }
  }

  "Casbah's DSL $setOnInsert Operator" should {
    "Accept one or many pairs of values" in {
      "A single pair" in {
        val set = $setOnInsert("foo" -> "bar")
        set must haveEntry("$setOnInsert.foo" -> "bar")
      }
      "Multiple pairs" in {
        val set = $setOnInsert("foo" -> "bar", "x" -> 5.2, "y" -> 9, "a" ->("b", "c", "d", "e"))
        set must haveEntry("$setOnInsert.foo" -> "bar")
        set must haveEntry("$setOnInsert.x" -> 5.2)
        set must haveEntry("$setOnInsert.y" -> 9)
        set must haveEntry("$setOnInsert.a" ->("b", "c", "d", "e"))
      }
    }
  }

  "Casbah's DSL $unset Operator" should {
    "Accept one or many values" in {
      "A single item" in {
        val unset = $unset("foo")
        unset must haveEntry("$unset.foo" -> 1)
      }
      "Multiple items" in {
        val unset = $unset("foo", "bar", "x", "y")
        unset must haveEntry("$unset.foo" -> 1)
        unset must haveEntry("$unset.bar" -> 1)
        unset must haveEntry("$unset.x" -> 1)
        unset must haveEntry("$unset.y" -> 1)
      }
    }
  }

  "Casbah's DSL $inc Operator" should {
    "Accept one or many sets of values" in {
      "A single set" in {
        val inc = $inc("foo" -> 5.0)
        inc must haveEntry("$inc.foo" -> 5.0)
      }
      "Multiple sets" in {
        val inc = $inc("foo" -> 5.0, "bar" -> -1.2)
        inc must haveEntry("$inc.foo" -> 5.0)
        inc must haveEntry("$inc.bar" -> -1.2)
      }
    }
  }

  "Casbah's DSL $max Operator" should {
    "Accept one or many sets of values" in {
      "A single set" in {
        val max = $max("foo" -> 5.0)
        max must haveEntry("$max.foo" -> 5.0)
      }
      "Multiple sets" in {
        val max = $max("foo" -> 5.0, "bar" -> -1.2)
        max must haveEntry("$max.foo" -> 5.0)
        max must haveEntry("$max.bar" -> -1.2)
      }
    }
  }

  "Casbah's DSL $or Operator" should {
    "Accept multiple values" in {
      val or = $or("foo" -> "bar", "x" -> "y")
      or must haveListEntry("$or", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("x" -> "y")))
    }
    "Accept a mix" in {
      val or = $or {
        "foo" -> "bar" :: ("foo" $gt 5 $lt 10)
      }
      or must haveListEntry("$or", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("foo" -> MongoDBObject("$gt" -> 5, "$lt" -> 10))))
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val or = $or("foo" $lt 5 $gt 1, "x" $gte 10 $lte 152)
        or must haveListEntry("$or", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
      "As a cons (::  constructed) cell" in {
        val or = $or(("foo" $lt 5 $gt 1) :: ("x" $gte 10 $lte 152))
        or must haveListEntry("$or", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
    }
  }

  "Casbah's DSL $and Operator" should {
    "Accept multiple values" in {
      val and = $and("foo" -> "bar", "x" -> "y")
      and must haveListEntry("$and", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("x" -> "y")))
    }
    "Accept a mix" in {
      val and = $and {
        "foo" -> "bar" :: ("foo" $gt 5 $lt 10)
      }
      and must haveListEntry("$and", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("foo" -> MongoDBObject("$gt" -> 5, "$lt" -> 10))))
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val and = $and("foo" $lt 5 $gt 1, "x" $gte 10 $lte 152)
        and must haveListEntry("$and", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
      "As a cons (::  constructed) cell" in {
        val and = $and(("foo" $lt 5 $gt 1) :: ("x" $gte 10 $lte 152))
        and must haveListEntry("$and", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
    }
  }

  "Casbah's DSL $rename Operator" should {
    "Accept one or many sets of renames" in {
      "A single set" in {
        val rename = $rename("foo" -> "bar")
        rename must haveEntry("$rename.foo" -> "bar")
      }
      "Multiple sets" in {
        val rename = $rename("foo" -> "bar", "x" -> "y")
        rename must haveEntry("$rename.foo" -> "bar")
        rename must haveEntry("$rename.x" -> "y")
      }
    }
  }

  "Casbah's $text operator" should {
    "Accept just a searchTerm" in {
      val textSearch = $text("hello")
      textSearch must beEqualTo(
        MongoDBObject("$text" -> MongoDBObject("$search" -> "hello"))
      )
    }
    "Chain searchTerm and language" in {
      val textSearch = $text("hola") $language "spanish"
        textSearch must beEqualTo(
        MongoDBObject("$text" -> MongoDBObject("$search" -> "hola", "$language" -> "spanish"))
      )
    }
  }

  "Casbah's DSL Array operators" should {
    "$push" in {
      "Accept a single value" in {
        val push = $push("foo" -> "bar")
        push must haveEntry("$push.foo" -> "bar")
      }
      "Accept multiple values" in {
        val push = $push("foo" -> "bar", "x" -> 5.2)
        push must haveEntry("$push.foo" -> "bar")
        push must haveEntry("$push.x" -> 5.2)
      }
      "Accept $each" in {
        val push = $push("foo") $each("x", "y", "foo", "bar", "baz")
        push must haveListEntry("$push.foo.$each", Seq("x", "y", "foo", "bar", "baz"))
      }
    }
    "$pushAll" in {
      "Accept a single value list" in {
        val push = $pushAll("foo" ->("bar", "baz", "x", "y"))
        push must haveListEntry("$pushAll.foo", List("bar", "baz", "x", "y"))
      }

      "Handle iterable types" in {
        val tuple = $pushAll("seq" ->(4, 5, 6))
        val seq = $pushAll("seq" -> Seq(4, 5, 6))
        val list = $pushAll("seq" -> List(4, 5, 6))
        val array = $pushAll("seq" -> Array(4, 5, 6))

        tuple must beEqualTo(seq)
        seq must beEqualTo(list)
        list must beEqualTo(array)
      }

      "Accept multiple value lists" in {
        val push = $pushAll("foo" ->("bar", "baz", "x", "y"), "n" ->(5, 10, 12, 238))
        push must haveListEntry("$pushAll.foo", List("bar", "baz", "x", "y"))
        push must haveListEntry("$pushAll.n", List(5, 10, 12, 238))
      }

      "Handle multiple iterable types" in {
        val tuple = $pushAll("1" ->(4, 5, 6), "2" ->(7, 8, 9))
        val seq = $pushAll("1" -> Seq(4, 5, 6), "2" -> Seq(7, 8, 9))
        val list = $pushAll("1" -> List(4, 5, 6), "2" -> List(7, 8, 9))
        val array = $pushAll("1" -> Array(4, 5, 6), "2" -> Array(7, 8, 9))

        tuple must beEqualTo(seq)
        seq must beEqualTo(list)
        list must beEqualTo(array)
      }
    }

    "$addToSet" in {
      "Accept a single value" in {
        val addToSet = $addToSet("foo" -> "bar")
        addToSet must haveEntry("$addToSet.foo" -> "bar")
      }
      "Accept multiple values" in {
        val addToSet = $addToSet("foo" -> "bar", "x" -> 5.2)
        addToSet must haveEntry("$addToSet.foo" -> "bar")
        addToSet must haveEntry("$addToSet.x" -> 5.2)
      }
      "Function with the $each operator for multi-value updates" in {
        val addToSet = $addToSet("foo") $each("x", "y", "foo", "bar", "baz")
        addToSet must haveListEntry("$addToSet.foo.$each", Seq("x", "y", "foo", "bar", "baz"))
      }
    }

    "$bit" in {
      "Accept a single value" in {
        "For 'and'" in {
          val bit = $bit("foo") and 5
          bit must haveEntry("$bit.foo.and" -> 5)
        }
        "For 'or'" in {
          val bit = $bit("foo") or 5
          bit must haveEntry("$bit.foo.or" -> 5)
        }
      }
    }

    "$where" should {
      "be a top level operator" in {
        val where = $where("function () { this.foo }")
        where must haveField("$where")
      }
    }

    "$pop" in {
      "Accept a single value" in {
        val pop = $pop("foo" -> 1)
        pop must haveEntry("$pop.foo" -> 1)
      }
      "Accept multiple values" in {
        val pop = $pop("foo" -> 1, "x" -> -1)
        pop must haveEntry("$pop.foo" -> 1)
        pop must haveEntry("$pop.x" -> -1)
      }
    }
    "$pull" in {
      "Accept a single value" in {
        val pull = $pull("foo" -> "bar")
        pull must haveEntry("$pull.foo" -> "bar")
      }
      "Allow Value Test Operators" in {
        "A simple $gt test" in {
          // Syntax oddity due to compiler confusion
          val pull = $pull {
            "foo" $gt 5
          }
          pull must haveEntry("$pull.foo.$gt" -> 5)
        }
        "A deeper chain test" in {
          // Syntax oddity due to compiler confusion
          val pull = $pull {
            "foo" $gt 5 $lte 52
          }
          pull must haveEntry("$pull.foo.$gt" -> 5)
          pull must haveEntry("$pull.foo.$lte" -> 52)
        }
      }
      "Accept multiple values" in {
        val pull = $pull("foo" -> "bar", "x" -> 5.2)
        pull must haveEntry("$pull.foo" -> "bar")
        pull must haveEntry("$pull.x" -> 5.2)
      }
    }
    "$pullAll" in {
      "Accept a single value list" in {
        val pull = $pullAll("foo" ->("bar", "baz", "x", "y"))
        pull must haveEntry("$pullAll.foo" -> Seq("bar", "baz", "x", "y"))
      }

      "Handle iterable types" in {
        val tuple = $pullAll("seq" ->(4, 5, 6))
        val seq = $pullAll("seq" -> Seq(4, 5, 6))
        val list = $pullAll("seq" -> List(4, 5, 6))
        val array = $pullAll("seq" -> Array(4, 5, 6))

        tuple must beEqualTo(seq)
        seq must beEqualTo(list)
        list must beEqualTo(array)
      }

      "Accept multiple value lists" in {
        val pull = $pullAll("foo" ->("bar", "baz", "x", "y"), "n" ->(5, 10, 12, 238))
        pull must haveEntry("$pullAll.foo" -> Seq("bar", "baz", "x", "y"))
        pull must haveEntry("$pullAll.n" -> Seq(5, 10, 12, 238))
      }
      "Handle multiple iterable types" in {
        val tuple = $pullAll("1" ->(4, 5, 6), "2" ->(7, 8, 9))
        val seq = $pullAll("1" -> Seq(4, 5, 6), "2" -> Seq(7, 8, 9))
        val list = $pullAll("1" -> List(4, 5, 6), "2" -> List(7, 8, 9))
        val array = $pullAll("1" -> Array(4, 5, 6), "2" -> Array(7, 8, 9))

        tuple must beEqualTo(seq)
        seq must beEqualTo(list)
        list must beEqualTo(array)
      }
    }

  }

  "Casbah's DSL $nor operator" should {
    "Accept multiple values" in {
      val nor = $nor("foo" -> "bar", "x" -> "y")
      nor must haveListEntry("$nor", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("x" -> "y")))
    }
    "Accept a mix" in {
      val nor = $nor {
        "foo" -> "bar" :: ("foo" $gt 5 $lt 10)
      }
      nor must haveListEntry("$nor", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("foo" -> MongoDBObject("$gt" -> 5, "$lt" -> 10))))
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val nor = $nor("foo" $lt 5 $gt 1, "x" $gte 10 $lte 152)
        nor must haveListEntry("$nor", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
      "As a cons (::  constructed) cell" in {
        val nor = $nor(("foo" $lt 5 $gt 1) :: ("x" $gte 10 $lte 152))
        nor must haveListEntry("$nor", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
    }
  }
}

