package com.novus.casbah
package mongodb
package mapper

import java.lang.reflect.Method
import java.lang.annotation.Annotation
import java.beans.{Introspector, PropertyDescriptor}

import scala.reflect.{BeanInfo, Manifest}
import scala.collection.JavaConversions._
import scala.collection.mutable.{Buffer, ArrayBuffer}
import scala.collection.immutable.List

import annotations.raw._
import util.Logging
import Imports._

object Mapper extends Logging {
  private val _m = new java.util.concurrent.ConcurrentHashMap[String, Mapper[_]]

  def apply[P <: AnyRef : Manifest](): Mapper[P] =
    apply(manifest[P].erasure.getName).get.asInstanceOf[Mapper[P]]

  def apply[P <: AnyRef : Manifest](p: String): Option[Mapper[P]] =
    if (_m.containsKey(p)) Some(_m.get(p).asInstanceOf[Mapper[P]])
    else None

  def apply[P <: AnyRef : Manifest](p: Class[P]): Option[Mapper[P]] = apply(p.getName)

  def update[P <: AnyRef : Manifest](p: String, m: Mapper[P]): Unit =
    if (!_m.contains(p)) _m(p) = m.asInstanceOf[Mapper[P]]

  def update[P <: AnyRef : Manifest](p: Class[P], m: Mapper[P]): Unit = update(p.getName, m)(manifest[P])
}

abstract class Mapper[P <: AnyRef : Manifest]() extends Logging {
  import Mapper._
  import MapperUtils._

  protected val obj_klass = manifest[P].erasure.asInstanceOf[Class[P]]

  Mapper(obj_klass) = this

  implicit protected def s2db(name: String): MongoDB = conn(name)
  implicit protected def s2coll(name: String): MongoCollection = db(name)

  var conn: MongoConnection = _
  var db  : MongoDB         = _
  var coll: MongoCollection = _

  lazy val info = Introspector.getBeanInfo(obj_klass)

  lazy val allProps =
    info.getPropertyDescriptors.filter {
      prop => (isAnnotatedWith_?(prop, classOf[ID]) || isAnnotatedWith_?(prop, classOf[Key]))
    }.map(validatePropKey _).toSet

  lazy val idProp =
    (allProps.filter(isId_? _)).toList match {
      case List(prop: PropertyDescriptor) =>
        if (getAnnotation(prop, classOf[ID]).get.auto && propType(prop) != classOf[ObjectId])
          throw new Exception("only ObjectId _id fields are supported when auto = true (%s . %s)".format(obj_klass.getName, prop.getName))
        else prop
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
                                               propType(p),
                                               isOption_?(p)))
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

  def getId(o: AnyRef): Option[AnyRef] = getPropValue[AnyRef](o, idProp)

  def asDBObject(p: P): DBObject = {
    def v(p: P, prop: PropertyDescriptor): Option[AnyRef] = {
      def vEmbed(e: AnyRef) = Mapper(propType(prop)).get.asDBObject(e match {
        case Some(vv: AnyRef) if isOption_?(prop) => vv
        case _ => e
      })

      prop.getReadMethod.invoke(p) match {
        case null => {
          if (isId_?(prop) && isAutoId_?) {
            val id = new ObjectId
            prop.getWriteMethod.invoke(p, id)
            Some(id)
          } else { None }
        }
        case l: List[AnyRef] if isEmbedded_?(prop) => Some(l.map(vEmbed _))
        case b: Buffer[AnyRef] if isEmbedded_?(prop) => Some(b.map(vEmbed _))
        case v if isEmbedded_?(prop) => {
          log.info("fall through embedded: %s", v)
          Some(vEmbed(v))
        }
        case v => Some(v)
      }
    }

    val result = allProps
    .foldLeft(MongoDBObject.newBuilder) {
      (builder, prop) => v(p, prop) match {
        case Some(value) => builder += getKey(prop) -> value
        case _ => builder
      }
    }
    .result

    log.debug("%s: %s -> %s", obj_klass.getSimpleName, p, result)
    result
  }

  def asObject(dbo: MongoDBObject): P = {
    def writeNested(p: P, prop: PropertyDescriptor, nested: MongoDBObject) = {
      val e = Mapper(propType(prop)).get.asObject(nested)
      val write = prop.getWriteMethod
      log.debug("write nested '%s' to '%s'.'%s' using: %s", nested, p, getKey(prop), write)
      write.invoke(p, if (isOption_?(prop)) Some(e) else e)
    }

    def writeSeq(p: P, prop: PropertyDescriptor, src: MongoDBObject) = {
      def init: Seq[Any] =
        if (isList_?(prop)) Nil
        else if (isBuffer_?(prop)) ArrayBuffer()
        else throw new Exception("whaaa! whaa! I'm lost! %s.%s".format(p, prop.getName))

      val dst = src.foldLeft(init) {
        case (list, (k, v)) =>
          init ++ (list.toList ::: (v match {
            case nested: MongoDBObject if isEmbedded_?(prop) =>
              Mapper(propType(prop)).get.asObject(nested)
            case _ => v
          }) :: Nil)
      }

      val write = prop.getWriteMethod
      log.debug("write list '%s' (%s) to '%s'.'%s' using %s",
                dst, dst.getClass.getName, p, getKey(prop), write)

      write.invoke(p, dst)
    }

    allProps.filter(!isReadOnly_?(_)).foldLeft(obj_klass.newInstance) {
      (p, prop) =>
        dbo.get(getKey(prop)) match {
          case Some(l: BasicDBList) => writeSeq(p, prop, l)
          case Some(v: MongoDBObject) if isEmbedded_?(prop) => writeNested(p, prop, v)
          case Some(v: DBObject) if isEmbedded_?(prop) => writeNested(p, prop, v)

          case Some(v) => {
            val write = prop.getWriteMethod
            log.debug("write raw '%s' (%s) to '%s'.'%s' using: %s",
                      v, v.getClass.getName, p, getKey(prop), write)
            write.invoke(p, v match {
              case oid: ObjectId => oid
              case s: String if isId_?(prop) && isAutoId_? => new ObjectId(s)
              case x => x
            })
          }
          case _ =>
        }
      p
    }
  }

  def findOne(id: AnyRef): Option[P] =
    coll.findOne(id) match {
      case None => None
      case Some(dbo) => Some(asObject(dbo))
    }

  // XXX: if <<? returns None, does it indicate failure_?
  def upsert(p: P): P = coll <<? asDBObject(p).asDBObject match {
    case Some(dbo) => p
    case None => p
  }
}

object MapperUtils {
  def getAnnotation[A <: Annotation](prop: PropertyDescriptor, ak: Class[A]): Option[A] =
    (List(prop.getReadMethod, prop.getWriteMethod).filter(_ != null).filter {
      meth => meth.isAnnotationPresent(ak)
    }) match {
      case Nil => None
      case x => Some(x.head.getAnnotation(ak))
    }

  def isAnnotatedWith_?[A <: Annotation](prop: PropertyDescriptor, ak: Class[A]): Boolean =
    getAnnotation(prop, ak) match { case Some(a) => true case _ => false }

  def isId_?(prop: PropertyDescriptor) =
    isAnnotatedWith_?(prop, classOf[ID])

  def isReadOnly_?(prop: PropertyDescriptor) = prop.getWriteMethod == null

  def isOption_?(prop: PropertyDescriptor) =
    prop.getPropertyType == classOf[Option[_]]

  def isEmbedded_?(prop: PropertyDescriptor) =
    (if (isSeq_?(propType(prop))) extractTypeParams(prop.getReadMethod).head
     else propType(prop)).isAnnotationPresent(classOf[MappedBy]) && isAnnotatedWith_?(prop, classOf[Key])

  implicit def propClass(prop: PropertyDescriptor): Class[AnyRef] =
    prop.getPropertyType.asInstanceOf[Class[AnyRef]]

  def isSeq_?(c: Class[_])    = isList_?(c) || isBuffer_?(c)
  def isList_?(c: Class[_])   = c.isAssignableFrom(classOf[List[_]])
  def isBuffer_?(c: Class[_]) = c.isAssignableFrom(classOf[Buffer[_]])

  def extractTypeParams(m: Method) = {
    m.getGenericReturnType
    .asInstanceOf[java.lang.reflect.ParameterizedType]
    .getActualTypeArguments.toList
    .map(_.asInstanceOf[Class[_]])
  }

  def propType(prop: PropertyDescriptor): Class[AnyRef] = {
    def writeType = extractTypeParams(prop.getReadMethod)

    (prop.getPropertyType match {
      case c if c == classOf[Option[_]] => writeType.head
      case c if isSeq_?(c) => writeType.head
      case c => c
    }).asInstanceOf[Class[AnyRef]]
  }

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

  def validatePropKey(prop: PropertyDescriptor) = {
    getKey(prop) match {
      case "_id" if !isId_?(prop) => throw new Exception("only @ID props can have key == \"_id\"")
      case s if s.startsWith("_") && !isId_?(prop) => throw new Exception("keys can't start with underscores")
      case s if s.contains(".") || s.contains("$") => throw new Exception("keys can't contain . or $")
      case _ => prop
    }
  }
}
