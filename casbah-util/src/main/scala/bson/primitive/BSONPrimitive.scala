/**
 * Copyright (c) 2010, 2011 10gen, Inc. <http://10gen.com>
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
 */
package com.mongodb.casbah.util.bson
package primitive


/**
 * A Container for BSON Primitive types
 *
 */
sealed trait BSONPrimitive[Raw] {
  /**
   * The "container" type for this...
   * basically the Scala side primitives
   * represented.
   * For example, a BSONDatePrimitive's Primitive
   * is a Long , holding the milliseconds
   *
   * This is, essentially, the MOST RAW type representation
   * rather than a specific instantiation.
   */
  type Primitive <: Any

  /**
   * The type represented by this primitive
   */
  def typeCode: BSONType.TypeCode

  /**
   * The bson "container" value, from the raw type
   *
   * e.g. Int -> BSON Representation of an Int
   *
   */
  def bsonValue(raw: Raw): Primitive

  /**
  * The "raw" type, read from the BSON Primitive
  *
  * e.g. BSON Integer -> JVM Int
  */
  def rawValue(bson: Primitive): Raw

}
// EOO is not a valid type container ;)

trait BSONDoublePrimitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = Double
  val typeCode = BSONType.Double
}

trait BSONStringPrimitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = String
  val typeCode = BSONType.String
}

trait BSONObjectPrimitive[Raw] extends BSONPrimitive[Raw] {
  // TODO - Hmm... this could be a Map[String, Any], no?
  type Primitive = org.bson.BSONObject
  val typeCode = BSONType.Object
}

trait BSONArrayPrimitive[T, Raw] extends BSONPrimitive[Raw] {
  type Primitive = Seq[T]
  val typeCode = BSONType.Array
}

sealed trait BSONBinaryPrimitive[Raw] extends BSONPrimitive[Raw] {
  val typeCode = BSONType.Binary
  def subtypeCode: BSONBinarySubtype.SubtypeCode
}

trait BSONGenericBinaryPrimitive[Raw] extends BSONBinaryPrimitive[Raw] {
  type Primitive = Array[Byte]
  val subtypeCode = BSONBinarySubtype.Generic
}

trait BSONBinaryFunctionPrimitive[Raw] extends BSONBinaryPrimitive[Raw] {
  type Primitive = Array[Byte] // TODO - Better type repr?
  val subtypeCode = BSONBinarySubtype.Function
}

trait BSONOldBinaryPrimitive[Raw] extends BSONBinaryPrimitive[Raw] {
  type Primitive = Array[Byte]
  val subtypeCode = BSONBinarySubtype.OldBinary
}

trait BSONUUIDPrimitive[Raw] extends BSONBinaryPrimitive[Raw] {
  type Primitive = (Long /* most sig bits */, Long /* least sig bits */)
  val subtypeCode = BSONBinarySubtype.UUID
}

trait BSONNewUUIDPrimitive[Raw] extends BSONBinaryPrimitive[Raw] {
  type Primitive = (Long /* most sig bits */, Long /* least sig bits */)
  val subtypeCode = BSONBinarySubtype.NewUUID
}

/**
* the Java driver lacks support for MD5 ... disabled for now
*/

/**
 * Use this for your own custom binarys, BSONBinaryPrimitive is
 * sealed for safety/sanity
 */
trait BSONCustomBinaryPrimitive[Raw] extends BSONBinaryPrimitive[Raw]

trait BSONObjectIdPrimitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = (Int /* time */, Int /* machineID */, Int /* Increment */)
  val typeCode = BSONType.ObjectId
}

trait BSONBooleanPrimitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = Boolean
  val typeCode = BSONType.Boolean
}

trait BSONDatePrimitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = Long /* the UTC milliseconds since the Unix Epoch. */
  val typeCode = BSONType.Date
}

trait BSONRegexPrimitive[Raw] extends BSONPrimitive[Raw] {
  val typeCode = BSONType.RegEx
  /**
   * [Regular expression]
   *
   * The first cstring is the regex pattern,
   * the second is the regex options string.
   *
   * Options are identified by characters,
   * which must be stored in alphabetical order.
   *
   * Valid options are:
   * 'i' for case insensitive matching,
   * 'm' for multiline matching,
   * 'x' for verbose mode,
   * 'l' to make \w, \W, etc. locale dependent,
   * 's' for dotall mode ('.' matches everything),
   * 'u' to make \w, \W, etc. match unicode.
   */
  type Primitive = (String /* pattern */, String /* flags */)
}

/**
 * Skip support for the deprecated DBPointer
 */

trait BSONCodePrimitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = String
  val typeCode = BSONType.Code
}

trait BSONSymbolPrimitive[Raw] extends BSONPrimitive[Raw] {
  /** Stored as a string */
  type Primitive = String
  val typeCode = BSONType.Symbol
}

trait BSONScopedCodePrimitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = (String /* code */, org.bson.BSONObject /* Scope */)
  val typeCode = BSONType.ScopedCode
}

trait BSONInt32Primitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = Int
  val typeCode = BSONType.Int32
}

trait BSONTimestampPrimitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = (Int /* Time */, Int /* Increment */)
  val typeCode = BSONType.Timestamp
}

trait BSONInt64Primitive[Raw] extends BSONPrimitive[Raw] {
  type Primitive = Long /* Yes, Billy. We represent Longs as Longs! */
  val typeCode = BSONType.Int64
}

/** No hard representation of MinKey and MaxKey */
