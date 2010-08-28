/**
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
 *     http://github.com/novus/casbah
 * 
 */

package com.novus.casbah
package conversions

import com.novus.casbah.commons.Imports._


import org.specs._
import org.specs.specification.PendingUntilFixed

class MongoDBObjectSpec extends Specification with PendingUntilFixed {
  "MongoDBObject expand operations" should {
    shareVariables()

    val x: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> "C"))
    val y: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> MongoDBObject("C" -> 5)))
    val z: DBObject = MongoDBObject("A" -> MongoDBObject("B" -> MongoDBObject("C" -> List(5, 4, 3, 2, 1))))
    
    "Expanding a simple layering should work" in {
      val b = x.expand[String]("A.B")
      b must beSome("C")

      "While overexpanding should probably fail" in {
        lazy val bFail = x.expand[String]("A.B.C")
        bFail must throwA[ClassCastException]
      }
    }

    "Expanding a further layering should work" in {
      // TODO - This is way broken as far as casting to int
      val c = y.expand[Int]("A.B.C")
      c must beSome(5)
      "While overexpanding should probably fail" in {
        lazy val cFail = y.expand[String]("A.B.C.D")
        cFail must throwA[ClassCastException]
      }
    }

    "And you can go further and even get a list" in {
      // TODO - This is way broken as far as casting to int
      val c = z.expand[List[_]]("A.B.C")
      c must beSome(List(5, 4, 3, 2, 1))
    }
  }
}


// vim: set ts=2 sw=2 sts=2 et:
