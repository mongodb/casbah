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
package mongodb
package test

import util.Logging
import gridfs._

import java.security.MessageDigest 
import java.io._

import Implicits._
import com.mongodb._
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers
import conversions.scala._

class GridFSSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers with Logging {
  val logoMD5 = "018612de54b1edd5053ff5a2be6b9763"
  val digest = MessageDigest.getInstance("MD5")

  feature("The map/reduce engine works correctly") {
    val conn = new Mongo().asScala
    // New functionality should forcibly unload Joda Helpers where they conflict; so load them explicitly
    RegisterJodaTimeConversionHelpers()
    //DeRegisterJodaTimeConversionHelpers()
    scenario("Error conditions such as a non-existant collection should not blow up but return an error-state result") {
      given("A Mongo object connected to the default [localhost]")
      assert(conn != null)
      implicit val mongo = conn("foo")
      //then("GridFS Can be interacted with as expected.")
      val gridfs = GridFS(mongo) 
      gridfs should not be (null)
      // Loan pattern with file
      when("A file is saved to gridFS")
      //val logo = scala.io.Source.fromFile("src/test/resources/novus-logo.png")
      val logo = new FileInputStream("src/test/resources/novus-logo.png")
      gridfs(logo) { fh =>
        fh.filename = "novus-logo.png"   
        fh.contentType = "image/png"
      }
      and("That file can be found again")
      val file = gridfs.findOne("novus-logo.png")
      assert(file.isInstanceOf[GridFSDBFile] && file != null)
      log.info("File: %s", file)
      and("The file matches up with expected MD5")
      pending
    }
  }
}

// vim: set ts=2 sw=2 sts=2 et:
