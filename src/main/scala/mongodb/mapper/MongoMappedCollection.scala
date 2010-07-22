package com.novus.casbah
package mongodb
package mapper

import Implicits._
import com.mongodb._

class MongoMappedCollection[P <: AnyRef : Manifest](val underlying: DBCollection) extends Iterable[P] with MongoCollectionWrapper with MapperImplicits[P] {

  val m = manifest[P]
  val mapper = Mapper[P]

  override def elements = find
  override def iterator = find

  def find() = underlying.find.asScalaMapped
  def find(ref: DBObject) = underlying.find(ref) asScalaMapped
  def find(ref: DBObject, keys: DBObject): MongoMappedCursor[P] =
    underlying.find(ref, keys) asScalaMapped

  def findOne(): Option[P] = optWrap(underlying.findOne())
  def findOne(o: DBObject): Option[P] = optWrap(underlying.findOne(o))
  def findOne(o: DBObject, fields: DBObject): Option[P] =
    optWrap(underlying.findOne(o, fields))
  def findOne(obj: Object): Option[P] = optWrap(underlying.findOne(obj))
  def findOne(obj: Object, fields: DBObject): Option[P] = optWrap(underlying.findOne(obj, fields))

  override def head: P = findOne.get
  override def headOption: Option[P] = Some(findOne.get)
  override def tail: Seq[P] = find.skip(1).toList

  override def toString = "MongoMappedCollection(%s)(%s)".format(m.erasure.getSimpleName, mapper)
}
