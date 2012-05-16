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

package com.mongodb.casbah.test.gridfs

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.commons.Logging


import org.scala_tools.time.Imports._

import java.security.MessageDigest
import java.io._

class GridFSSpec extends com.mongodb.casbah.commons.test.CasbahMutableSpecification {
  implicit val mongo = MongoConnection()("casbah_test")
  mongo.dropDatabase()
  val gridfs = GridFS(mongo)
  def logo_fh = new FileInputStream("casbah-gridfs/src/test/resources/powered_by_mongo.png")

  def logo_bytes = {
    val data = new Array[Byte](logo_fh.available())
    logo_fh.read(data)
    data
  }

  def logo = new ByteArrayInputStream(logo_bytes)

  lazy val digest = MessageDigest.getInstance("MD5")
  digest.update(logo_bytes)
  lazy val logo_md5 = digest.digest().map("%02X" format _).mkString.toLowerCase()

  def findItem(id: ObjectId, filename: Option[String] = None, contentType: Option[String] = None) = {
    gridfs.findOne(id) must beSome[GridFSDBFile]
    var md5 = ""
    var fn: Option[String] = None
    var ct: Option[String] = None
    gridfs.findOne(id) foreach { file =>
      md5 = file.md5
      fn = file.filename
      ct = file.contentType
    }

    md5 must beEqualTo(logo_md5)
    fn must beEqualTo(filename)
    ct must beEqualTo(contentType)
  }

  "Casbah's GridFS Implementations" should {
    implicit val mongo = MongoConnection()("casbah_test")
    mongo.dropDatabase()
    val gridfs = GridFS(mongo)

    "Find the file in GridFS later" in {
      val id = gridfs(logo_bytes) { fh =>
        fh.filename = "powered_by_mongo_find.png"
        fh.contentType = "image/png"
      }

      gridfs.findOne("powered_by_mongo_find.png") must beSome[GridFSDBFile]
      var md5 = ""
      var uploadDate: java.util.Date = null
      gridfs.findOne("powered_by_mongo_find.png") foreach { file =>
        md5 = file.md5
        uploadDate = file.uploadDate
        log.debug("MD5: %s", file.md5)
      }
      md5 must beEqualTo(logo_md5)
      require(uploadDate != null)
      uploadDate must beAnInstanceOf[java.util.Date]
    }



    "Correctly catch the non-existence of a file and fail gracefully" in {
      gridfs.findOne("powered_by_mongoFOOBAR235254252.png") must beNone
    }

    "Return a wrapped MongoCursor if you call files,  as reported by Gregg Carrier" in {
      val files = gridfs.files
      files must beAnInstanceOf[MongoCursor]
    }

    "Be properly iterable" in {
      val id = gridfs(logo) { fh =>
        fh.filename = "powered_by_mongo_iter.png"
        fh.contentType = "image/png"
      }
      var x = false
      for (f <- gridfs) x = true
      x must beTrue
    }

  }
  "Casbah's Joda GridFS Implementations" should {
    implicit val mongo = MongoConnection()("casbah_test")
    mongo.dropDatabase()
    val gridfs = JodaGridFS(mongo)

    "Find the file in GridFS later" in {
      val id = gridfs(logo_bytes) { fh =>
        fh.filename = "powered_by_mongo_find.png"
        fh.contentType = "image/png"
      }

      gridfs.findOne("powered_by_mongo_find.png") must beSome[JodaGridFSDBFile]
      var md5 = ""
      var uploadDate: DateTime = null
      gridfs.findOne("powered_by_mongo_find.png") foreach { file =>
        md5 = file.md5
        uploadDate = file.uploadDate
        log.debug("MD5: %s", file.md5)
      }
      md5 must beEqualTo(logo_md5)
      require(uploadDate != null)
      uploadDate must beAnInstanceOf[DateTime]
    }



    "Correctly catch the non-existence of a file and fail gracefully" in {
      gridfs.findOne("powered_by_mongoFOOBAR235254252.png") must beNone
    }

    "Return a wrapped MongoCursor if you call files,  as reported by Gregg Carrier" in {
      val files = gridfs.files
      files must beAnInstanceOf[MongoCursor]
    }

    "Be properly iterable" in {
      val id = gridfs(logo) { fh =>
        fh.filename = "powered_by_mongo_iter.png"
        fh.contentType = "image/png"
      }
      var x = false
      for (f <- gridfs) x = true
      x must beTrue
    }

  }
  "Return the created file's ID from the loan pattern methods." should {

    "Using a InputStream" in {
      val id = gridfs(logo) { fh =>
        fh.filename = "powered_by_mongo_inputstream.png"
        fh.contentType = "image/png"
      }
      id must beSome[AnyRef]
      id.get must beAnInstanceOf[ObjectId]
      findItem(id.get.asInstanceOf[ObjectId], Some("powered_by_mongo_inputstream.png"), Some("image/png"))
    }

    "Using a Byte Array" in {
      val id = gridfs(logo_bytes) { fh =>
        fh.filename = "powered_by_mongo_bytes.png"
        fh.contentType = "image/png"
      }
      id must beSome
      id.get must beAnInstanceOf[ObjectId]
      findItem(id.get.asInstanceOf[ObjectId], Some("powered_by_mongo_bytes.png"), Some("image/png"))
    }
  }
  "Allow filename and contentType to be nullable, returning 'None' appropriately." in {
      "Filename may be null" in {
        val id = gridfs(logo) { fh =>
          fh.contentType = "image/png"
        }
        id.get must beAnInstanceOf[ObjectId]
        findItem(id.get.asInstanceOf[ObjectId], None, Some("image/png"))
      }
    "content Type may be null" in {
        val id = gridfs(logo) { fh =>
          fh.filename = "null_content_type.png"
        }
        id.get must beAnInstanceOf[ObjectId]
        findItem(id.get.asInstanceOf[ObjectId], Some("null_content_type.png"), None)
      }
    "both may be null" in {
        val id = gridfs(logo) { fh => }
        id.get must beAnInstanceOf[ObjectId]
        findItem(id.get.asInstanceOf[ObjectId])
      }
  }

}

// vim: set ts=2 sw=2 sts=2 et:
