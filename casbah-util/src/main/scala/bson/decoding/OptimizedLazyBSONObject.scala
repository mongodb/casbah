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

import org.bson._
import org.bson.types._

import com.mongodb.DBObject 

import java.util.{ Date, UUID }

import scala.annotation.{ tailrec, switch }
import com.mongodb.casbah.util.bson.decoding.io.BSONByteBuffer;

import scala.util.control.Exception._
import scala.collection.JavaConversions._
import scala.collection.mutable.{ HashMap, HashSet } 

/**
 * @author Brendan McAdams <brendan@10gen.com>
 */
class OptimizedLazyBSONObject(val input: BSONByteBuffer,
                              val callback: OptimizedLazyBSONCallback, 
                              val doc_start: Int = 0) extends BSONObject with Logging { self => 
  type LookupRecord = Pair[Byte, Int]

  final val FirstElementOffset = 4
  final val fieldIndex = HashMap.empty[String, LookupRecord] 
  final val noHitIndex = HashSet.empty[String] 

  /** 
   * We are implementing a java based API so of course 
   * we get the fun of returning null.
   *
   * ... ugh.
   */
  def get(key: String): AnyRef = {

    if (noHitIndex.contains( key )) 
      null
    
    @tailrec def findField(offset: Int): Option[LookupRecord] = {
      if ( elementEmpty_?( offset ) ) 
        None
      else {
        val _type = elementType( offset )
        val name = input.cString( offset + 1 )    
        val v = offset + ( sizeCString( offset + 1 ) + 1 )
      
        val t_record = (_type, v)

        fieldIndex put (name, t_record)

        if ( name == key ) 
          Some( t_record )
        else 
          findField( offset + ( sizeCString( offset ) + 
                                elementBSONSize( offset, _type ) ) )
      }
    }

    fieldIndex.get( key ) match {
      case Some( record ) => elementValue( record )
      case None => findField( doc_start + FirstElementOffset ) match {
        case Some( record ) => elementValue( record ) 
        case None => 
          noHitIndex += key
          null // Goddamn Java based interfaces, I hate returning null
      }
    }

  }

  @Deprecated
  def containsKey(key: String): Boolean = 
    containsField( key )

  def containsField(key: String): Boolean = 
    if (noHitIndex.contains( key )) 
      false 
    else if (fieldIndex.contains( key )) 
      true
    else 
      keySet().contains( key )


  def keySet: java.util.Set[String] = new OptimizedLazyBSONKeySet()

  def elementEmpty_?(offset: Int): Boolean = elementType( offset ) == BSON.EOO

  def empty_?(): Boolean = elementEmpty_?( doc_start + FirstElementOffset )

  def isEmpty: Boolean = empty_?

  def bsonSize(offset: Int = doc_start) = input.int(offset)
  
  protected def elementFieldName(offset: Int) = input.cString(offset)

  protected def elementType(offset: Int) = input(offset)

  protected def elementBSONSize(offset: Int, _type: Byte = -1): Int = {
    import BSON._

    val t = if (_type == -1) elementType(offset + 1) else _type
    val n = sizeCString( offset + 1 )
    val v = (offset + 1 + 1 ) + n // Value Offset 

    (t: @switch) match {
      case EOO | UNDEFINED | NULL | MAXKEY | MINKEY => 0
      case BOOLEAN => 1
      case NUMBER_INT => 4
      case TIMESTAMP | DATE | NUMBER_LONG | NUMBER => 8
      case OID => 12
      case SYMBOL | CODE | STRING => bsonSize( v ) + 4
      case CODE_W_SCOPE => bsonSize( v ) 
      case REF => bsonSize( 4 ) + 12
      case OBJECT | ARRAY => bsonSize( v )
      case BINARY => bsonSize( v ) + 4 + 1 /* subtype */ 
      case REGEX => 
        // Comprised of 2 cStrings
        val _1 = sizeCString( v ) + 1 
        _1 + ( sizeCString( v + _1 ) + 1 ) + 4
      case _ =>
        throw new BSONException("Invalid BSON Type '" + t + "'.")
    }
  }

  protected def sizeCString(offset: Int) = {
    @tailrec def cStrSz(x: Int): Int = input(x) match {
      case 0 => x
      case b => cStrSz(x + 1)
    }
    cStrSz(offset + 1) - offset + 1
  }

  protected def elementValue(record: LookupRecord): AnyRef = {
    import BSON._ 
    val v: Int = record._2 // Value Offset

    /** Switch lookup on type */
    ( (record._1: @switch) match {
      case EOO | UNDEFINED | NULL => 
        null
      case MAXKEY => 
        new MaxKey()
      case MINKEY => 
        new MinKey()
      case BOOLEAN => 
        input( v ) != 0
      case NUMBER => 
        java.lang.Double.longBitsToDouble( input.long( v ) ) 
      case NUMBER_INT => 
        input.int( v )
      case NUMBER_LONG =>
        input.long( v )
      case TIMESTAMP => 
        new BSONTimestamp( input.int( v + 4 ), 
                           input.int( v ) ) 
      case DATE => 
        new Date( input.long( v ) )
      case OID => 
        new ObjectId( input.intBE( v ),
                      input.intBE( v + 4 ), 
                      input.intBE( v + 8 ) )
      case STRING => 
        input.utf8String( v )
      case SYMBOL => 
        new Symbol( input.utf8String( v ) )
      case CODE => 
        new Code( input.utf8String( v ) )
      case CODE_W_SCOPE => 
        new CodeWScope( input.utf8String( v + 4 ),
                        callback.createObject( input.array, 
                                               v + 4 + 4 + bsonSize( v + 4) ).asInstanceOf[BSONObject]
                      )
      case REF => 
        val o = v + bsonSize( v ) + 4
        val ns = input.cString( v + 4 )
        val oid = new ObjectId( input.intBE( o ),
                                input.intBE( o + 4 ), 
                                input.intBE( o + 8 ) )
        callback.createDBRef( ns, oid ) 
      case OBJECT => 
        callback.createObject( input.array, v )
      case ARRAY => 
        callback.createObject( input.array, v )
      case BINARY => 
        readBinary( v )
      case REGEX => 
        java.util.regex.Pattern.compile( input.cString( v ), // pattern
            regexFlags ( input.cString( v + sizeCString( v ) ) ) /* flags*/ )  
      case _ =>
        throw new BSONException("Invalid BSON Type '" + record._1 + "'.")

    } ).asInstanceOf[AnyRef] // f-ing boxing bullshit
  }

  private def readBinary(offset: Int): AnyRef = {
    import BSON.{ B_GENERAL, B_BINARY, B_UUID }

    val l = bsonSize( offset ) 

    (input( offset + 4 ): @switch) match { 
      case B_BINARY => 
        val _l = bsonSize( offset + 4 )
        require(_l + 4 ==  l, "Bad Data Size: Binary Subtype 2." + " { Got: " + 
                              _l + " Expected: " + l + "}")
          ( for {
              n <- 0 until l 
            } yield input( (offset + 4 + 4) + n ) ).toArray

      case B_UUID => 
        require( l == 16,  "Bad Data Size: Binary Subtype 3 (UUID)." + " { Got: " + 
                           l + " Expected: 16 }")
        new UUID( input.long( offset + 4 ), input.long( offset + 4 + 8 ) )
      /** B_GENERAL behavior is identical to 'default'. */
      case B_GENERAL | _ => 
        ( for { 
            n <- 0 until l
          } yield input( (offset + 4) + n ) ).toArray
    }
  }
  
  /**
   * Returns a JSON Serialization of the object
   */
  override def toString: String = com.mongodb.util.JSON.serialize(this)
  
  def put(key: String, v: AnyRef): AnyRef = 
    throw new UnsupportedOperationException("Read Only.")

  def putAll(o: BSONObject): Unit = 
    throw new UnsupportedOperationException("Read Only.")
  
  def putAll(m: java.util.Map[_, _]): Unit = 
    throw new UnsupportedOperationException("Read Only.")

  def toMap(): java.util.Map[_, _] = 
    throw new UnsupportedOperationException("Read Only.")

  def removeField(key: String): AnyRef = 
    throw new UnsupportedOperationException("Read Only.")


  class OptimizedLazyBSONIterator extends java.util.Iterator[String] {
    var offset = doc_start + FirstElementOffset

    def hasNext(): Boolean = !elementEmpty_?(offset) 

    def next(): String = {
      val fieldSize = sizeCString(offset)
      val elementSize = elementBSONSize(offset + 1)
      val key = input.cString( offset + 1 )
      offset += ( fieldSize + elementSize ) + 1
      key
    }

    def remove() {
      throw new UnsupportedOperationException("Read Only.")
    }
  }

  class OptimizedLazyBSONKeySet extends java.util.Set[String] {

    def size(): Int = iterator.size

    def isEmpty: Boolean = self.isEmpty

    def contains(o: AnyRef) = iterator.contains(o)

    def iterator: java.util.Iterator[String] = new OptimizedLazyBSONIterator()
    
    def toArray: Array[AnyRef] = 
      Array(iterator.map( _.asInstanceOf[AnyRef] ))

    def toArray[T](ts: Array[T with AnyRef]): Array[T with AnyRef] = {
      var i = 0
      for (k <- iterator if i < ts.length) 
        ts(i) = k.asInstanceOf[T with AnyRef]; i += 1
      ts
    }


    def add(e: String): Boolean = 
      throw new UnsupportedOperationException("Read Only.")

    def remove(e: AnyRef): Boolean = 
      throw new UnsupportedOperationException("Read Only.")

    def containsAll(coll: java.util.Collection[_]): Boolean = 
      iterator forall (coll contains)

    def addAll(coll: java.util.Collection[_ <: String]): Boolean = 
      throw new UnsupportedOperationException("Read Only.")

    def retainAll(coll: java.util.Collection[_]): Boolean = 
      throw new UnsupportedOperationException("Read Only.")

    def removeAll(coll: java.util.Collection[_]): Boolean = 
      throw new UnsupportedOperationException("Read Only.")
  
    def clear(): Unit = 
      throw new UnsupportedOperationException("Read Only.")
  }


}

class OptimizedLazyDBObject(input: BSONByteBuffer,
                            callback: OptimizedLazyBSONCallback, 
                            doc_start: Int = 0) 
  extends OptimizedLazyBSONObject(input, callback, doc_start) with DBObject {

  def markAsPartialObject = ()

  def isPartialObject: Boolean = false

}

// vim: set ts=2 sw=2 sts=2 et:
