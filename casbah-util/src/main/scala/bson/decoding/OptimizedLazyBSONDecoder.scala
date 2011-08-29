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

package com.mongodb.casbah.util;
package bson.decoding;

import com.mongodb._ 
import org.bson._
import org.bson.types._

import java.io.{ InputStream, ByteArrayInputStream, IOException }

import java.util.{ Date, UUID }

import scala.annotation.{ tailrec, switch }
import com.mongodb.casbah.util.bson.decoding.io.BSONByteBuffer;

import scala.util.control.Exception._
import scala.collection.JavaConversions._
import scala.collection.mutable.{ HashMap, HashSet } 

/**
 * @author Brendan McAdams <brendan@10gen.com>
 */
class OptimizedLazyBSONDecoder extends BSONDecoder with Logging {
    
  def readObject(b: Array[Byte]): BSONObject = try {
    readObject( new ByteArrayInputStream( b ) )
  } catch {
    case e: IOException => 
      throw new BSONException("Failed to deserialize BSON Object from byte array.", e )
  }

  def readObject(in: InputStream): BSONObject = {
    val cb = new OptimizedLazyBSONCallback
    decode(in, cb)
    cb().asInstanceOf[BSONObject]
  }

  /** 
   * TODO - Skip the byte array input stream where possible.
   */
  def decode(b: Array[Byte], callback: BSONCallback): Int = try {
    callback.gotBinary(null, 0: Byte, b)
    b.length
  } catch {
    case e: IOException => 
      throw new BSONException("Failed to deserialize BSON from byte array.", e )
  }

  def decode(in: InputStream, callback: BSONCallback): Int = {
    val head = Array.ofDim[Byte](4)

    in.read(head)

    val objSize = org.bson.io.Bits.readInt(head) - 4

    val data = Array.ofDim[Byte](objSize)

    in.read(data)

    callback.gotBinary(null, 0: Byte, Array.concat(head, data))

    objSize
  }

}

class OptimizedLazyDBDecoder extends OptimizedLazyBSONDecoder with DBDecoder {
  /** 
   * Callback doesn't do anything special here,
   * could potentially be unique per decoder, but 
   * for now we need one per collection due to DBRef issues.
   */
  def getDBCallback(collection: DBCollection): DBCallback = 
    new OptimizedLazyDBCallback(collection)

  def decode(b: Array[Byte], collection: DBCollection) = {
    val cb = getDBCallback(collection)
    cb.reset
    decode(b, cb)
    cb.get.asInstanceOf[DBObject]
  }

  def decode(in: InputStream, collection: DBCollection) = {
    val cb = getDBCallback(collection)
    cb.reset
    decode(in, cb)
    cb.get.asInstanceOf[DBObject]
  }

}

object OptimizedLazyDBDecoder {
  final val Factory = OptimizedLazyDBDecoderFactory
}

object OptimizedLazyDBDecoderFactory extends DBDecoderFactory {
  override def create(): DBDecoder = new OptimizedLazyDBDecoder
}

// vim: set ts=2 sw=2 sts=2 et:
