package com.novus.casbah
package mongodb
package mapper

import java.lang.reflect.Method
import java.lang.annotation.Annotation
import java.beans.{Introspector, PropertyDescriptor}

import scala.reflect.{BeanInfo, Manifest}
import scala.collection.JavaConversions._

import annotations.raw._
import util.Logging
import Implicits._

import org.bson.types.ObjectId

object Mapper {
  private val _m = new java.util.concurrent.ConcurrentHashMap[Class[_], Mapper[_,_]]

  def apply[P <: AnyRef : Manifest](): Mapper[AnyRef, P] = apply[P](manifest[P].erasure.asInstanceOf[Class[P]])(manifest[P])

  def apply[P <: AnyRef : Manifest](klass: Class[P]) = {
    if (_m.containsKey(klass)) _m.get(klass).asInstanceOf[Mapper[AnyRef, P]]
    else if (klass.isAnnotationPresent(classOf[MappedBy])) {
      val svc = klass.getAnnotation(classOf[MappedBy]).value().newInstance.asInstanceOf[Mapper[AnyRef, P]]
      _m.put(klass, svc)
      svc
    } else {
      throw new Exception("please annotate " + klass + " with @MappedBy")
    }
  }
}

abstract class Mapper[I <: AnyRef : Manifest, P <: AnyRef : Manifest]() extends Logging {
  import Mapper._
  import MapperUtils._

  protected val id_klass = manifest[I].erasure.asInstanceOf[Class[I]]
  protected val obj_klass = manifest[P].erasure.asInstanceOf[Class[P]]

  implicit protected def s2db(name: String): MongoDB = conn(name)
  implicit protected def s2coll(name: String): MongoCollection = db(name)

  var conn: MongoConnection = _
  var db  : MongoDB         = _
  var coll: MongoCollection = _

  lazy val info = Introspector.getBeanInfo(obj_klass)

  lazy val allProps =
    info.getPropertyDescriptors.filter {
      prop => (isAnnotatedWith_?(prop, classOf[ID]) || isAnnotatedWith_?(prop, classOf[Key]))
    }.toSet

  lazy val idProp =
    (allProps.filter(isIdProp_? _)).toList match {
      case List(prop) => prop
      case Nil => throw new Exception("no @ID on " + obj_klass)
      case _ => throw new Exception("more than one @ID on " + obj_klass)
    }

  lazy val nonIdProps = allProps - idProp

  lazy val isAutoId_? = getAnnotation(idProp, classOf[ID]).get.auto

  override def toString =
    "Mapper(%s -> idProp: %s, is_auto_id: %s, allProps: %s)".format(
      obj_klass.getSimpleName, idProp.getName, isAutoId_?,
      allProps.map(p =>
        "Prop(%s -> %s, is_option: %s)".format(p.getName,
                                               getPropType(p),
                                               isOptionProp_?(p)))
    )

  def getPropNamed(key: String) =
    nonIdProps.filter(_.getName == key).toList match {
      case List(prop) => Some(prop)
      case _ => None
    }

  def getPropValue[V <: AnyRef : Manifest](o: AnyRef, prop: PropertyDescriptor): Option[V] = {
    val cv = manifest[V].erasure.asInstanceOf[Class[V]]
    def getPropValue0(p: AnyRef): Option[V] =
      prop.getReadMethod.invoke(p) match {
        case v if v == null => None
        case v if cv.isAssignableFrom(v.getClass) => Some(cv.cast(v))
        case _ => None
      }

    o match {
      case None => None
      case Some(p) => { getPropValue0(p.asInstanceOf[AnyRef]) }
      case _ => getPropValue0(o)
    }
  }

  def getId(o: AnyRef): Option[I] = getPropValue[I](o, idProp)

  def asMongoDBObject(p: P): MongoDBObject = {
    def v(p: P, prop: PropertyDescriptor): Option[AnyRef] =
      prop.getReadMethod.invoke(p) match {
        case null => {
          if (isIdProp_?(prop) && isAutoId_?) {
            val id = "" + new ObjectId
            prop.getWriteMethod.invoke(p, id)
            Some(id)
          } else {
            throw new Exception("null detected in %s of %s".format(getKey(prop), p))
          }
        }
        case v if isEmbeddedProp_?(prop) =>
          Some(Mapper(getPropType(prop)).asMongoDBObject(v match {
            case Some(vv: AnyRef) if isOptionProp_?(prop) => vv case _ => v
          }))
        case v => Some(v)
      }

    allProps
    .foldLeft(MongoDBObject.newBuilder) {
      (builder, prop) => builder += getKey(prop) -> v(p, prop).get
    }
    .result
  }

  def asObject(dbo: MongoDBObject): P =
    allProps.foldLeft(obj_klass.newInstance) {
      (p, prop) =>
        val write = prop.getWriteMethod
      dbo.get(getKey(prop)) match {
        case Some(v: MongoDBObject) if isEmbeddedProp_?(prop) => {
          val e = Mapper(getPropType(prop)).asObject(v)
          write.invoke(p, if (isOptionProp_?(prop)) Some(e) else e)
        }
        case Some(v) => write.invoke(p, v)
        case _ =>
      }
      p
    }

  def findOne(id: I): Option[P] =
    coll.findOne(id) match {
      case None => None
      case Some(dbo) => Some(asObject(dbo))
    }

  // XXX: if <<? returns None, does it indicate failure_?
  def upsert(p: P): P = coll <<? asMongoDBObject(p).asDBObject match {
    case Some(dbo) => p
    case None => p
  }
}

object MapperUtils {
  def getAnnotation[A <: Annotation](prop: PropertyDescriptor, ak: Class[A]): Option[A] =
    (List(prop.getReadMethod, prop.getWriteMethod).filter {
      meth => meth.isAnnotationPresent(ak)
    }) match {
      case Nil => None
      case x => Some(x.head.getAnnotation(ak))
    }

  def isAnnotatedWith_?[A <: Annotation](prop: PropertyDescriptor, ak: Class[A]): Boolean =
    getAnnotation(prop, ak) match { case Some(a) => true case _ => false }

  def isIdProp_?(prop: PropertyDescriptor) =
    isAnnotatedWith_?(prop, classOf[ID])

  def isOptionProp_?(prop: PropertyDescriptor) =
    prop.getPropertyType == classOf[Option[_]]

  def isEmbeddedProp_?(prop: PropertyDescriptor) =
    getPropType(prop).isAnnotationPresent(classOf[MappedBy]) && isAnnotatedWith_?(prop, classOf[Key])

  implicit def getPropType(prop: PropertyDescriptor): Class[AnyRef] =
    (prop.getPropertyType match {
      case c if c == classOf[Option[_]] => {
        prop.getWriteMethod.getGenericParameterTypes
        .toList
        .map {
          _.asInstanceOf[java.lang.reflect.ParameterizedType]
          .getActualTypeArguments.head
        }.toList.head
      }
      case c => c
    }).asInstanceOf[Class[AnyRef]]

  def getKey(prop: PropertyDescriptor): String =
    if (isAnnotatedWith_?(prop, classOf[ID])) "_id"
    else {
      getAnnotation(prop, classOf[Key]) match {
        case None => prop.getName
        case Some(ann) => ann.value match {
          case "" => prop.getName
          case x => x
        }
      }
    }
}
