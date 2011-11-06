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


/**
 * A Container for BSON Primitive types
 *
 */
sealed trait BSONPrimitive {
  /**
   * The "container" type for this...
   * basically the Scala side primitives
   * represented.
   * For example, a BSONDatePrimitive's ContainerType
   * is a Long , holding the milliseconds
   *
   * This is, essentially, the MOST RAW type representation
   * rather than a specific instantiation.
   */
  type ContainerType <: Any

  /**
   * The type represented by this primitive
   */
  def typeCode: BSONType.TypeCode

  /**
   * The "container" value
   */
  def bsonValue: ContainerType

}
// EOO is not a valid type container ;)

trait BSONDoublePrimitive extends BSONPrimitive {
  type ContainerType = Double
  val typeCode = BSONType.Double
}

trait BSONStringPrimitive extends BSONPrimitive {
  type ContainerType = String
  val typeCode = BSONType.String
}

trait BSONObjectPrimitive extends BSONPrimitive {
  // TODO - Hmm... this could be a Map[String, Any], no?
  type ContainerType = org.bson.BSONObject
  val typeCode = BSONType.Object
}

trait BSONArrayPrimitive extends BSONPrimitive {
  type ContainerType = Seq[Any]
  val typeCode = BSONType.Array
}

sealed trait BSONBinaryPrimitive extends BSONPrimitive {
  val typeCode = BSONType.Binary
  def subtypeCode: BSONBinarySubtype.SubtypeCode
}

trait BSONGenericBinaryPrimitive extends BSONBinaryPrimitive {
  type ContainerType = Array[Byte]
  val subtypeCode = BSONBinarySubtype.Generic
}

trait BSONBinaryFunctionPrimitive extends BSONBinaryPrimitive {
  type ContainerType = Array[Byte] // TODO - Better type repr?
  val subtypeCode = BSONBinarySubtype.Function
}

trait BSONOldBinaryPrimitive extends BSONBinaryPrimitive {
  type ContainerType = Array[Byte]
  val subtypeCode = BSONBinarySubtype.OldBinary
}

trait BSONUUIDPrimitive extends BSONBinaryPrimitive {
  type ContainerType = (Long /* most sig bits */, Long /* least sig bits */)
  val subtypeCode = BSONBinarySubtype.UUID
}

trait BSONNewUUIDPrimitive extends BSONBinaryPrimitive {
  type ContainerType = (Long /* most sig bits */, Long /* least sig bits */)
  val subtypeCode = BSONBinarySubtype.NewUUID
}

/**
* the Java driver lacks support for MD5 ... disabled for now
*/

/**
 * Use this for your own custom binarys, BSONBinaryPrimitive is
 * sealed for safety/sanity
 */
trait BSONCustomBinaryPrimitive extends BSONBinaryPrimitive

trait BSONObjectIdPrimitive extends BSONPrimitive {
  type ContainerType = (Int /* time */, Int /* machineID */, Int /* Increment */)
  val typeCode = BSONType.ObjectId
}

trait BSONBooleanPrimitive extends BSONPrimitive {
  type ContainerType = Boolean
  val typeCode = BSONType.Boolean
}

trait BSONDatePrimitive extends BSONPrimitive {
  type ContainerType = Long /* the UTC milliseconds since the Unix Epoch. */
  val typeCode = BSONType.Date
}

trait BSONRegexPrimitive extends BSONPrimitive {
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
  type ContainerType = (String /* pattern */, String /* flags */)
}

/**
 * Skip support for the deprecated DBPointer
 */

trait BSONCodePrimitive extends BSONPrimitive {
  type ContainerType = String
  val typeCode = BSONType.Code
}

trait BSONSymbolPrimitive extends BSONPrimitive {
  /** Stored as a string */
  type ContainerType = String
  val typeCode = BSONType.Symbol
}

trait BSONScopedCodePrimitive extends BSONPrimitive {
  type ContainerType = (String /* code */, org.bson.BSONObject /* Scope */)
  val typeCode = BSONType.ScopedCode
}

trait BSONInt32Primitive extends BSONPrimitive {
  type ContainerType = Int
  val typeCode = BSONType.Int32
}

trait BSONTimestampPrimitive extends BSONPrimitive {
  type ContainerType = (Int /* Time */, Int /* Increment */)
  val typeCode = BSONType.Timestamp
}

trait BSONInt64Primitive extends BSONPrimitive {
  type ContainerType = Long /* Yes, Billy. We represent Longs as Longs! */
  val typeCode = BSONType.Int64
}

/** No hard representation of MinKey and MaxKey */
