/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
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
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/mongodb/casbah
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
 * @since 1.0
 * @see SetOp
 */
trait BarewordQueryOperator {

  /*
   * TODO - Implicit filtering of 'valid' (aka convertible) types for [A]
   */

  def apply[A](oper: String)(fields: Seq[(String, A)]): DBObject = {
    val bldr = MongoDBObject.newBuilder
    for ((k, v) <- fields) bldr += k -> v
    MongoDBObject(oper -> bldr.result.asDBObject)
  }

}

class NestedBarewordListOperator(oper: String) {

  def apply[A: ValidBarewordExpressionArgType](fields: A*): DBObject = {
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
 * @since 1.0
 * @see com.mongodb.casbah.Implicits
 */
trait FluidQueryBarewordOps extends SetOp
with SetOnInsertOp
with UnsetOp
with IncOp
with OrOp
with AndOp
with RenameOp
with ArrayOps
with NorOp
with BitOp
with WhereOp

trait ArrayOps extends PushOp
with PushAllOp
with AddToSetOp
with PopOp
with PullOp
with PullAllOp

/**
 * Trait to provide the \$set (Set) Set method as a bareword operator.
 *
 * {{{ \$set ("Foo" -> "bar") }}}
 *
 * Targets an RValue of (String, Any)* to be converted to a DBObject
 *
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24set
 */
trait SetOp extends BarewordQueryOperator {
  def $set[A](fields: (String, A)*): DBObject = apply[A]("$set")(fields)
}

/**
 * Trait to provide the \$setOnInsert (SetOnInsert) SetOnInsert method as a
 * bareword operator.
 *
 * {{{ \$setOnInsert ("Foo" -> "bar") }}}
 *
 * Targets an RValue of (String, Any)* to be converted to a DBObject
 *
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24set
 */
trait SetOnInsertOp extends BarewordQueryOperator {
  def $setOnInsert[A](fields: (String, A)*): DBObject = apply[A]("$setOnInsert")(fields)
}

/**
 * Trait to provide the \$unset (UnSet) UnSet method as a bareword operator..
 *
 * {{{ \$unset ("foo") }}}
 *
 * Targets an RValue of String*, where String are field names to be converted to a DBObject
 *
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24unset
 */
trait UnsetOp extends BarewordQueryOperator {
  def $unset(args: String*): DBObject =
    apply[Int]("$unset")(Seq(args.map(_ -> 1): _*))
}

/**
 * Trait to provide the \$inc (inc) method as a bareword operator..
 *
 * {{{ \$inc ("foo" -> 5) }}}
 *
 * Targets an RValue of (String, ValidNumericType)* to be converted to a DBObject
 *
 * Due to a quirk in the way I implemented type detection this fails if you mix ValidNumericType types.
 * E.g. floats work, but not mixing floats and ints. This can be easily circumvented
 * if you want 'ints' with floats by making your ints floats with .0:
 *
 * {{{ \$inc ("foo" -> 5.0, "bar" -> 1.6) }}}
 *
 * @since 1.0
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24inc
 */
trait IncOp extends BarewordQueryOperator {
  def $inc[T: ValidNumericType](args: (String, T)*): DBObject = apply[T]("$inc")(args)
}

/*
 * Trait to provide the \$push (push) method as a bareword operator.
 *
 * Targets an RValue of (String, Any)* to be converted to a DBObject
 *
 * If Field exists but is not an array an error will occur
 *
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24push
 *
 */
trait PushOp extends BarewordQueryOperator {
  def $push[A](fields: (String, A)*): DBObject = apply[A]("$push")(fields)

  def $push(field: String) = {
    /**
     * Special query operator only available on the right-hand side of an
     * \$push which takes a list of values.
     *
     * Slightly hacky to prevent it from returning unless completed with a \$each
     *
     * @since 2.6.2
     * @see http://www.mongodb.org/display/DOCS/Updating
     */
    new {
      protected def eachOp(target: Any) =
        MongoDBObject("$push" -> MongoDBObject(field -> MongoDBObject("$each" -> target)))

      def $each[A: AsQueryParam](target: A*) = eachOp(target)
    }
  }
}

/*
 * Trait to provide the \$pushAll (pushAll) method as a bareword operator..
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a DBObject
 *
 * RValue MUST Be an array - otherwise use push.
 *
 *
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pushAll
 */
trait PushAllOp extends BarewordQueryOperator {
  def $pushAll[A: AsQueryParam](args: (String, A)*): DBObject =
    apply("$pushAll")(Seq(args.map(z => z._1 -> AsQueryParam[A].asQueryParam(z._2)): _*))
}

/*
 * Trait to provide the \$addToSet (addToSet) method as a bareword operator..
 *
 * Targets an RValue of (String, Any)* to be converted to a DBObject
 *
 * Can also combined with the \$each operator for adding many values:
 *
 * {{{
 *  scala> \$addToSet ("foo") \$each (5, 10, 15, "20"))
 *  res1: com.mongodb.casbah.commons.Imports.DBObject = { "\$addToSet" : { "foo" : { "\$each" : [ 5 , 10 , 15 , "20"]}}}
 * }}}
 *
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24addToSet
 */
trait AddToSetOp extends BarewordQueryOperator {
  def $addToSet[T <% DBObject](arg: T): DBObject = MongoDBObject("$addToSet" -> arg)

  def $addToSet[A](fields: (String, A)*): DBObject = apply[A]("$addToSet")(fields)

  def $addToSet(field: String) = {
    /**
     * Special query operator only available on the right-hand side of an
     * \$addToSet which takes a list of values.
     *
     * Slightly hacky to prevent it from returning unless completed with a \$each
     *
     * @since 2.0
     * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24addToSet
     */
    new {
      protected def op(target: Any) =
        MongoDBObject("$addToSet" -> MongoDBObject(field -> MongoDBObject("$each" -> target)))

      def $each[A: AsQueryParam](target: A*) = op(target)
    }
  }

}

/*
 * Trait to provide the \$pop (pop) method as a bareword operator..
 *
 * If Field exists but is not an array an error will occurr.
 *
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pop
 */
trait PopOp extends BarewordQueryOperator {
  def $pop[T: ValidNumericType](args: (String, T)*) = apply[T]("$pop")(Seq(args: _*))
}

/*
 * Trait to provide the \$pull (pull) method as a bareword operator..
 *
 * Targets an RValue of (String, Any)* to be converted to a DBObject
 *
 * If Field exists but is not an array an error will occurr.
 *
 * Pull is special as defined in the docs and needs to allow operators on fields.
 *
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pull
 */
trait PullOp extends BarewordQueryOperator {
  def $pull[A](fields: (String, A)*) = apply[Any]("$pull")(fields)

  def $pull(inner: => DBObject): DBObject = MongoDBObject("$pull" -> inner)

  def $pull(inner: DBObject): DBObject = MongoDBObject("$pull" -> inner)
}

/*
 * Trait to provide the \$pullAll (pullAll) method as a bareword operator..
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a DBObject
 *
 * RValue MUST Be an array - otherwise use pull.
 *
 *
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24pullAll
 */
trait PullAllOp extends BarewordQueryOperator {
  def $pullAll[A: AsQueryParam](args: (String, A)*): DBObject =
    apply("$pullAll")(Seq(args.map(z => z._1 -> AsQueryParam[A].asQueryParam(z._2)): _*))
}

/**
 * Trait to provide the \$and method as a bareword operator.
 *
 * {{{ \$and ("Foo" -> "bar") }}}
 *
 * Targets an RValue of (String, Any)* to be converted to a DBObject
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24and
 */
trait AndOp {
  def $and = new NestedBarewordListOperator("$and")
}

/**
 * Trait to provide the \$or method as a bareword operator.
 *
 * {{{ \$or ("Foo" -> "bar") }}}
 *
 * Targets an RValue of (String, Any)* to be converted to a DBObject
 *
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24or
 */
trait OrOp {
  def $or = new NestedBarewordListOperator("$or")
}

/**
 * Trait to provide the \$rename (Rename field) as a bareword operator
 *
 * Targets (takes a right-hand value of) a DBObject or a Tuple of (String, String)
 *
 * WORKS ONLY IN MONGODB 1.7.2+
 *
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24rename
 *
 */
trait RenameOp extends BarewordQueryOperator {
  def $rename[A](fields: (String, A)*) = apply[Any]("$rename")(fields)
}

/**
 * Trait to provide the \$nor (nor) method as a bareword operator
 *
 * Nor is a combination of \$not and \$or with no left anchor
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a DBObject
 *
 * @since 2.0
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24nor
 */
trait NorOp {
  def $nor = new NestedBarewordListOperator("$nor")
}

/**
 * Trait to provide the \$bit (bit) update method as a bareword Operator
 *
 * Bit does a bitwise operation either AND or OR against a given field or set of fields
 * with no left anchor.
 *
 * Targets an RValue of {field: {and|or: integer}}.
 *
 * @since 2.1.1
 * @see http://www.mongodb.org/display/DOCS/Updating#Updating-%24bit
 */
trait BitOp extends BarewordQueryOperator {
  def $bit(field: String) = {
    new {
      protected def op(oper: String, target: Any) =
        MongoDBObject("$bit" -> MongoDBObject(field -> MongoDBObject(oper -> target)))

      def and[T: ValidNumericType](target: T) = op("and", target)

      def or[T: ValidNumericType](target: T) = op("or", target)
    }
  }
}

/**
 * Trait to provide the \$where (Where) method on appropriate callers.
 *
 * Targets (takes a right-hand value of) JSFunction [which is currently just as string containing a javascript function]
 *
 * @see http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-JavascriptExpressionsand%7B%7B%24where%7D%7D
 */
trait WhereOp extends BarewordQueryOperator {
  def $where(target: JSFunction) = MongoDBObject("$where" -> target)
}
