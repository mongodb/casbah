/**
 * Copyright (c) 2010, 2011 10gen, Inc. <http://10gen.com>
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

package com.mongodb.casbah.query.dsl
package light

import com.mongodb.casbah.util.Logging

import com.mongodb.casbah.query._

import scalaj.collection.Imports._


/**
 * Aggregation object for Bareword Operators.
 * Bareword operators stand on their own - they lack the requirement for an LValue.
 * This mixes them in so they can be pulled down in a single import.
 *
 * Typically, you want to follow the model Implicits does, and mix this in
 * if you want to use it but not import Implicits
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 * @see com.mongodb.casbah.Implicits
 */
trait LightFluidQueryBarewordOps extends LightSetOp
                              with LightUnsetOp
                              with LightIncOp
                              with LightOrOp
                              with LightAndOp
                              with LightRenameOp
                              with LightArrayOps
                              with LightNorOp
                              with LightBitOp

trait LightArrayOps extends LightPushOp
                with LightPushAllOp
                with LightAddToSetOp
                with LightPopOp
                with LightPullOp
                with LightPullAllOp

/**
 * trait to provide the set (Set) Set method as a bareword operator.
 *
 * set ("Foo" -> "bar")
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24set
 */
trait LightSetOp extends SetOpBase {
  def set = _set
}

/**
 * trait to provide the unset (UnSet) UnSet method as a bareword operator..
 *
 * unset ("foo")
 *
 * Targets an RValue of String*, where String are field names to be converted to a  DBObject
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24unset
 */
trait LightUnsetOp extends UnsetOpBase {
  def unset = _unset _
}

/**
 * trait to provide the inc (inc) method as a bareword operator..
 *
 *   inc ("foo" -> 5)
 *
 * Targets an RValue of (String, ValidNumericType)* to be converted to a  DBObject
 *
 * Due to a quirk in the way I implemented type detection this fails if you mix ValidNumericType types.  E.g. floats work, but not mixing floats and ints.
 * This can be easily circumvented if you want 'ints' with floats by making your ints floats with .0:
 *
 *   inc ("foo" -> 5.0, "bar" -> 1.6)
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24inc
 */
trait LightIncOp extends IncOpBase {
  def inc[T: ValidNumericType](args: (String,  T)*): DBObject = _inc(args: _*)
}


trait LightPushOpBase extends BarewordQueryOperator {
  protected def _push = apply[Any]("push")_
}

/*
 * trait to provide the push (push) method as a bareword operator.
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * If Field exists but is not an array an error will occur
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24push
 *
 */
trait LightPushOp extends PushOpBase {
  def push = _push
}

/*
 * trait to provide the pushAll (pushAll) method as a bareword operator..
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a  DBObject
 *
 * RValue MUST Be an array - otherwise use push.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pushAll
 */
trait LightPushAllOp extends PushAllOpBase {
  def pushAll[A <: Any: Manifest](args: (String, A)*): DBObject = _pushAll(args: _*)
}

/*
 * trait to provide the addToSet (addToSet) method as a bareword operator..
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * Can also combined with the each operator for adding many values:
 *
 *   scala> addToSet ("foo") each (5, 10, 15, "20"))
 *  res6: com.mongodb.casbah.commons.Imports.DBObject = { "addToSet" : { "foo" : { "each" : [ 5 , 10 , 15 , "20"]}}}
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24addToSet
 */
trait LightAddToSetOp extends AddToSetOpBase {
  def addToSet[T <% DBObject](arg: T): DBObject = _addToSet(arg)
  /* $each-able */
  def addToSet(field: String) = {
    /**
     * Special query operator only available on the right-hand side of an
     * $addToSet which takes a list of values.
     *
     * Slightly hacky to prevent it from returning unless completed with a $each
     *
     * THIS WILL NOT WORK IN MONGOD ANYWHERE BUT INSIDE AN ADDTOSET
     *
     * @author Brendan W. McAdams <brendan@10gen.com>
     * @since 2.0
     * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24addToSet
     */
    new {
      protected def op(target: Any) =
        MongoDBObject("$addToSet" -> MongoDBObject(field -> MongoDBObject("$each" -> target)))

      def each(target: Array[Any]) = op(target.toList)
      def each(target: Any*) =
        if (target.size > 1)
          op(target.toList)
        else if (!target(0).isInstanceOf[Iterable[_]] &&
          !target(0).isInstanceOf[Array[_]])
          op(List(target(0)))
        else op(target(0))
    }
  }
  def addToSet = _addToSet
}

/*
 * trait to provide the pop (pop) method as a bareword operator..
 *
 * If Field exists but is not an array an error will occurr.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pop
 */
trait LightPopOp extends PopOpBase {
  def pop[T: ValidNumericType](args: (String, T)*) = _pop(args: _*)
}

/*
 * trait to provide the pull (pull) method as a bareword operator..
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * If Field exists but is not an array an error will occurr.
 *
 * Pull is special as defined in the docs and needs to allow operators on fields.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pull
 */
trait LightPullOp extends PullOpBase {
  def pull = apply[Any]("pull")_
  def pull(inner: => DBObject): DBObject = _pull(inner)
  def pull(inner: DBObject): DBObject = _pull(inner)
}

/*
 * trait to provide the pullAll (pullAll) method as a bareword operator..
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a  DBObject
 *
 * RValue MUST Be an array - otherwise use pull.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pullAll
 */
trait LightPullAllOp extends PullAllOpBase {
  def pullAll[A <: Any: Manifest](args: (String, A)*): DBObject = _pullAll(args: _*)
}

/**
 * trait to provide the and method as a bareword operator.
 *
 * and ("Foo" -> "bar")
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * @author Ben Gamari <bgamari.foss@gmail.com>
 * @since 3.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24and
 */
trait LightAndOp extends AndOpBase {
  def and = _and
}

/**
 * trait to provide the or method as a bareword operator.
 *
 * or ("Foo" -> "bar")
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24or
 */
trait LightOrOp extends OrOpBase {
  def or = _or
}

/**
 * trait to provide the rename (Rename field) as a bareword operator
 *
 * Targets (takes a right-hand value of) a DBObject or a Tuple of (String, String)
 *
 * WORKS ONLY IN MONGODB 1.7.2+
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24rename
 *
 */
trait LightRenameOp extends RenameOpBase {
  def rename = _rename
}

/**
 * trait to provide the nor (nor ) method as a bareword operator
 *
 * Nor is a combination of not and or with no left anchor
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a  DBObject
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24nor
 */
trait LightNorOp extends NorOpBase {
  def nor = _nor
}

/**
 * trait to provide the bit (bit) update method as a bareword Operator
 *
 * Bit does a bitwise operation either AND or OR against a given field or set of fields
 * with no left anchor.
 *
 * Targets an RValue of {field: {and|or: integer}}.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.1.1
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24bit
 */
trait LightBitOp extends BitOpBase {
  def bit(field: String) = _bit(field)
}

// vim: set ts=2 sw=2 sts=2 et:
