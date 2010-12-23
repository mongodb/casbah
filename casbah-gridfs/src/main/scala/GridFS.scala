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
package gridfs

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.commons.conversions.scala._

import com.mongodb.DBObject
import com.mongodb.gridfs.{GridFS => MongoGridFS, GridFSDBFile => MongoGridFSDBFile, GridFSFile => MongoGridFSFile, GridFSInputFile => MongoGridFSInputFile}

import java.io._ 

import scala.reflect._
import scalaj.collection.Imports._

import org.scala_tools.time.Imports._

// todo - look into potential naming conflicts... 

/** 
 * Companion object for GridFS.
 * Entry point for creation of GridFS Instances.
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
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

class GridFS protected[gridfs](val underlying: MongoGridFS) extends Iterable[GridFSDBFile] with Logging {
  log.info("Instantiated a new GridFS instance against '%s'", underlying) 

  type FileOp = GridFSFile => Unit
  type FileWriteOp = GridFSInputFile => Unit
  type FileReadOp = GridFSDBFile => Unit

  implicit val db = underlying.getDB().asScala

  def iterator = new Iterator[GridFSDBFile] {
    val fileSet = files.underlying
    def count() = fileSet.count
    def itcount() = fileSet.itcount()
    def jIterator() = fileSet.iterator asScala
    override def length() = fileSet.length
    def numGetMores() = fileSet.numGetMores
    def numSeen() =  fileSet.numSeen
    def remove() = fileSet.remove

    def curr() = new GridFSDBFile(fileSet.curr.asInstanceOf[MongoGridFSDBFile])
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
   protected[gridfs] def loan[T <: GridFSFile](file: T)(op: T => Unit) = op(file)

   /**
    * Create a new GridFS File from a scala.io.Source
    * 
    * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
    * as a parameter.
    * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
    * If you don't want automatic saving/loaning please see the createFile method instead.
    * @see createFile 
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
   def withNewFile(data: Array[Byte])(op: FileWriteOp) { loan(createFile(data))({ fh => op(fh); fh.save } ) }
   def createFile(f: File): GridFSInputFile = underlying.createFile(f)
   def withNewFile(f: File)(op: FileWriteOp) { loan(createFile(f))({ fh => op(fh); fh.save }) }
   def createFile(in: InputStream): GridFSInputFile = underlying.createFile(in)
   def withNewFile(in: InputStream)(op: FileWriteOp) { loan(createFile(in))({ fh => op(fh); fh.save }) }
   def createFile(in: InputStream, filename: String): GridFSInputFile = underlying.createFile(in, filename)
   def withNewFile(in: InputStream, filename: String)(op: FileWriteOp) { loan(createFile(in, filename))({ fh => op(fh); fh.save }) }

  /** Hacky fun - 
   * Unload the Joda Time code, IF ITS LOADED, and reload it after we're done.
   * This should minimize conflicts or exceptions from forgetting.
   * This is hacky as it can potentially clobber anybody's "custom" java.util.Date deserializer with ours.  
   * TODO - Make this more elegant
   */
   def sansJodaTime[T](op: => T) = org.bson.BSON.getDecodingHooks(classOf[java.util.Date]) match {   
      case null => {
        log.trace("Didn't find a registration for JodaTime.")
        op
      }
      case transformer => {
        log.trace("DateTime Decoder was loaded; unloading before continuing.")
        new JodaDateTimeDeserializer { unregister() }
        val ret = op
        log.trace("Retrieval finished.  Re-registering decoder.")
        new JodaDateTimeDeserializer { register() }
        ret
      }
    }

   /** Find by query - returns a list */
   def find(query: DBObject) = sansJodaTime { underlying.find(query).asScala }
   /** Find by query - returns a single item */
   def find(id: ObjectId): GridFSDBFile = sansJodaTime { underlying.find(id) }
   /** Find by query - returns a list */
   def find(filename: String) = sansJodaTime { underlying.find(filename).asScala }

   def findOne(query: DBObject): Option[GridFSDBFile] = sansJodaTime {
     underlying.findOne(query) match {
      case null => None
      case x => Some(x)
     }
   }
   def findOne(id: ObjectId): Option[GridFSDBFile] = sansJodaTime {
     underlying.findOne(id) match {
      case null => None
      case x => Some(x)
     }
   }
   def findOne(filename: String): Option[GridFSDBFile] = sansJodaTime {
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
   def files = sansJodaTime { new MongoCursor(underlying.getFileList) }
   def files(query: DBObject) = sansJodaTime { new MongoCursor(underlying.getFileList(query)) } 

   def remove(query: DBObject) = underlying.remove(query)
   def remove(id: ObjectId) = underlying.remove(id)
   def remove(filename: String) = underlying.remove(filename)
}

@BeanInfo
trait GridFSFile extends MongoDBObject with Logging {
  val underlying: MongoGridFSFile
  def save = underlying.save

  override def iterator = underlying.keySet.asScala.map { k =>
    k -> underlying.get(k)
  }.toMap.iterator.asInstanceOf[Iterator[(String, AnyRef)]]
  /** 
   * validate the object.
   * Throws an exception if it fails
   * @throws MongoException An error describing the validation failure
   */
  def validate = underlying.validate

  def numChunks: Int = underlying.numChunks

  def id = underlying.getId
  def filename = underlying.getFilename
  // todo - does mongo support mime magic? Should we pull some in here?
  def contentType = underlying.getContentType
  def length = underlying.getLength
  def chunkSize = underlying.getChunkSize
  def uploadDate = underlying.getUploadDate
  def aliases = underlying.getAliases.asScala
  def metaData: DBObject = underlying.getMetaData
  def md5: String = underlying.getMD5
  
  override def toString = "{ GridFSFile(id=%s, filename=%s, contentType=%s) }".
                              format(id, filename, contentType)

}

@BeanInfo
class GridFSDBFile protected[gridfs](override val underlying: MongoGridFSDBFile) extends GridFSFile {
  override def toString = "{ GridFSDBFile(id=%s, filename=%s, contentType=%s) }".
                              format(id, filename, contentType)
}

@BeanInfo
class GridFSInputFile protected[gridfs](override val underlying: MongoGridFSInputFile) extends GridFSFile {
  def filename_=(name: String) = underlying.setFilename(name)
  def contentType_=(cT: String) = underlying.setContentType(cT)
}

// vim: set ts=2 sw=2 sts=2 et:
