/**
 * Copyright (c) 2008 - 2011 10gen, Inc. <http://10gen.com>
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
package com.mongodb.casbah

import com.mongodb.DBObject

import scalaj.collection.Imports._

/**
 * Helper class for creating ReadPreference instances
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.2
 * @see com.mongodb.ReadPreference
 */
object ReadPreference {

  /**
   * Reads come only through the Primary
   */
  val Primary = com.mongodb.ReadPreference.PRIMARY

  /**
   * Reads come from Secondary servers (equiv of old SlaveOK)
   */
  val Secondary = com.mongodb.ReadPreference.SECONDARY

  /**
   * Read by particular tags.
   * Note that you should provide an *ordered* Map for tags,
   * as the driver looks in order for matching servers.
   * (DBObjects are automatically order preserving)
   *
   * I.E. {dc: "london", type: "backup", foo: "bar"}
   *
   * Tries first to find a secondary with 'dc: "London"',
   * if it doesn't find one then 'type: "backup"', and finally
   * 'foo: "bar"'.
   */
  def apply(tags: DBObject) =
    com.mongodb.ReadPreference.withTags(tags)

  /**
   * Read by particular tags.
   * Note that you should provide an *ordered* Map for tags,
   * as the driver looks in order for matching servers.
   * (DBObjects are automatically order preserving)
   *
   * I.E. {dc: "london", type: "backup", foo: "bar"}
   *
   * Tries first to find a secondary with 'dc: "London"',
   * if it doesn't find one then 'type: "backup"', and finally
   * 'foo: "bar"'.
   */
  def apply(tags: Map[String, String]) =
    com.mongodb.ReadPreference.withTags(tags)
}