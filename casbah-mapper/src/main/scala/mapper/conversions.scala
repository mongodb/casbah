package com.novus.casbah.mapper.conversions

import com.novus.casbah.Imports._
import com.novus.casbah.mapper.{Mapper, MapperUtils}
import com.novus.casbah.conversions.MongoConversionHelper
import org.bson.{BSON, Transformer}

object RegisterMapperConversionHelpers extends MapperHelpers {
  def apply() = super.register()
}

object DeregisterMapperConversionHelpers extends MapperHelpers {
  def apply() = super.unregister()
}

trait MapperHelpers extends MapperSerializer

trait MapperSerializer extends MongoConversionHelper {
  private val transformer = new Transformer {
    def transform(o: AnyRef): AnyRef = o match {
      case _ => Mapper(o.getClass.getName) match {
        case Some(mapper) => mapper.asInstanceOf[Mapper[AnyRef]].asDBObject(o)
        case _ => o
      }
    }
  }

  override def register() = {
    BSON.addEncodingHook(classOf[AnyRef], transformer)
    super.register()
  }

  override def unregister() = {
    org.bson.BSONEncoders.remove(classOf[AnyRef])
    super.unregister()
  }
}
