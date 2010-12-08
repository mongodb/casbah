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
package test 

import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.commons.Logging

import java.security.MessageDigest
import java.io._

import org.specs._
import org.specs.specification.PendingUntilFixed

class GridFSSpec extends Specification with PendingUntilFixed with Logging {
  val logo_md5 = "479977b85391a88bbc1da1e9f5175239"
  val digest = MessageDigest.getInstance("MD5")


  "Casbah's GridFS Implementations" should {
    shareVariables()
    implicit val mongo = MongoConnection()("casbah_test")
    mongo.dropDatabase()
    val logo = new FileInputStream("casbah-gridfs/src/test/resources/powered_by_mongo.png")
    val gridfs = GridFS(mongo)

    "Correctly save a file to GridFS" in {
      gridfs must notBeNull 
      logo must notBeNull

      gridfs(logo) { fh =>
        fh.filename = "powered_by_mongo.png"
        fh.contentType = "image/png"
      }
    
    }

    "Find the file in GridFS later" in {
      gridfs.findOne("powered_by_mongo.png") must beSome[GridFSDBFile]
      gridfs.findOne("powered_by_mongo.png") foreach { file =>
        file must notBeNull
        file must haveSuperClass[GridFSDBFile]
        file.md5 must beEqualTo(logo_md5)
        log.debug("MD5: %s", file.md5)
      }
    }

    "Correctly catch the non-existence of a file and fail gracefully" in {
      gridfs.findOne("powered_by_mongoFOOBAR235254252.png") must beNone
    }

    "Return a wrapped MongoCursor if you call files,  as reported by Gregg Carrier" in {
      val files = gridfs.files
      files must notBeNull
      files must haveClass[MongoCursor]
    }

    "Be properly iterable" in {
      var x = false
      for (f <- gridfs) x = true
      x must beTrue
    }

  }

}


// vim: set ts=2 sw=2 sts=2 et:
