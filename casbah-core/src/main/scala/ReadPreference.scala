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

import scala.collection.JavaConverters._
import com.mongodb.DBObject

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
  val Primary = com.mongodb.ReadPreference.primary()

  /**
   * Reads come from Secondary servers (equiv of old SlaveOK)
   */
  val Secondary = com.mongodb.ReadPreference.secondary()


  /**
   * Reads come from secondary if available, otherwise from primary
   */
  val SecondaryPreferred = com.mongodb.ReadPreference.secondaryPreferred()

  /**
   * Reads come from nearest node.
   */
  val Nearest = com.mongodb.ReadPreference.nearest()

  /**
   *
   * @return ReadPreference with reads primary if available
   */
  def primaryPreferred = com.mongodb.ReadPreference.primaryPreferred()

  /**
   * @return ReadPreference which reads primary if available, otherwise a secondary respective of tags.
   */
  def primaryPreferred(firstTagSet: DBObject, remainingTagSets: DBObject*) =
    com.mongodb.ReadPreference.primaryPreferred(firstTagSet, remainingTagSets: _*)

  /**
   * @return ReadPreference which returns secondary respective of tags
   */
  def secondary(firstTagSet: DBObject, remainingTagSets: DBObject*) =
    com.mongodb.ReadPreference.secondary(firstTagSet, remainingTagSets: _*)

  /**
   * @return ReadPreference which reads secondary if available respective of tags,
   *         otherwise from primary irresepective of tags
   */
  def secondaryPreferred(firstTagSet: DBObject, remainingTagSets: DBObject*) =
    com.mongodb.ReadPreference.secondaryPreferred(firstTagSet, remainingTagSets: _*)

  /**
   * @return ReadPreference which reads nearest node respective of tags
   */
  def nearest(firstTagSet: DBObject, remainingTagSets: DBObject*) =
    com.mongodb.ReadPreference.nearest(firstTagSet, remainingTagSets: _*)

}
