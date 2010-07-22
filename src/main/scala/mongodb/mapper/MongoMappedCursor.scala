package com.novus.casbah
package mongodb
package mapper

import Implicits._
import com.mongodb._

class MongoMappedCursor[P <: AnyRef : Manifest] protected[mapper](val underlying: DBCursor) extends MongoCursorWrapper[DBObject]  {
  private val mapper = Mapper[P]

  def batchSize(n: Int) = underlying.batchSize(n) asScalaMapped
  def copy() = underlying.copy asScalaMapped
  def hint(indexKeys: DBObject) = underlying.hint(indexKeys) asScalaMapped
  def hint(indexName: String) = underlying.hint(indexName) asScalaMapped
  def limit(n: Int) = underlying.limit(n) asScalaMapped
  def skip(n: Int) = underlying.skip(n) asScalaMapped
  def snapshot() = underlying.snapshot() asScalaMapped
  def sort(orderBy: DBObject) = underlying.sort(orderBy) asScalaMapped
  override def toString() =  "MongoMappedCursor{Iterator[_] with %d objects.}".format(count)
}
