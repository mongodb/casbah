/**
 * Copyright (c) 2010, 2011 10gen, Inc. <http://10gen.com>
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
package com.mongodb.casbah.test.query.light

import com.mongodb.casbah.query.light._
import com.mongodb.casbah.commons.test.CasbahSpecification

// TODO - Operational/Integration testing with this code
@SuppressWarnings(Array("deprecation"))
class LightBarewordOperatorsSpec extends CasbahSpecification {
  "Casbah's DSL set Operator" should {
    "Accept one or many pairs of values" in {
      "A single pair" in {
        val setVal = set("foo" -> "bar")
        setVal must haveEntry("$set.foo" -> "bar")
      }
      "Multiple pairs" in {
        val setVal = set("foo" -> "bar", "x" -> 5.2, "y" -> 9, "a" -> ("b", "c", "d", "e"))
        setVal must haveEntry("$set.foo" -> "bar")
        setVal must haveEntry("$set.x" -> 5.2)
        setVal must haveEntry("$set.y" -> 9)
        setVal must haveEntry("$set.a" -> ("b", "c", "d", "e"))
      }
    }
  }

  "Casbah's DSL unset Operator" should {
    "Accept one or many values" in {
      "A single item" in {
        val _unset = unset("foo")
        _unset must haveEntry("$unset.foo" -> 1)
      }
      "Multiple items" in {
        val _unset = unset("foo", "bar", "x", "y")
        _unset must haveEntry("$unset.foo" -> 1)
        _unset must haveEntry("$unset.bar" -> 1)
        _unset must haveEntry("$unset.x" -> 1)
        _unset must haveEntry("$unset.y" -> 1)
      }
    }
  }

  "Casbah's DSL inc Operator" should {
    "Accept one or many sets of values" in {
      "A single set" in {
        val _inc = inc("foo" -> 5.0)
        _inc must haveEntry("$inc.foo" -> 5.0)
      }
      "Multiple sets" in {
        val _inc = inc("foo" -> 5.0, "bar" -> -1.2)
        _inc must haveEntry("$inc.foo" -> 5.0)
        _inc must haveEntry("$inc.bar" -> -1.2)
      }
    }
  }

  "Casbah's DSL or Operator" >> {
    "Accept multiple values" in {
      val _or = or ( "foo" -> "bar", "x" -> "y" )
      _or must haveListEntry("$or", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("x" -> "y")))
    }
    "Accept a mix" in {
      val _or = or { "foo" -> "bar" :: ("foo" gt 5 lt 10) }
      _or must haveListEntry("$or", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("foo" -> MongoDBObject("$gt" -> 5, "$lt" -> 10))))
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val _or = or( "foo" lt 5 gt 1, "x" gte 10 lte 152 )
        _or must haveListEntry("$or", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
      "As a cons (::  constructed) cell" in {
        val _or = or( ("foo" lt 5 gt 1) :: ("x" gte 10 lte 152) )
        _or must haveListEntry("$or", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
    }
  }

  "Casbah's DSL and Operatand" >> {
    "Accept multiple values" in {
      val _and = and ( "foo" -> "bar", "x" -> "y" )
      _and must haveListEntry("$and", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("x" -> "y")))
    }
    "Accept a mix" in {
      val _and = and { "foo" -> "bar" :: ("foo" gt 5 lt 10) }
      _and must haveListEntry("$and", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("foo" -> MongoDBObject("$gt" -> 5, "$lt" -> 10))))
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val _and = and( "foo" lt 5 gt 1, "x" gte 10 lte 152 )
        _and must haveListEntry("$and", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
      "As a cons (::  constructed) cell" in {
        val _and = and( ("foo" lt 5 gt 1) :: ("x" gte 10 lte 152) )
        _and must haveListEntry("$and", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
    }
  }

  "Casbah's DSL rename Operator" should {
    "Accept one or many sets of renames" in {
      "A single set" in {
        val _rename = rename("foo" -> "bar")
        _rename must haveEntry("$rename.foo" -> "bar")
      }
      "Multiple sets" in {
        val _rename = rename("foo" -> "bar", "x" -> "y")
        _rename must haveEntry("$rename.foo" -> "bar")
        _rename must haveEntry("$rename.x" -> "y")
      }
    }
  }

  "Casbah's DSL Array operators" should {
    "push" in {
      "Accept a single value" in {
        val _push = push("foo" -> "bar")
        _push must haveEntry("$push.foo" -> "bar")
      }
      "Accept multiple values" in {
        val _push = push("foo" -> "bar", "x" -> 5.2)
        _push must haveEntry("$push.foo" -> "bar")
        _push must haveEntry("$push.x" -> 5.2)
      }
    }
    "pushAll" in {
      "Accept a single value list" in {
        val _push = pushAll("foo" -> ("bar", "baz", "x", "y"))
        _push must haveListEntry("$pushAll.foo", List("bar", "baz", "x", "y"))
      }
      "Not allow a non-list value" in {
        (pushAll("foo" -> "bar")) must throwA[IllegalArgumentException]
      }
      "Accept multiple value lists" in {
        val _push = pushAll("foo" -> ("bar", "baz", "x", "y"), "n" -> (5, 10, 12, 238))
        _push must haveListEntry("$pushAll.foo", List("bar", "baz", "x", "y"))
        _push must haveListEntry("$pushAll.n", List(5, 10, 12, 238))
      }
    }
    "addToSet" in {
      "Accept a single value" in {
        val _addToSet = addToSet("foo" -> "bar")
        _addToSet must haveEntry("$addToSet.foo" -> "bar")
      }
      "Accept multiple values" in {
        val _addToSet = addToSet("foo" -> "bar", "x" -> 5.2)
        _addToSet must haveEntry("$addToSet.foo" -> "bar")
        _addToSet must haveEntry("$addToSet.x" -> 5.2)
      }
      "Function with the each operator for multi-value updates" in {
        val _addToSet = addToSet("foo") each ("x", "y", "foo", "bar", "baz")
        _addToSet must haveListEntry("$addToSet.foo.each", Seq("x", "y", "foo", "bar", "baz"))
      }
    }
    "bit" in {
      "Accept a single value" in {
        "For 'and'" in {
          val _bit = bit("foo") and 5
          _bit must haveEntry("$bit.foo.and" -> 5)
        }
        "For 'or'" in {
          val _bit = bit("foo") or 5
          _bit must haveEntry("$bit.foo.or" -> 5)
        }
      }
    }
    "pop" in {
      "Accept a single value" in {
        val _pop = pop("foo" -> 1)
        _pop must haveEntry("$pop.foo" -> 1)
      }
      "Accept multiple values" in {
        val _pop = pop("foo" -> 1, "x" -> -1)
        _pop must haveEntry("$pop.foo" -> 1)
        _pop must haveEntry("$pop.x" -> -1)
      }
    }
    "pull" in {
      "Accept a single value" in {
        val _pull = pull("foo" -> "bar")
        _pull must haveEntry("$pull.foo" -> "bar")
      }
      "Allow Value Test Operators" in {
        "A simple gt test" in {
          // Syntax oddity due to compiler confusion
          val _pull = pull { "foo" gt 5 }
          _pull must haveEntry("$pull.foo.gt" -> 5)
        }
        "A deeper chain test" in {
          // Syntax oddity due to compiler confusion
          val _pull = pull { "foo" gt 5 lte 52 }
          _pull must haveEntry("$pull.foo.gt" -> 5)
          _pull must haveEntry("$pull.foo.lte" -> 52)
        }
      }
      "Accept multiple values" in {
        val _pull = pull("foo" -> "bar", "x" -> 5.2)
        _pull must haveEntry("$pull.foo" -> "bar")
        _pull must haveEntry("$pull.x" -> 5.2)
      }
    }
    "pullAll" in {
      "Accept a single value list" in {
        val _pull = pullAll("foo" -> ("bar", "baz", "x", "y"))
        _pull must haveEntry("$pullAll.foo" -> Seq("bar", "baz", "x", "y"))
      }
      "Not allow a non-list value" in {
        (pullAll("foo" -> "bar")) must throwA[IllegalArgumentException]
      }
      "Accept multiple value lists" in {
        val _pull = pullAll("foo" -> ("bar", "baz", "x", "y"), "n" -> (5, 10, 12, 238))
        _pull must haveEntry("$pullAll.foo" -> Seq("bar", "baz", "x", "y"))
        _pull must haveEntry("$pullAll.n" -> Seq(5, 10, 12, 238))
      }
    }

  }

  "Casbah's DSL nor operator" >> {
    "Accept multiple values" in {
      val _nor = nor ( "foo" -> "bar", "x" -> "y" )
      _nor must haveListEntry("$nor", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("x" -> "y")))
    }
   "Accept a mix" in {
      val _nor = nor { "foo" -> "bar" :: ("foo" gt 5 lt 10) }
      _nor must haveListEntry("$nor", Seq(MongoDBObject("foo" -> "bar"), MongoDBObject("foo" -> MongoDBObject("$gt" -> 5, "$lt" -> 10))))
    }
    "Work with nested operators" in {
      "As a simple list (comma separated)" in {
        val _nor = nor( "foo" lt 5 gt 1, "x" gte 10 lte 152 )
        _nor must haveListEntry("$nor", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$gte" -> 10, "$lte" -> 152))))
      }
      "As a cons (::  constructed) cell" in {
        val _nor = nor( ("foo" lt 5 gt 1) :: ("x" gte 10 lte 152) )
        _nor must haveListEntry("$nor", Seq(MongoDBObject("foo" -> MongoDBObject("$lt" -> 5, "$gt" -> 1)),
          MongoDBObject("x" -> MongoDBObject("$$gte" -> 10, "$lte" -> 152))))
      }
    }
  }
}

// vim: set ts=2 sw=2 sts=2 et:
