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
class DynamicDBObject protected[mongodb](val underlying: DBObject = new BasicDBObject) extends MongoDBObject with MongoDynamic {
  override def applyDynamic(name: String)(args: Any*): MongoDynamic = defaultCaster(getAs[Any](name))

  override def typed[A : Manifest]: Option[A] =  if (manifest[A] == manifest[DBObject]) Some(underlying.asInstanceOf[A]) else None

  /**
   * Partial function -- override in your own custom class to tack on behavior
   */
  protected def defaultCaster: PartialFunction[Option[Any], MongoDynamic] = {
    case null | None => EmptyMongoDynamic
    case Some(b: Byte) => ValueMongoDynamic(b)
    case Some(s: Short) => ValueMongoDynamic(s)
    case Some(i: Int) => ValueMongoDynamic(i)
    case Some(l: Long) => ValueMongoDynamic(l)
    case Some(f: Float) => ValueMongoDynamic(f)
    case Some(d: Double) => ValueMongoDynamic(d)
    case Some(b: Boolean) => ValueMongoDynamic(b)
    case Some(s: String) => ValueMongoDynamic(s)
    case Some(oid: ObjectId) => ValueMongoDynamic(oid)
    case Some(dbo: DBObject) => new DynamicDBObject(dbo)
  }
}

object DynamicDBObject {
  def empty: DynamicDBObject = new DynamicDBObject

  def apply[A <: String, B <: Any](elems: (A, B)*): DynamicDBObject = (newBuilder[A, B] ++= elems).result
  def apply[A <: String, B <: Any](elems: List[(A, B)]): DynamicDBObject = apply(elems: _*)

  def newBuilder[A <: String, B <: Any]: DynamicDBObjectBuilder = new DynamicDBObjectBuilder
}


sealed class DynamicDBObjectBuilder extends scala.collection.mutable.Builder[(String, Any), DynamicDBObject] {
  import com.mongodb.BasicDBObjectBuilder

  protected val empty = BasicDBObjectBuilder.start
  protected var elems = empty
  override def +=(x: (String, Any)) = {
    elems.add(x._1, x._2)
    this
  }

  def clear() { elems = empty }
  def result(): DynamicDBObject = new DynamicDBObject(elems.get)
}

object EmptyMongoDynamic extends MongoDynamic {
  override def applyDynamic(name: String)(args: Any*): MongoDynamic = EmptyMongoDynamic
  override def typed[A : Manifest]: Option[A] = None
}

case class ValueMongoDynamic[V : Manifest](value: V) extends MongoDynamic {
  override def applyDynamic(name: String)(args: Any*): MongoDynamic = EmptyMongoDynamic
  override def typed[A : Manifest]: Option[A] =
    if (manifest[V] == manifest[A])
      Some(value.asInstanceOf[A])
    else
      None
}
