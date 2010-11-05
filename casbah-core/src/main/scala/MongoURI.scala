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

import com.mongodb.casbah.Imports._

import scalaj.collection.Imports._


/** 
 * 
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 */
object MongoURI {
  
  /** 
   * Create a new MongoURI with a URI String e.g.:
   *
   * mongodb://localhost 
   * mongodb://fred:foobar@localhost/
   * 
   * @param  uri (String) 
   */
  def apply(uri: String) = new MongoURI(new com.mongodb.MongoURI(uri))
}

/** 
 * Create a new MongoURI with a URI String e.g.:
 *
 * mongodb://localhost 
 * mongodb://fred:foobar@localhost/
 * 
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 */
class MongoURI (val underlying: com.mongodb.MongoURI) {
  def username = underlying.getUsername
  def password = underlying.getPassword
  def hosts = underlying.getHosts.asScala
  def database = underlying.getDatabase
  def collection = underlying.getCollection
  def options = underlying.getOptions
  def connect = underlying.connect.asScala
  def connectDB = underlying.connectDB.asScala
  def connectDB(m: MongoConnection) = 
    underlying.connectDB(m.underlying).asScala
  def connectCollection(db: MongoDB) = 
    underlying.connectCollection(db.underlying).asScala
  def connectCollection(m: MongoConnection) = 
    underlying.connectCollection(m.underlying).asScala
  
  override def toString = underlying.toString
}

// vim: set ts=2 sw=2 sts=2 et:
