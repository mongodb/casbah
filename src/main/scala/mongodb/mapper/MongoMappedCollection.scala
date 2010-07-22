package com.novus.casbah
package mongodb
package mapper

import Implicits._
import com.mongodb._

class MongoMappedCollection[P <: AnyRef : Manifest](val underlying: DBCollection) extends Iterable[P] with MongoCollectionWrapper {

  val m = manifest[P]
  val mapper = Mapper[P]

  underlying.setObjectClass(m.erasure)

  override def elements = find
  override def iterator = find

  implicit def dbo2p(dbo: DBObject): P = mapper.from_dbo(dbo)

  def find() = underlying.find.asScalaTyped
  def find(ref: DBObject) = underlying.find(ref) asScalaTyped
  def find(ref: DBObject, keys: DBObject) = underlying.find(ref, keys) asScalaTyped
  def findOne() = optWrap(underlying.findOne())
  def findOne(o: DBObject) = optWrap(underlying.findOne(o))
  def findOne(o: DBObject, fields: DBObject) = optWrap(underlying.findOne(o, fields))
  def findOne(obj: Object) = optWrap(underlying.findOne(obj))
  def findOne(obj: Object, fields: DBObject) = optWrap(underlying.findOne(obj, fields))
  override def head = mapper.from_dbo(findOne.get)
  override def headOption = Some(findOne.get)
  override def tail = find.skip(1).toList
}
