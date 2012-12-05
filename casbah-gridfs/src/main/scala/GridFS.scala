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

package com.mongodb.casbah
package gridfs

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.conversions.scala._

import com.mongodb.DBObject
import com.mongodb.gridfs.{ GridFS => MongoGridFS, GridFSDBFile => MongoGridFSDBFile, GridFSFile => MongoGridFSFile, GridFSInputFile => MongoGridFSInputFile }

import java.io._

import scala.reflect._
import scalaj.collection.Imports._

import org.scala_tools.time.Imports._

// todo - look into potential naming conflicts...

/**
 * Companion object for GridFS.
 * Entry point for creation of GridFS Instances.
 *
 * @since 1.0
 */
object GridFS extends Logging {

  def apply(db: MongoDB) = {
    log.info("Creating a new GridFS Entry against DB '%s', using default bucket ('%s')", db.name, MongoGridFS.DEFAULT_BUCKET)
    new GridFS(new MongoGridFS(db.underlying))
  }

  def apply(db: MongoDB, bucket: String) = {
    log.info("Creating a new GridFS Entry against DB '%s', using specific bucket ('%s')", db.name, bucket)
    new GridFS(new MongoGridFS(db.underlying, bucket))
  }

}

class GridFS protected[gridfs] (val underlying: MongoGridFS) extends Iterable[GridFSDBFile] with Logging {
  log.info("Instantiated a new GridFS instance against '%s'", underlying)

  type FileOp = GridFSFile => Unit
  type FileWriteOp = GridFSInputFile => Unit
  type FileReadOp = GridFSDBFile => Unit

  implicit val db = underlying.getDB().asScala

  def iterator = new Iterator[GridFSDBFile] {
    val fileSet = files
    def count() = fileSet.count
    override def length() = fileSet.length
    def numGetMores() = fileSet.numGetMores
    def numSeen() = fileSet.numSeen

    def curr() = new GridFSDBFile(fileSet.next().asInstanceOf[MongoGridFSDBFile])
    def explain() = fileSet.explain

    def next() = new GridFSDBFile(fileSet.next.asInstanceOf[MongoGridFSDBFile])
    def hasNext: Boolean = fileSet.hasNext
  }

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
  /**
   * Create a new GridFS File from a scala.io.Source
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @returns The ID of the created File (Option[AnyRef])
   */
  def apply(data: scala.io.Source)(op: FileWriteOp) = withNewFile(data)(op)

  /**
   * Create a new GridFS File from a Byte Array
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @returns The ID of the created File (Option[AnyRef])
   */
  def apply(data: Array[Byte])(op: FileWriteOp) = withNewFile(data)(op)

  /**
   * Create a new GridFS File from a java.io.File
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @returns The ID of the created File (Option[AnyRef])
   */
  def apply(f: File)(op: FileWriteOp) = withNewFile(f)(op)

  /**
   * Create a new GridFS File from a java.io.InputStream
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @returns The ID of the created File (Option[AnyRef])
   */
  def apply(in: InputStream)(op: FileWriteOp) = withNewFile(in)(op)

  /**
   * Create a new GridFS File from a java.io.InputStream and a specific filename
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @returns The ID of the created File (Option[AnyRef])
   */
  def apply(in: InputStream, filename: String)(op: FileWriteOp) = withNewFile(in, filename)(op)

  /**
   * createFile
   *
   * Creates a new file in GridFS
   *
   * TODO - Should the curried versions give the option to not automatically save?
   */
  def createFile(data: scala.io.Source): GridFSInputFile = throw new UnsupportedOperationException("Currently no support for scala.io.Source")
  def withNewFile(data: scala.io.Source)(op: FileWriteOp) = throw new UnsupportedOperationException("Currently no support for scala.io.Source")

  def createFile(data: Array[Byte]): GridFSInputFile = underlying.createFile(data)
  /**
   * Loan pattern style file creation.
   * @returns The ID of the created File (Option[AnyRef])
   */
  def withNewFile(data: Array[Byte])(op: FileWriteOp) = loan(createFile(data)) { fh =>
    op(fh)
    fh.save()
    fh.validate()
    Option(fh.id)
  }

  def createFile(f: File): GridFSInputFile = underlying.createFile(f)
  /**
   * Loan pattern style file creation.
   * @returns The ID of the created File (Option[AnyRef])
   */
  def withNewFile(f: File)(op: FileWriteOp) = loan(createFile(f)) { fh =>
    op(fh)
    fh.save()
    fh.validate()
    Option(fh.id)
  }

  def createFile(in: InputStream): GridFSInputFile = underlying.createFile(in)

  /**
   * Loan pattern style file creation.
   * @returns The ID of the created File (Option[AnyRef])
   */
  def withNewFile(in: InputStream)(op: FileWriteOp) = loan(createFile(in)) { fh =>
    op(fh)
    fh.save()
    fh.validate()
    Option(fh.id)
  }

  def createFile(in: InputStream, filename: String): GridFSInputFile = underlying.createFile(in, filename)
  /**
   * Loan pattern style file creation.
   * @returns The ID of the created File (Option[AnyRef])
   */
  def withNewFile(in: InputStream, filename: String)(op: FileWriteOp) = loan(createFile(in, filename)) { fh =>
    op(fh)
    fh.save()
    fh.validate()
    Option(fh.id)
  }



  /** Find by query - returns a list */
  def find[A <% DBObject](query: A) = underlying.find(query).asScala
  /** Find by query - returns a single item */
  def find(id: ObjectId): GridFSDBFile = underlying.find(id)
  /** Find by query - returns a list */
  def find(filename: String) = underlying.find(filename).asScala

  def findOne[A <% DBObject](query: A): Option[GridFSDBFile] = {
    underlying.findOne(query) match {
      case null => None
      case x => Some(x)
    }
  }
  def findOne(id: ObjectId): Option[GridFSDBFile] = {
    underlying.findOne(id) match {
      case null => None
      case x => Some(x)
    }
  }
  def findOne(filename: String): Option[GridFSDBFile] = {
    underlying.findOne(filename) match {
      case null => None
      case x => Some(x)
    }
  }

  def bucketName = underlying.getBucketName

  /**
   * Returns a cursor for this filestore
   * of all of the files...
   */
  def files = { new MongoCursor(underlying.getFileList) }
  def files[A <% DBObject](query: A) = { new MongoCursor(underlying.getFileList(query)) }

  def remove[A <% DBObject](query: A) = underlying.remove(query)
  def remove(id: ObjectId) = underlying.remove(id)
  def remove(filename: String) = underlying.remove(filename)
}

@BeanInfo
abstract class GenericGridFSFile(override val underlying: MongoGridFSFile) extends MongoDBObject with Logging {
  type DateType

  def convertDate(in: AnyRef): DateType

  override def iterator = underlying.keySet.asScala.map { k =>
    k -> underlying.get(k)
  }.toMap.iterator.asInstanceOf[Iterator[(String, AnyRef)]]

  def save() { underlying.save() }

  /**
   * validate the object.
   * Throws an exception if it fails
   * @throws MongoException An error describing the validation failure
   */
  def validate() { underlying.validate() }

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

@BeanInfo
class GridFSFile(_underlying: MongoGridFSFile) extends GenericGridFSFile(_underlying) {
  type DateType = java.util.Date
  def convertDate(in: AnyRef) = in match {
    case d: java.util.Date => d
    case j: DateTime => new java.util.Date(j.getMillis)
  }
}



@BeanInfo
abstract class GenericGridFSDBFile protected[gridfs] (override val underlying: MongoGridFSDBFile) extends GenericGridFSFile(underlying) {

  def inputStream = underlying.getInputStream

  def source = scala.io.Source.fromInputStream(inputStream)

  def writeTo(file: java.io.File) = underlying.writeTo(file)

  def writeTo(out: java.io.OutputStream) = underlying.writeTo(out)

  def writeTo(filename: String) = underlying.writeTo(filename)

  override def toString = "{ GridFSDBFile(id=%s, filename=%s, contentType=%s) }".
    format(id, filename, contentType)
}

@BeanInfo
class GridFSDBFile(_underlying: MongoGridFSDBFile) extends GenericGridFSDBFile(_underlying) {
  type DateType = java.util.Date
  def convertDate(in: AnyRef): DateType = in match {
    case d: java.util.Date => d
    case j: DateTime => new java.util.Date(j.getMillis)
  }
}


@BeanInfo
abstract class GenericGridFSInputFile protected[gridfs] (override val underlying: MongoGridFSInputFile) extends GenericGridFSFile(underlying) {
  def filename_=(name: String) = underlying.setFilename(name)
  def contentType_=(cT: String) = underlying.setContentType(cT)
}


@BeanInfo
class GridFSInputFile(_underlying: MongoGridFSInputFile) extends GenericGridFSInputFile(_underlying) {
  type DateType = java.util.Date
  def convertDate(in: AnyRef): DateType = in match {
    case d: java.util.Date => d
    case j: DateTime => new java.util.Date(j.getMillis)
  }
}


