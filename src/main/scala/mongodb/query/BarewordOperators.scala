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

package com.novus.casbah
package mongodb
package query

import util.Logging

import com.mongodb.{DBObject, BasicDBObjectBuilder}
import scala.collection.JavaConversions._

trait BarewordQueryOperator extends Logging {
  import com.mongodb.{BasicDBObject, BasicDBObjectBuilder}
  log.info("Instantiated SetterLike QueryHelper")

  def apply(oper: String)(fields: (String, Any)*) = { 
    log.info("Apply - %s", fields)
    val bldr = new BasicDBObjectBuilder
    for ((k, v) <- fields) bldr.add(k, v)
    new BasicDBObject(oper, bldr.get)
  }

}

/**
 * Trait to provide the $set (Set) Set method on appropriate callers.
 * 
 * Also provides a reversed version so you can bareword it such as:
 * 
 * $set_:("Foo" -> "bar")
 *
 * Targets (takes a right-hand value of) DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait SetOp extends BarewordQueryOperator {
  implicit def $set = apply("$set")_
}

/**
 * Trait to provide the $unset (UnSet) UnSet method on appropriate callers.
 *
 * Also provides a reversed version so you can bareword it such as:
 * 
 * $unset:("Foo" -> "bar")
 *
 * Targets (takes a right-hand value of) DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait UnsetOp extends BarewordQueryOperator {
  implicit def $unset = apply("$unset")_
}

trait FluidQueryBarewordOps extends SetOp 
                               with UnsetOp



// vim: set ts=2 sw=2 sts=2 et:
