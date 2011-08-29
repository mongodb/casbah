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
 * Enumerative lookup for BSON Types, as defined in the spec
 *
 * @see http://bsonspec.org/#/specification
 */
object BSONType extends Enumeration {
  type TypeCode = Value
  /**
   * End of Object marker.  Basically
   * a null terminator; used to
   * mark the end of several areas including
   * the end of any CStrings
   */
  val EOO = Value(0x00)
  val Double = Value(0x01)
  val String = Value(0x02)
  val Object = Value(0x03)
  val Array = Value(0x04)
  val Binary = Value(0x05)
  val Undefined = Value(0x06)
  val ObjectId = Value(0x07)
  val Boolean = Value(0x08)
  /**
   * UTC Datetime, represented as an int64
   *
   * Long Value is UTC milliseconds
   * since the Unix Epoch.
   */
  val Date = Value(0x09)
  val Null = Value(0x0A)
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
  val RegEx = Value(0x0B)
  @deprecated
  val DBPointer = Value(0x0C)
  val Code = Value(0x0D)
  val Symbol = Value(0x0E)
  /**
   * Code w/ scope
   *
   * The int32 is the length in bytes of the
   * entire code_w_s Value. The string is JavaScript code.
   * The document is a mapping from identifiers to Values,
   * representing the scope in which the string should be
   * evaluated
   */
  val ScopedCode = Value(0x0F)
  val Int32 = Value(0x10)
  /**
   * Timestamp - Special internal type used by MongoDB replication
   * and sharding. First 4 bytes are an increment,
   * second 4 are a timestamp.
   * Setting the timestamp to 0 has special semantics.
   */
  val Timestamp = Value(0x11)
  /**
   * AKA - "Long"
   */
  val Int64 = Value(0x12)
  val MinKey = Value(0xFF)
  val MaxKey = Value(0x7F)
}

object BSONBinarySubtype extends Enumeration {
  type SubtypeCode = Value
  /**
   * [Generic binary subtype]
   *
   * This is the most commonly used binary
   * subtype and should be the 'default' for
   * drivers and tools
   */
  val Generic = Value(0x00)
  val Function = Value(0x01)
  /**
   * [Old generic subtype]
   *
   * This used to be the default subtype,
   * but was deprecated in favor of \x00.
   * Drivers and tools should be sure to handle \x02 appropriately.
   * The structure of the binary data (the byte* array in the binary
   * non-terminal) must be an int32 followed by a (byte*).
   * The int32 is the number of bytes in the repetition.
   */
  val OldBinary = Value(0x02)
  /**
   * "Old" UUID Type.
   * There are some planend changes happening to
   * the UUID system
   * which will be explained at a later date.
   */
  val UUID = Value(0x03)
  /**
   * "Old" UUID Type.
   * There are some planend changes happening to
   * the UUID system
   * which will be explained at a later date.
   */
  val NewUUID = Value(0x04)
  val MD5 = Value(0x05)
  /**
   * [User defined]
   *
   * Technically users may define ANY SubType Value.
   * You may want to consider using 80+ for yourself
   * to avoid conflicts with future expansions w/i BSON.
   *
   * The structure of the binary data can be anything.
   */
  val UserDefined = Value(0x80)
}