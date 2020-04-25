/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.mongodb.casbah

import com.mongodb.{ ReadPreference => JReadPreference, TagSet }

import scala.jdk.CollectionConverters._

/**
 * Helper class for creating ReadPreference instances
 *
 * @since 2.2
 * @see  JReadPreference
 */
object ReadPreference {

  /**
   * Reads come only through the Primary
   */
  val Primary: JReadPreference = JReadPreference.primary()

  /**
   * Reads come from Secondary servers (equiv of old SlaveOK)
   */
  val Secondary: JReadPreference = JReadPreference.secondary()

  /**
   * Reads come from secondary if available, otherwise from primary
   */
  val SecondaryPreferred: JReadPreference = JReadPreference.secondaryPreferred()

  /**
   * Reads come from nearest node.
   */
  val Nearest: JReadPreference = JReadPreference.nearest()

  /**
   *
   * @return ReadPreference with reads primary if available
   */
  def primaryPreferred: JReadPreference = JReadPreference.primaryPreferred()

  /**
   *
   * @return ReadPreference with reads primary if available
   */
  def primaryPreferred(tagSet: TagSet): JReadPreference = JReadPreference.primaryPreferred(tagSet)
  /**
   *
   * @return ReadPreference with reads primary if available
   */
  def primaryPreferred(tagSetList: List[TagSet]): JReadPreference = JReadPreference.primaryPreferred(tagSetList.asJava)

  /**
   * @return ReadPreference which returns secondary respective of tags
   */
  def secondary(tagSet: TagSet): JReadPreference = JReadPreference.secondary(tagSet)

  /**
   * @return ReadPreference which returns secondary respective of tags
   */
  def secondary(tagSetList: List[TagSet]): JReadPreference = JReadPreference.secondary(tagSetList.asJava)

  /**
   * @return ReadPreference which reads nearest node respective of tags
   */
  def nearest(tagSet: TagSet): JReadPreference = JReadPreference.nearest(tagSet)

  /**
   * @return ReadPreference which reads nearest node respective of tags
   */
  def nearest(tagSetList: List[TagSet]): JReadPreference = JReadPreference.nearest(tagSetList.asJava)

}
