/**
 * Copyright (c) 2010 - 2012 10gen, Inc. <http://10gen.com>
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

package com.mongodb.casbah.test.query

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.test.CasbahMutableSpecification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.data.Sized

// TODO - Operational/Integration testing with this code
@RunWith(classOf[JUnitRunner])
class AggregationFrameworkSpec extends CasbahMutableSpecification {
  implicit object SizePipeline extends Sized[AggregationPipeline] {
    def size(t: AggregationPipeline) = t.pipelineSize
  }

  "Casbah's Aggregation DSL" should {
    "Work with $limit" in {
      val limit = | $limit 5
      limit must not beNull
    }
    "Work with $skip" in {
      val skip = | $skip 5
      skip must not beNull
    }
    "Work with $sort" in {
      val sort = | $sort ( "foo" -> 1, "bar" -> -1 )
      sort must not beNull
    }
     
    "Work with $unwind" in {
      val unwind = | $unwind "$foo"
      //unwind(0) must haveEntry("$unwind" -> "$foo")
      unwind must not beNull
    } 

    "Fail to accept a non $-ed target field" in {
      (| $unwind "foo" ) must throwA[IllegalArgumentException]
    }

    "Work with $match and Casbah Queries" in {
      val _match = | $match { "score" $gt 50 $lte 90 }
      //_match must haveEntry("$match.score.$gt" -> 50) and haveEntry("$match.score.$lte" -> 90)
      _match must not beNull
    }
    "Work with $match and Casbah Queries plus additional chains" in {
      val _match = | $match { ("score" $gt 50 $lte 90) ++ ("type" $in ("exam", "quiz")) }
      //_match must haveEntries("$match.score.$gt" -> 50, "$match.score.$lte" -> 90, "$match.type.$in" -> List("exam", "quiz"))
      _match must not beNull
    }
    "Allow full chaining of operations" in {
      val x = | $group { ("lastAuthor" $last "$author") ++ ("firstAuthor" $first "$author")  ++ ("_id" -> "$foo") } 
      val y = x $unwind("$tags") $sort ( "foo" -> 1, "bar" -> -1 ) $skip 5 $limit 10
      y must beAnInstanceOf[MongoDBList]
      y must have size(5)
      y must not beNull
    }
  }
  
  "Aggregation's Group Operator" should {
    "Work with field operators" in {
      "Allow $first" >> {
        val _group = | $group { ("firstAuthor" $first "$author") ++ ("_id" -> "$isbn") }
        _group must not beNull
      }
      "Allow $last" >> {
        val _group = | $group { ("lastAuthor" $last "$author") ++ ("_id" -> "$isbn") }
        _group must not beNull
      }
      "Require $-signs in inner operator fields" >> {
        lazy val _group = | $group { ("firstAuthor" $first "author") ++ ("_id" -> "$isbn") }
        _group must throwA[IllegalArgumentException]
      }
      "Require _id to be present" >> {
        lazy val _group = | $group { "firstAuthor" $first "author" }
        _group must throwA[IllegalArgumentException]
        
      }
       
//       "Chain with another op" >> {
//         val _test = | $group "lastAuthor" $last "$author" $unwind "$tags"
//         // TODO - Proper test
//         _test must not beNull
//       }

    }

     /*
      "Work with multiple operators" in {
      val _group = | $group { "lastAuthor" $last "$author" :: "firstAuthor" $first "$author" }
     }
     */


    /*"Require _id" in {
      null must beNull
    }
*/
  }
}


// vim: set ts=2 sw=2 sts=2 et:
