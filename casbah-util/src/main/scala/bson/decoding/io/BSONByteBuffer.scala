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
package bson.decoding.io;

import org.bson._
import org.bson.io._

import java.nio.{ ByteBuffer, ByteOrder }
import java.io.UnsupportedEncodingException

import scala.annotation.{ tailrec, switch }


import scala.util.control.Exception._

/**
 * [Based on a potentially forthcoming class in the Java Driver
 * which may not be ready for a necessary Casbah release]
 *
 * Pseudo Byte Buffer oriented around BSON Decoding, delegates
 * as it tends to be too hard to properly override/extend the ByteBuffer
 * API.
 *
 * Locked to Little Endian, with helpers for getting BigEndian Ints
 *
 * @author Brendan McAdams <brendan@10gen.com>
 */
class BSONByteBuffer protected[io](val buf: ByteBuffer) {
  val size = int(0)

  private val str_b = new PoolOutputBuffer()

  private val random = Array.ofDim[Byte](1024)

  buf.order( ByteOrder.LITTLE_ENDIAN )
  
  def apply(index: Int): Byte = buf.get(index)

  def char(index: Int): Char = buf.getChar(index)

  def short(index: Int): Short = buf.getShort(index)

  /**
   * Fetches a little endian integer by default
   */
  def int(index: Int, littleEndian: Boolean = true): Int = 
    if (littleEndian) intLE(index) else intBE(index)
  
  def intLE(index: Int): Int = {
    var x = 0
    x |= ( 0xFF & this(index + 0) ) << 0
    x |= ( 0xFF & this(index + 1) ) << 8
    x |= ( 0xFF & this(index + 2) ) << 16
    x |= ( 0xFF & this(index + 3) ) << 24
    x
  }

  def intBE(index: Int): Int = {
    var x = 0
    x |= ( 0xFF & this(index + 0) ) << 24
    x |= ( 0xFF & this(index + 1) ) << 16
    x |= ( 0xFF & this(index + 2) ) << 8
    x |= ( 0xFF & this(index + 3) ) << 0
    x
  }

  def long(index: Int): Long = buf.getLong(index)

  def float(index: Int): Float = buf.getFloat(index)

  def double(index: Int): Double = buf.getDouble(index)

  def cString(index: Int): String = {
    var ascii = true
    
    /**
     * Short circuit 1 byte strings 
     */
    random(0) = this( index + 1 )
    if (random(0) == 0) 
      ""
    else {
      random(1) = this( index + 2 )
      if ( random(1) == 0 ) {
        BSONByteBuffer.OneByteStrings( random(0) ) match {
          case null => try {
            new String(random, 0, 1, "UTF-8") 
          } catch {
            case _: UnsupportedEncodingException => 
              throw new BSONException("Cannot decode c-string as UTF-8") 
          }
          case onebyte => onebyte
        }
      }

      /**
       * An unfortunate trade off in case another thread
       * tries accessing this method at same time on same buffer.
       * We might consider making it thread local instead.
       */
      str_b synchronized {
        str_b reset()
        str_b write random(0)
        str_b write random(1)
        
        ascii = ascii_?( random(0) ) && ascii_?( random(1) ) 

        @tailrec def cStrByte(x: Int): Unit = this(x) match {
          case 0 => ()
          case b => 
            str_b write b
            ascii = ascii && ascii_?( b ) 
            cStrByte(x + 1)
        }

        cStrByte(index + 3)

        val out = if (ascii) 
          str_b.asAscii()
        else try {
          str_b.asString("UTF-8") 
        } catch {
          case _: UnsupportedEncodingException => 
            throw new BSONException("Cannot decode c-string as UTF-8") 
        }

        str_b.reset()

        out.intern()
      }
    }

  }

  def utf8String(index: Int): String = {
    var i = index 

    int(i) match {
      case 1 => "" 
      /**
       * Attempt to protect against corruption
       * by avoiding huge strings
       */
      case x if x <= 0 || x > ( 32 * 1024 * 1024 ) => 
        throw new BSONException( "Bad String Size: " + size )
      case x => 
        i += 4
        val b = if (x < random.length) random else Array.ofDim[Byte](x)

        for (n <- 0 until x) 
          b(n) = this(i + n)

        try {
          new String(b, 0, x - 1, "UTF-8").intern() 
        } catch {
          case _: UnsupportedEncodingException => 
            throw new BSONException("Cannot decode string as UTF-8") 
        }
    }
  }


  protected def ascii_?(b: Byte): Boolean = 
    b >= 0 && b <= 127

  def hasArray: Boolean = buf.hasArray

  def array: Array[Byte] = buf.array

  def arrayOffset: Int = buf.arrayOffset
  
  def compareTo(other: ByteBuffer): Int = 
    buf.compareTo(other)

  def order: ByteOrder = buf.order

}

object BSONByteBuffer {
  def apply(bytes: Array[Byte], offset: Int, length: Int) = 
    new BSONByteBuffer( ByteBuffer.wrap(bytes, offset, length) )

  def apply(bytes: Array[Byte]) =
    new BSONByteBuffer( ByteBuffer.wrap(bytes) )

  def wrap(bytes: Array[Byte], offset: Int, length: Int) = 
    this(bytes, offset, length)

  def wrap(bytes: Array[Byte]) = this(bytes)

  final val OneByteStrings = {
    val arr_b = Array.ofDim[String](128) 
    def fill(min: Byte, max: Byte) = min to max foreach { n =>
      val b: Byte = n.toByte
      arr_b(b.toInt) = new String(Array[Char](b toChar))
    }

    fill('0': Byte, '9': Byte)
    fill('A': Byte, 'Z': Byte)
    fill('a': Byte, 'z': Byte)
    arr_b
  }

}

// vim: set ts=2 sw=2 sts=2 et:
