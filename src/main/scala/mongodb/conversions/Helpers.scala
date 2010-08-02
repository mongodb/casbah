/**
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
 *     http://bitbucket.org/novus/casbah
 * 
 * NOTICE: Portions of this work are derived from the Apache License 2.0 "mongo-scala-driver" work
 * by Alexander Azarov <azarov@osinka.ru>, available from http://github.com/alaz/mongo-scala-driver
 */

package com.novus.casbah.mongodb
package conversions

import com.novus.casbah.util.Logging 

import com.mongodb._
import org.bson.{BSON, Transformer}

import org.scala_tools.time.Imports._


trait MongoConversionHelper extends Logging {

  def register() = {
    log.debug("Reached base registration method on MongoConversionHelper.")
  }

  def unregister() = {
    log.debug("Reached base de-registration method on MongoConversionHelper.")
  }
}
// vim: set ts=2 sw=2 sts=2 et:
