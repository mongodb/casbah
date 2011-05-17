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
package com.mongodb.casbah.dynamic

/**
 * Dynamic facilitation trait for MongoDB Objects.
 * Requires Scala 2.9+ with Dynamic support turned on.
 * If you do not know how to do that, you shouldn't be using this trait.
 * For sanity's sake, don't ask on the Casbah mailing list.
 * The reason why is that "Here Be Dragons".  We aren't sure this is a good idea yet.
 *
 * @since 2.2
 * @author Jorge Ortiz <jorge.ortiz@gmail.com> /*Original Prototype*/
 * @author Brendan W. McAdams <brendan@10gen.com>
 */
trait MongoDynamic extends Dynamic {
  def applyDynamic(name: String)(args: Any*): MongoDynamic
  def typed[A : Manifest]: Option[A]
}