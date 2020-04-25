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

package com.mongodb.casbah.test.gridfs

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.gridfs.{ GridFSDBFile => MongoGridFSDBFile }

import com.github.nscala_time.time.Imports._
import org.specs2.specification.BeforeEach

class JodaGridFSSpec extends GridFSSpecification with BeforeEach {

  def before {
    DeregisterJodaLocalDateTimeConversionHelpers()
    RegisterJodaTimeConversionHelpers()
  }

  override val databaseName = TEST_DB + "-JodaGridFS"

  val gridfs = JodaGridFS(database, "jodaGridFs")

  def findItem(id: ObjectId, filename: Option[String] = None, contentType: Option[String] = None) = {
    gridfs.findOne(id) must beSome[JodaGridFSDBFile]
    var md5 = ""
    var fn: Option[String] = None
    var ct: Option[String] = None
    gridfs.findOne(id) foreach {
      file =>
        md5 = file.md5
        fn = file.filename
        ct = file.contentType
    }

    md5 must beEqualTo(logo_md5)
    fn must beEqualTo(filename)
    ct must beEqualTo(contentType)
  }

  "Casbah's Joda GridFS Implementations with Registered JodaTime Helpers" should {

    "Find the file in GridFS later" in {
      val id = gridfs(logo_bytes) {
        fh =>
          fh.put("uploadDate", new DateTime())
          fh.filename = "powered_by_mongo_find.png"
          fh.contentType = "image/png"
      }
      gridfs.findOne("powered_by_mongo_find.png") must beSome[JodaGridFSDBFile]
      var md5 = ""
      var uploadDate: DateTime = null
      gridfs.findOne("powered_by_mongo_find.png") foreach {
        file =>
          md5 = file.md5
          uploadDate = file.uploadDate
      }
      md5 must beEqualTo(logo_md5)
      require(uploadDate != null)
      uploadDate must beAnInstanceOf[DateTime]

      gridfs.findOne(id.get.asInstanceOf[ObjectId]) foreach {
        file =>
          md5 = file.md5
          uploadDate = file.uploadDate
      }
      md5 must beEqualTo(logo_md5)
      require(uploadDate != null)
      uploadDate must beAnInstanceOf[DateTime]

    }

    "Find files in GridFS shouldn't break either" in {
      gridfs(logo_bytes) {
        fh =>
          fh.put("uploadDate", new DateTime())
          fh.filename = "powered_by_mongo_find.png"
          fh.contentType = "image/png"
      }
      gridfs.find("powered_by_mongo_find.png") foreach {
        file => file must beAnInstanceOf[MongoGridFSDBFile]
      }
      success
    }

    "Correctly catch the non-existence of a file and fail gracefully" in {
      gridfs.findOne("powered_by_mongoFOOBAR235254252.png") must beNone
    }

    "Return a wrapped MongoCursor if you call files,  as reported by Gregg Carrier" in {
      val files = gridfs.files
      files must beAnInstanceOf[MongoCursor]
    }

    "Be properly iterable" in {

      val id = gridfs(logo) {
        fh =>
          fh.filename = "powered_by_mongo_iter.png"
          fh.contentType = "image/png"
      }
      var x = false
      for (f <- gridfs) x = true
      x must beTrue
    }

    "read back as expected" in {
      gridfs("hello world".getBytes) {
        fh =>
          fh.filename = "hello_world.txt"
          fh.contentType = "text/plain"
      }

      val file = gridfs.findOne("hello_world.txt")
      file.get.source.mkString must beEqualTo("hello world")

      // Ensure the iterator also works
      gridfs.iterator.filter(f => f.filename.contains("hello_world.txt")).foreach(f =>
        f.source.mkString must beEqualTo("hello world"))
      success
    }
  }

  "Casbah's Joda GridFS Implementations with no registered JodaTime helpers" should {

    "Still find the file in GridFS later and contain DateTime objects" in {
      val id = gridfs(logo_bytes) {
        fh =>
          fh.put("uploadDate", new DateTime())
          fh.filename = "powered_by_mongo_find_datetime.png"
          fh.contentType = "image/png"
      }

      gridfs.findOne("powered_by_mongo_find_datetime.png") must beSome[JodaGridFSDBFile]
      var md5 = ""
      var uploadDate: DateTime = null
      gridfs.findOne("powered_by_mongo_find_datetime.png") foreach {
        file =>
          md5 = file.md5
          uploadDate = file.uploadDate
      }
      md5 must beEqualTo(logo_md5)
      require(uploadDate != null)
      uploadDate must beAnInstanceOf[DateTime]

      gridfs.findOne(id.get.asInstanceOf[ObjectId]) foreach {
        file =>
          md5 = file.md5
          uploadDate = file.uploadDate
      }
      md5 must beEqualTo(logo_md5)
      require(uploadDate != null)
      uploadDate must beAnInstanceOf[DateTime]
    }

    "Handle LocalTime" in {
      gridfs(logo_bytes) {
        fh =>
          fh.put("uploadDate", new LocalDateTime())
          fh.filename = "powered_by_mongo_find_local.png"
          fh.contentType = "image/png"
      }

      var md5 = ""
      var uploadDate: DateTime = null
      gridfs.findOne("powered_by_mongo_find_local.png") foreach {
        file =>
          md5 = file.md5
          uploadDate = file.uploadDate
      }
      uploadDate must beAnInstanceOf[DateTime]
    }

    "Handle Date" in {
      gridfs(logo_bytes) {
        fh =>
          fh.put("uploadDate", new java.util.Date())
          fh.filename = "powered_by_mongo_find_date.png"
          fh.contentType = "image/png"
      }

      var md5 = ""
      var uploadDate: DateTime = null
      gridfs.findOne("powered_by_mongo_find_date.png") foreach {
        file =>
          md5 = file.md5
          uploadDate = file.uploadDate
      }
      uploadDate must beAnInstanceOf[DateTime]
    }
  }

}

