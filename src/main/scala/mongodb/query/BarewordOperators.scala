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


case class DefaultRValue(default: Any)


/** 
 * Base Operator class for Bareword Operators.
 * 
 * Bareword operators stand on their own - they lack the requirement for an LValue.
 * 
 * Operator implementations (see SetOp for an example) should partially apply apply with just their operator name.
 * The apply method's type parameter can be used to restrict the valid RValue values at will.  
 * 
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0, 06/17/10
 * @since 1.0
 * @see SetOp
 */
trait BarewordQueryOperator extends Logging {
  import com.mongodb.{BasicDBObject, BasicDBObjectBuilder}
  log.info("Instantiated SetterLike QueryHelper")

  def apply[A : Manifest](oper: String, defaultRVal: DefaultRValue = DefaultRValue(None))(fields: A*) = {
    val m = manifest[A]
    log.info("Apply - %s (Manifest: %s)", fields, m.erasure)
    val bldr = new BasicDBObjectBuilder
    defaultRVal match {
      // Assume without defaulting they're passing a pair in. TODO - fix this check for sanity
      case DefaultRValue(x @ None) => 
        if (m <:< manifest[Tuple2[String, _]]) 
          for ((k, v) <- fields) bldr.add(k.asInstanceOf[String], v) 
        else 
          throw new IllegalArgumentException("Internal Error: BarewordQueryOperator() must be invoked with a Tuple2[String, _]. If you intended to not use a tuple you must pass an explicit instance of DefaultRValue.")
      // If defaulting is set, it's not a pair
      case DefaultRValue(default) =>
        if (m <:< manifest[String]) 
          for (k <- fields) bldr.add(k.asInstanceOf[String], default);
        else 
          throw new IllegalArgumentException("Internal Error: BarewordQueryOperator() with a DefaultRValue must be invoked with a String for it's Type arg.")
    }
    new BasicDBObject(oper, bldr.get)
  }

}

/** 
 * Aggregation object for Bareword Operators.
 * Bareword operators stand on their own - they lack the requirement for an LValue.
 * This mixes them in so they can be pulled down in a single import.
 *
 * Typically, you want to follow the model Implicits does, and mix this in
 * if you want to use it but not import Implicits
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0, 06/17/10
 * @since 1.0
 * @see com.novus.casbah.mongodb.Implicits
 */
trait FluidQueryBarewordOps extends SetOp 
                               with UnsetOp
                               with IncOp


/**
 * Trait to provide the $set (Set) Set method on appropriate callers.
 * 
 * Also provides a reversed version so you can bareword it such as:
 * 
 * $set ("Foo" -> "bar")
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait SetOp extends BarewordQueryOperator {
  implicit def $set = apply[(String, Any)]("$set")_
}

/**
 * Trait to provide the $unset (UnSet) UnSet method on appropriate callers.
 *
 * Also provides a reversed version so you can bareword it such as:
 * 
 * $unset "foo"
 *
 * Targets an RValue of String*, where String are field names to be converted to a  DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 */
trait UnsetOp extends BarewordQueryOperator {
  implicit def $unset = apply[String]("$unset")_
}


/** 
 * Trait to provide the $unset (UnSet) UnSet method on appropriate callers.
 *
 * Also provides a reversed version so you can bareword it such as:
 * 
 * $inc ("foo" -> 5)
 *
 * Targets an RValue of (String, Numeric)* to be converted to a  DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0, 06/17/10
 * @since 1.0
 */
trait IncOp extends BarewordQueryOperator {
  //implicit def $inc[T: Manifest]()(implicit numeric: Numeric[T]) = apply[T]("$inc", DefaultRValue(1))_
}

/*
[>*
 * Trait to provide the $inc (Inc) increment method on appropriate callers.
 *
 * Targets (takes a right-hand value of) DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 <]
trait IncOp extends QueryOperator {
  def $inc(target: DBObject) = op("$inc", target)
}

trait ArrayOps extends PushOp
                  with PushAllOp
                  with AddToSetOp
                  with PopOp
                  with PullOp
                  with PullAllOp

[>*
 * Trait to provide the $push (push) push method on appropriate callers.
 *
 * Targets (takes a right-hand value of) DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 <]
trait PushOp extends QueryOperator {
  def $push(target: DBObject) = op("$push", target)
}

[>*
 * Trait to provide the $pushAll (pushAll) pushAll method on appropriate callers.
 *
 * Targets (takes a right-hand value of) DBObject  
 *
 * TODO: Value of the dbobject should be an array - verify target
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 <]
trait PushAllOp extends QueryOperator {
  def $pushAll(target: DBObject) = op("$pushAll", target)
}

[>*
 * Trait to provide the $addToSet (addToSet) addToSet method on appropriate callers.
 *
 * Targets (takes a right-hand value of) DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 <]
trait AddToSetOp extends QueryOperator {
  def $addToSet(target: DBObject) = op("$addToSet", target)
}

[>*
 * Trait to provide the $pop (pop) pop method on appropriate callers.
 *
 * Targets (takes a right-hand value of) DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 <]
trait PopOp extends QueryOperator {
  def $pop(target: DBObject) = op("$pop", target)
}

[>*
 * Trait to provide the $push (push) push method on appropriate callers.
 *
 * Targets (takes a right-hand value of) DBObject  
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 <]
trait PullOp extends QueryOperator {
  def $pull(target: DBObject) = op("$pull", target)
}

[>*
 * Trait to provide the $pushAll (pushAll) pushAll method on appropriate callers.
 *
 * Targets (takes a right-hand value of) DBObject  
 *
 * TODO: Value of the dbobject should be an array - verify target
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0
 <]
trait PullAllOp extends QueryOperator {
  def $pullAll(target: DBObject) = op("$pullAll", target)
}
*/

// vim: set ts=2 sw=2 sts=2 et:
