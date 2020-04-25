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
package com.mongodb.casbah
package gridfs

import java.io.{ File, InputStream }

import com.mongodb.gridfs.{
  GridFS => MongoGridFS,
  GridFSDBFile => MongoGridFSDBFile,
  GridFSFile => MongoGridFSFile,
  GridFSInputFile => MongoGridFSInputFile
}

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.commons.Logging
import com.github.nscala_time.time.Imports.{ DateTime, LocalDateTime }

/**
 * Companion object for GridFS.
 * Entry point for creation of GridFS Instances.
 *
 * @since 1.0
 */
object GridFS extends Logging {

  def apply(db: MongoDB): GridFS = {
    log.info("Creating a new GridFS Entry against DB '%s', using default bucket ('%s')", db.name, MongoGridFS.DEFAULT_BUCKET)
    new GridFS(new MongoGridFS(db.underlying))
  }

  def apply(db: MongoDB, bucket: String): GridFS = {
    log.info("Creating a new GridFS Entry against DB '%s', using specific bucket ('%s')", db.name, bucket)
    new GridFS(new MongoGridFS(db.underlying, bucket))
  }

}

class GridFS protected[gridfs] (override val underlying: MongoGridFS) extends GenericGridFS with Iterable[GridFSDBFile] {

  type FileWriteOp = GridFSInputFile => Unit

  def iterator: Iterator[GridFSDBFile] = new Iterator[GridFSDBFile] {
    val fileSet: MongoCursor = files

    def count(): Int = fileSet.count

    //override def length: Int = fileSet.length

    def numSeen(): Int = fileSet.numSeen

    def curr: GridFSDBFile = next()

    def explain(): CursorExplanation = fileSet.explain

    @SuppressWarnings(Array("deprecation"))
    def next(): GridFSDBFile = {
      val gridfsfile = fileSet.next().asInstanceOf[GridFSDBFileSafeJoda]
      gridfsfile.setGridFS(underlying)
      new GridFSDBFile(gridfsfile)
    }

    def hasNext: Boolean = fileSet.hasNext
  }

  /**
   * Create a new GridFS File from a scala.io.Source
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(data: scala.io.Source)(op: FileWriteOp): Nothing = withNewFile(data)(op)

  /**
   * Create a new GridFS File from a Byte Array
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(data: Array[Byte])(op: FileWriteOp): Option[AnyRef] = withNewFile(data)(op)

  /**
   * Create a new GridFS File from a java.io.File
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(f: File)(op: FileWriteOp): Option[AnyRef] = withNewFile(f)(op)

  /**
   * Create a new GridFS File from a java.io.InputStream
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(in: InputStream)(op: FileWriteOp): Option[AnyRef] = withNewFile(in)(op)

  /**
   * Create a new GridFS File from a java.io.InputStream and a specific filename
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(in: InputStream, filename: String)(op: FileWriteOp): Option[AnyRef] = withNewFile(in, filename)(op)

  /**
   * createFile
   *
   * Creates a new file in GridFS
   */
  def createFile(data: scala.io.Source): GridFSInputFile = throw new UnsupportedOperationException("Currently no support for scala.io.Source")

  def createFile(data: Array[Byte]): GridFSInputFile = underlying.createFile(data)

  def createFile(f: File): GridFSInputFile = underlying.createFile(f)

  def createFile(in: InputStream): GridFSInputFile = underlying.createFile(in)

  def createFile(in: InputStream, filename: String): GridFSInputFile = underlying.createFile(in, filename)

  def withNewFile(data: scala.io.Source)(op: FileWriteOp): Nothing = throw new UnsupportedOperationException("Currently no support for scala.io.Source")

  /**
   * Loan pattern style file creation.
   * @return The ID of the created File (Option[AnyRef])
   */
  def withNewFile(data: Array[Byte])(op: FileWriteOp): Option[AnyRef] = loan(createFile(data)) {
    fh =>
      op(fh)
      fh.save()
      fh.validate()
      Option(fh.id)
  }

  /**
   * Loan pattern style file creation.
   * @return The ID of the created File (Option[AnyRef])
   */
  def withNewFile(f: File)(op: FileWriteOp): Option[AnyRef] = loan(createFile(f)) {
    fh =>
      op(fh)
      fh.save()
      fh.validate()
      Option(fh.id)
  }

  /**
   * Loan pattern style file creation.
   * @return The ID of the created File (Option[AnyRef])
   */
  def withNewFile(in: InputStream)(op: FileWriteOp): Option[AnyRef] = loan(createFile(in)) {
    fh =>
      op(fh)
      fh.save()
      fh.validate()
      Option(fh.id)
  }

  /**
   * Loan pattern style file creation.
   * @return The ID of the created File (Option[AnyRef])
   */
  def withNewFile(in: InputStream, filename: String)(op: FileWriteOp): Option[AnyRef] =
    loan(createFile(in, filename)) {
      fh =>
        op(fh)
        fh.save()
        fh.validate()
        Option(fh.id)
    }

  def findOne[A](query: A)(implicit ev$1: A => DBObject): Option[GridFSDBFile] = {
    filesCollection.findOne(query) match {
      case None => None
      case x => {
        val gridfsFile = x.get
        gridfsFile.setGridFS(underlying)
        Some(new GridFSDBFile(gridfsFile))
      }
    }
  }

  def findOne(id: ObjectId): Option[GridFSDBFile] = findOne(MongoDBObject("_id" -> id))

  def findOne(filename: String): Option[GridFSDBFile] = findOne(MongoDBObject("filename" -> filename))

}

class GridFSFile(_underlying: MongoGridFSFile) extends GenericGridFSFile(_underlying) with ConvertToDate

class GridFSDBFile(_underlying: MongoGridFSDBFile) extends GenericGridFSDBFile(_underlying) with ConvertToDate

class GridFSInputFile(_underlying: MongoGridFSInputFile) extends GenericGridFSInputFile(_underlying) with ConvertToDate

trait ConvertToDate {
  type DateType = java.util.Date

  def convertDate(in: AnyRef): DateType = in match {
    case d: java.util.Date => d
    case j: DateTime       => j.toDate
    case l: LocalDateTime  => l.toDateTime.toDate
  }
}
