/**
 * Copyright (c) 2010 - 2012 10gen, Inc. <http://10gen.com>
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

import com.mongodb.casbah.commons.Logging

import com.mongodb.casbah.query.Imports._

import scala.collection.JavaConverters._

/** 
 * Base Operator class for Bareword Operators.
 * 
 * Bareword operators stand on their own - they lack the requirement for an LValue.
 * 
 * Operator implementations (see SetOp for an example) should partially apply with just their operator name.
 * The apply method's type parameter can be used to restrict the valid RValue values at will.  
 * 
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 * @see SetOp
 */
trait BarewordQueryOperator {

  /*
   * TODO - Implicit filtering of 'valid' (aka convertible) types for [A]
   */
  def apply[A](oper: String)(fields: (String, A)*): DBObject = {
    val bldr = MongoDBObject.newBuilder
    for ((k, v) <- fields) bldr += k -> v
    MongoDBObject(oper -> bldr.result.asDBObject)
  }

}

class NestedBarewordListOperator(oper: String) {

  def apply[A : ValidBarewordExpressionArgType](fields: A*): DBObject = {
    val b = Seq.newBuilder[DBObject]
    fields.foreach(x => b += implicitly[ValidBarewordExpressionArgType[A]].toDBObject(x))
    apply(b.result(): Seq[DBObject])
  }
  
  def apply(list: Seq[DBObject]): DBObject = {
    MongoDBObject(oper -> list)
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
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 * @see com.mongodb.casbah.Implicits
 */
trait FluidQueryBarewordOps extends SetOp
  with UnsetOp
  with IncOp
  with OrOp
  with AndOp
  with RenameOp
  with ArrayOps
  with NorOp
  with BitOp

trait ArrayOps extends PushOp
                  with PushAllOp
                  with AddToSetOp
                  with PopOp
                  with PullOp
                  with PullAllOp

trait SetOpBase extends BarewordQueryOperator {
  protected def _set = apply[Any]("$set")_
}

/**
 * Trait to provide the $set (Set) Set method as a bareword operator.
 *
 * $set ("Foo" -> "bar")
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24set
 */
trait SetOp extends SetOpBase {
  def $set = _set
}

trait UnsetOpBase extends BarewordQueryOperator {
  protected def _unset(args: String*): DBObject = apply("$unset")(args.map(_ -> 1): _*)
}

/**
 * Trait to provide the $unset (UnSet) UnSet method as a bareword operator..
 *
 * $unset ("foo")
 *
 * Targets an RValue of String*, where String are field names to be converted to a  DBObject
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24unset
 */
trait UnsetOp extends UnsetOpBase {
  def $unset = _unset _
}

trait IncOpBase extends BarewordQueryOperator {
  protected def _inc[T: ValidNumericType](args: (String, T)*): DBObject = apply[T]("$inc")(args: _*)
}

/**
 * Trait to provide the $inc (inc) method as a bareword operator..
 *
 *   $inc ("foo" -> 5)
 *
 * Targets an RValue of (String, ValidNumericType)* to be converted to a  DBObject
 *
 * Due to a quirk in the way I implemented type detection this fails if you mix ValidNumericType types.  E.g. floats work, but not mixing floats and ints.
 * This can be easily circumvented if you want 'ints' with floats by making your ints floats with .0:
 *
 *   $inc ("foo" -> 5.0, "bar" -> 1.6)
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24inc
 */
trait IncOp extends IncOpBase {
  def $inc[T: ValidNumericType](args: (String,  T)*): DBObject = _inc(args: _*)
}


trait PushOpBase extends BarewordQueryOperator {
  protected def _push = apply[Any]("$push")_
}

/*
 * Trait to provide the $push (push) method as a bareword operator.
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * If Field exists but is not an array an error will occur
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24push
 *
 */
trait PushOp extends PushOpBase {
  def $push = _push
}

trait PushAllOpBase extends BarewordQueryOperator {
  protected def _pushAll[A : AsIterable](args: (String, A)*): DBObject =
    apply("$pushAll")(args.map(z => z._1 -> AsIterable[A].asIterable(z._2)) :_*)
}

/*
 * Trait to provide the $pushAll (pushAll) method as a bareword operator..
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a  DBObject
 *
 * RValue MUST Be an array - otherwise use push.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pushAll
 */
trait PushAllOp extends PushAllOpBase {
  def $pushAll[A : AsIterable](args: (String, A)*): DBObject = _pushAll(args: _*)
}

trait AddToSetOpBase extends BarewordQueryOperator {
  protected def _addToSet[T <% DBObject](arg: T): DBObject =
    MongoDBObject("$addToSet" -> arg)


  /* $each-able */
  protected def _addToSet(field: String) = {
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

      def $each(target: Array[Any]) = op(target.toList)
      def $each(target: Any*) =
        if (target.size > 1)
          op(target.toList)
        else if (!target(0).isInstanceOf[Iterable[_]] &&
          !target(0).isInstanceOf[Array[_]])
          op(List(target(0)))
        else op(target(0))
    }
  }
  protected def _addToSet = apply[Any]("$addToSet")_

}

/*
 * Trait to provide the $addToSet (addToSet) method as a bareword operator..
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * Can also combined with the $each operator for adding many values:
 *
 *   scala> $addToSet ("foo") $each (5, 10, 15, "20"))
 *  res6: com.mongodb.casbah.commons.Imports.DBObject = { "$addToSet" : { "foo" : { "$each" : [ 5 , 10 , 15 , "20"]}}}
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24addToSet
 */
trait AddToSetOp extends AddToSetOpBase {
  def $addToSet[T <% DBObject](arg: T): DBObject = _addToSet(arg)
  def $addToSet(field: String) = _addToSet(field)
  def $addToSet = _addToSet
}

trait PopOpBase extends BarewordQueryOperator {
  protected def _pop[T: ValidNumericType](args: (String, T)*) = apply[T]("$pop")(args: _*)
}

/*
 * Trait to provide the $pop (pop) method as a bareword operator..
 *
 * If Field exists but is not an array an error will occurr.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pop
 */
trait PopOp extends PopOpBase {
  def $pop[T: ValidNumericType](args: (String, T)*) = _pop(args: _*)
}

trait PullOpBase extends BarewordQueryOperator {
  protected def _pull = apply[Any]("$pull")_
  /** ValueTest enabled version */
  protected def _pull(inner: => DBObject): DBObject = MongoDBObject("$pull" -> inner)
  protected def _pull(inner: DBObject): DBObject = MongoDBObject("$pull" -> inner)
}

/*
 * Trait to provide the $pull (pull) method as a bareword operator..
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
trait PullOp extends PullOpBase {
  def $pull = apply[Any]("$pull")_
  def $pull(inner: => DBObject): DBObject = _pull(inner)
  def $pull(inner: DBObject): DBObject = _pull(inner)
}

trait PullAllOpBase extends BarewordQueryOperator {
  protected def _pullAll[A : AsIterable](args: (String, A)*): DBObject =
    apply("$pullAll")(args.map(z => z._1 -> AsIterable[A].asIterable(z._2)): _*)    
}

/*
 * Trait to provide the $pullAll (pullAll) method as a bareword operator..
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a  DBObject
 *
 * RValue MUST Be an array - otherwise use pull.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pullAll
 */
trait PullAllOp extends PullAllOpBase {
  def $pullAll[A : AsIterable](args: (String, A)*): DBObject = _pullAll(args: _*)
}

trait AndOpBase {
  protected def _and = new NestedBarewordListOperator("$and")
}

/**
 * Trait to provide the $and method as a bareword operator.
 *
 * $and ("Foo" -> "bar")
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * @author Ben Gamari <bgamari.foss@gmail.com>
 * @since 3.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24and
 */
trait AndOp extends AndOpBase {
  def $and = _and
}

trait OrOpBase {
  protected def _or = new NestedBarewordListOperator("$or")
}

/**
 * Trait to provide the $or method as a bareword operator.
 *
 * $or ("Foo" -> "bar")
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24or
 */
trait OrOp extends OrOpBase {
  def $or = _or
}


trait RenameOpBase extends BarewordQueryOperator {
  protected def _rename = apply[Any]("$rename")_
}

/** 
 * Trait to provide the $rename (Rename field) as a bareword operator
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
trait RenameOp extends RenameOpBase {
  def $rename = _rename
}

trait NorOpBase {
  protected def _nor = new NestedBarewordListOperator("$nor")
}

/**
 * Trait to provide the $nor (nor ) method as a bareword operator
 *
 * Nor is a combination of $not and $or with no left anchor
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a  DBObject  
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24nor
 */
trait NorOp extends NorOpBase {
  def $nor = _nor
}

trait BitOpBase extends BarewordQueryOperator {
  protected def _bit(field: String) = {
    new {
      protected def op(oper: String, target: Any) =
        MongoDBObject("$bit" -> MongoDBObject(field -> MongoDBObject(oper -> target)))

      def and[T: ValidNumericType](target: T) = op("and", target)
      def or[T: ValidNumericType](target: T) = op("or", target)
    }
  }
}

/**
 * Trait to provide the $bit (bit) update method as a bareword Operator
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
trait BitOp extends BitOpBase {
  def $bit(field: String) = _bit(field)
}
// vim: set ts=2 sw=2 sts=2 et:
