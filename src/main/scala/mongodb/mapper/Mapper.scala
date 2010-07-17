package com.novus.casbah
package mongodb
package mapper

import java.lang.reflect.Method
import java.lang.annotation.Annotation
import java.beans.{Introspector, PropertyDescriptor}

import scala.reflect.{BeanInfo, Manifest}
import scala.collection.JavaConversions._

import mongodb.{MongoDB, MongoCollection, MongoDBObject}
import annotations.raw._
import util.Logging

import org.bson.types.ObjectId

object Mapper {
  private val _m = new java.util.concurrent.ConcurrentHashMap[Class[_], Mapper[_,_]]

  def apply[P <: AnyRef : Manifest](): Mapper[AnyRef, P] = {
    val klass = manifest[P].erasure
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

  protected val id_klass = manifest[I].erasure.asInstanceOf[Class[I]]
  protected val obj_klass = manifest[P].erasure.asInstanceOf[Class[P]]

  implicit protected def s2db(name: String): MongoDB = conn(name)
  implicit protected def s2coll(name: String): MongoCollection = db(name)

  var conn: MongoConnection = _
  var db  : MongoDB         = _
  var coll: MongoCollection = _

  lazy val info = Introspector.getBeanInfo(obj_klass)

  lazy val all_props =
    info.getPropertyDescriptors.filter {
      prop => (is_annotated_with_?(prop, classOf[ID]) || is_annotated_with_?(prop, classOf[Key]))
    }.toSet

  lazy val id_prop =
    (all_props.filter(is_id_prop_? _)).toList match {
      case List(prop) => prop
      case Nil => throw new Exception("no @ID on " + obj_klass)
      case _ => throw new Exception("more than one @ID on " + obj_klass)
    }

  lazy val non_id_props = all_props - id_prop

  lazy val is_auto_id_? = get_annotation(id_prop, classOf[ID]).get.auto

  override def toString =
    "Mapper(%s -> id_prop: %s, is_auto_id: %s, all_props: %s)".format(
      obj_klass.getSimpleName, id_prop.getName, is_auto_id_?, all_props.map(_.getName)
    )

  private def get_annotation[A <: Annotation](prop: PropertyDescriptor, ak: Class[A]): Option[A] =
    (List(prop.getReadMethod, prop.getWriteMethod).filter {
      meth => meth.isAnnotationPresent(ak)
    }) match {
      case Nil => None
      case x => Some(x.head.getAnnotation(ak))
    }

  private def is_annotated_with_?[A <: Annotation](prop: PropertyDescriptor, ak: Class[A]): Boolean =
    get_annotation(prop, ak) match { case Some(a) => true case _ => false }

  private def is_id_prop_?(prop: PropertyDescriptor) =
    is_annotated_with_?(prop, classOf[ID])

  def get_prop_named(key: String) =
    non_id_props.filter(_.getName == key).toList match {
      case List(prop) => Some(prop)
      case _ => None
    }

  def get_prop_value[V <: AnyRef : Manifest](o: AnyRef, prop: PropertyDescriptor): Option[V] = {
    val cv = manifest[V].erasure.asInstanceOf[Class[V]]
    def do_get_prop_value0(p: AnyRef): Option[V] = {
      prop.getReadMethod.invoke(p) match {
        case v if v == null => None
        case v if cv.isAssignableFrom(v.getClass) => Some(cv.cast(v))
        case _ => None
      }
    }

    o match {
      case None => None
      case Some(p) => { do_get_prop_value0(p.asInstanceOf[AnyRef]) }
      case _ => do_get_prop_value0(o)
    }
  }

  def get_id(o: AnyRef): Option[I] = get_prop_value[I](o, id_prop)

  implicit private def prop_type(prop: PropertyDescriptor): Class[AnyRef] =
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

  private def get_key(prop: PropertyDescriptor): String =
    if (is_annotated_with_?(prop, classOf[ID])) "_id"
    else {
      get_annotation(prop, classOf[Key]) match {
        case None => prop.getName
        case Some(ann) => ann.value match {
          case "" => prop.getName
          case x => x
        }
      }
    }

  private def get_key(key: String): String = get_prop_named(key) match {
    case Some(prop) => get_key(prop)
    case None => key
  }

  def to_dbo(p: P): MongoDBObject = {
    def v(p: P, prop: PropertyDescriptor): Option[AnyRef] =
      prop.getReadMethod.invoke(p) match {
        case null => {
          if (is_id_prop_?(prop) && is_auto_id_?) {
            val id = "" + new ObjectId
            prop.getWriteMethod.invoke(p, id)
            Some(id)
          } else {
            throw new Exception("null detected in %s of %s".format(get_key(prop), p))
          }
        }
        case v => Some(v)
      }

    all_props
    .foldLeft(MongoDBObject.newBuilder) {
      (builder, prop) => builder += get_key(prop) -> v(p, prop).get
    }
    .result
  }

  def from_dbo(map: Map[String, AnyRef]): P =
    all_props.foldLeft(obj_klass.newInstance) {
      (p, prop) =>
        val key = get_key(prop)
      if (map.contains(key))
        prop.getWriteMethod.invoke(p, map(key))
      p
    }

  def find_one(id: I): Option[P] = None
  def find: Seq[P] = Nil
  def upsert(p: P): P = coll <<? to_dbo(p).asDBObject match {
    case Some(dbo) => p
    case None => p
  }
}
