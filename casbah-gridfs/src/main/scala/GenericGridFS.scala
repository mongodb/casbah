/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
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
package com.mongodb.casbah
package gridfs


import scala.reflect._
import scala.collection.JavaConverters._

import com.github.nscala_time.time.Imports._
import com.mongodb.gridfs.{GridFS => MongoGridFS, GridFSDBFile => MongoGridFSDBFile, GridFSFile => MongoGridFSFile, GridFSInputFile => MongoGridFSInputFile}

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging


abstract class GenericGridFS protected[gridfs] extends Logging {
  log.info("Instantiated a new GridFS instance against '%s'", underlying)

  val underlying: MongoGridFS

  implicit val db = underlying.getDB().asScala
  implicit val filesCollection: MongoGenericTypedCollection[GridFSDBFileSafeJoda] =
    db(underlying.getBucketName + ".files").setObjectClass(classOf[GridFSDBFileSafeJoda])

  /**
   * loan
   *
   * Basic implementation of the Loan pattern -
   * the idea is to pass a Unit returning function
   * and a Mongo file handle, and work on it within
   * a code block.
   *
   */
  protected[gridfs] def loan[T <: GenericGridFSFile](file: T)(op: T => Option[AnyRef]) = op(file)

  /** Find by query - returns a list */
  def find[A <% DBObject](query: A) = underlying.find(query).asScala

  /** Find by query - returns a single item */
  def find(id: ObjectId) = underlying.find(id)

  /** Find by query - returns a list */
  def find(filename: String) = underlying.find(filename).asScala

  def bucketName = underlying.getBucketName

  /**
   * Returns a cursor for this filestore
   * of all of the files...
   */
  def files = {
    new MongoCursor(underlying.getFileList)
  }

  def files[A <% DBObject](query: A) = {
    new MongoCursor(underlying.getFileList(query))
  }

  def remove[A <% DBObject](query: A) = underlying.remove(query)

  def remove(id: ObjectId) = underlying.remove(id)

  def remove(filename: String) = underlying.remove(filename)

}

@BeanInfo
abstract class GenericGridFSFile(override val underlying: MongoGridFSFile) extends MongoDBObject with Logging {
  type DateType

  def convertDate(in: AnyRef): DateType

  override def iterator = underlying.keySet.asScala.map {
    k =>
      k -> underlying.get(k)
  }.toMap.iterator.asInstanceOf[Iterator[(String, AnyRef)]]

  def save() {
    underlying.save()
  }

  /**
   * validate the object.
   * Throws an exception if it fails
   * @throws MongoException An error describing the validation failure
   */
  def validate() {
    underlying.validate()
  }

  def numChunks: Int = underlying.numChunks

  def id = underlying.getId

  def filename = Option(underlying.getFilename)

  // todo - does mongo support mime magic? Should we pull some in here?
  def contentType = Option(underlying.getContentType)

  def length = underlying.getLength

  def chunkSize = underlying.getChunkSize

  def uploadDate: DateType = convertDate(underlying.get("uploadDate"))

  def aliases = underlying.getAliases.asScala

  def metaData: DBObject = underlying.getMetaData

  def metaData_=[A <% DBObject](meta: A) = underlying.setMetaData(meta)

  def md5: String = underlying.getMD5


  override def toString = "{ GridFSFile(id=%s, filename=%s, contentType=%s) }".
    format(id, filename, contentType)
}

class GridFSDBFileSafeJoda extends MongoGridFSDBFile {

  override def setGridFS( fs: MongoGridFS ){
    _fs = fs
  }
  override def put(key: String, v: AnyRef) = {
    val _v = v match {
      case j: DateTime => j.toDate
      case l: LocalDateTime => l.toDateTime.toDate
      case default => default
    }
    super.put(key, _v)
  }
}

@BeanInfo
abstract class GenericGridFSDBFile protected[gridfs](override val underlying: MongoGridFSDBFile) extends GenericGridFSFile(underlying) {

  def inputStream = underlying.getInputStream

  def source = scala.io.Source.fromInputStream(inputStream)

  def writeTo(file: java.io.File) = underlying.writeTo(file)

  def writeTo(out: java.io.OutputStream) = underlying.writeTo(out)

  def writeTo(filename: String) = underlying.writeTo(filename)

  override def toString = "{ GridFSDBFile(id=%s, filename=%s, contentType=%s) }".
    format(id, filename, contentType)

  override def put(key: String, v: AnyRef) = {
    val _v = v match {
      case j: DateTime => j.toDate
      case l: LocalDateTime => l.toDateTime.toDate
      case default => default
    }
    super.put(key, _v)
  }
}


@BeanInfo
abstract class GenericGridFSInputFile protected[gridfs](override val underlying: MongoGridFSInputFile) extends GenericGridFSFile(underlying) {
  def filename_=(name: String) = underlying.setFilename(name)

  def contentType_=(cT: String) = underlying.setContentType(cT)

  override def put(key: String, v: AnyRef) = {
    val _v = v match {
      case j: DateTime => j.toDate
      case l: LocalDateTime => l.toDateTime.toDate
      case default => default
    }
    super.put(key, _v)
  }
}