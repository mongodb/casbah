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

import com.mongodb.casbah.commons.Logging 

import org.bson.{BSON, Transformer}


package org.bson {
  object BSONTransformMonkeyPatch extends Logging {
    // Just dumps info out to the log info of what's registered.
    def dump() = {
      log.info("Encoding Hooks: %s", org.bson.BSON._encodingHooks)
      log.info("Decoding Hooks: %s", org.bson.BSON._decodingHooks)
    }
  }

  object BSONEncoders extends Logging {
    // Finds all the transformers for a given class
    def apply(encodeType: Class[_]) = org.bson.BSON._encodingHooks.get(encodeType) match {
      case null => None
      case x => Some(x)
    }
    def apply() = org.bson.BSON._encodingHooks
    //def +=(elems: (Class[_], Transformer)) = elems.foreach { ct => 
    def remove(encodeType: Class[_]) =  apply(encodeType) match {
      case Some(list) => {
        log.info("Clearing encoding transform list ['%s'] for class '%s'", list, encodeType)
        org.bson.BSON._encodingHooks.remove(encodeType) //list.clear()
      }
      case None => log.warning("No encoding transformers found registered for class '%s'", encodeType)
    }
  }

  object BSONDecoders extends Logging {
    // Finds all the transformers for a given class
    def apply(decodeType: Class[_]) = org.bson.BSON._decodingHooks.get(decodeType) match {
      case null => None
      case x => Some(x)
    }
    def apply()  = org.bson.BSON._decodingHooks
    //def +=(elems: (Class[_], Transformer)) = elems.foreach { ct => 
    def remove(decodeType: Class[_]) = apply(decodeType) match {
      case Some(list) => {
        log.info("Clearing decoding transform list ['%s'] for class '%s'", list, decodeType)
        org.bson.BSON._decodingHooks.remove(decodeType) //list.clear()
      }
      case None => log.warning("No decoding transformers found registered for class '%s'", decodeType)
    }
  }
}

// vim: set ts=2 sw=2 sts=2 et:
