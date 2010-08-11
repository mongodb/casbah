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

class RichPropertyDescriptor(val pd: PropertyDescriptor, val parent: Class[_]) {
  import MapperUtils._

  lazy val name = pd.getName
  lazy val key = {
    (if (isAnnotatedWith_?(pd, classOf[ID])) "_id"
     else {
       getAnnotation(pd, classOf[Key]) match {
         case None => name
         case Some(ann) => ann.value match {
           case "" => name
           case x => x
         }
       }
     }) match {
      case "_id" if !id_? => throw new Exception("only @ID props can have key == \"_id\"")
      case s if s.startsWith("_") && !id_? => throw new Exception("keys can't start with underscores")
      case s if s.contains(".") || s.contains("$") => throw new Exception("keys can't contain . or $")
      case p => p
    }
  }

  lazy val read = pd.getReadMethod
  lazy val write = if (pd.getWriteMethod == null) None else Some(pd.getWriteMethod)

  lazy val innerType = {
    lazy val typeParams = extractTypeParams(read)

    (pd.getPropertyType match {
      case c if c == classOf[Option[_]] => typeParams.head
      case c if seq_? => typeParams.head
      case c => c
    }).asInstanceOf[Class[AnyRef]]
  }

  lazy val outerType = pd.getPropertyType.asInstanceOf[Class[AnyRef]]

  lazy val option_? = outerType == classOf[Option[_]]
  lazy val readOnly_? = write == null
  lazy val id_? = isAnnotatedWith_?(pd, classOf[ID])
  lazy val autoId_? = id_? && getAnnotation(pd, classOf[ID]).get.auto
  lazy val embedded_? = {
    Mapper((if (seq_?) extractTypeParams(read).head
            else innerType).getName).isDefined && isAnnotatedWith_?(pd, classOf[Key])
  }

  lazy val seq_? = list_? || buffer_?
  lazy val list_? = outerType.isAssignableFrom(classOf[List[_]])
  lazy val buffer_? = outerType.isAssignableFrom(classOf[Buffer[_]])

  override def equals(o: Any): Boolean = o match {
    case other: RichPropertyDescriptor => pd.equals(other.pd)
    case _ => false
  }

  override def hashCode(): Int = pd.hashCode()
}

abstract class Mapper[P <: AnyRef : Manifest]() extends Logging {
  import Mapper._
  import MapperUtils._

  protected val obj_klass = manifest[P].erasure.asInstanceOf[Class[P]]
  Mapper(obj_klass) = this

  //implicit def rpd2pd(prop: RichPropertyDescriptor): PropertyDescriptor = prop.pd

  implicit protected def s2db(name: String): MongoDB = conn(name)
  implicit protected def s2coll(name: String): MongoCollection = db(name)

  var conn: MongoConnection = _
  var db  : MongoDB         = _
  var coll: MongoCollection = _

  lazy val info = Introspector.getBeanInfo(obj_klass)

  lazy val allProps =
    info.getPropertyDescriptors.filter {
      prop => (isAnnotatedWith_?(prop, classOf[ID]) || isAnnotatedWith_?(prop, classOf[Key]))
    }.map(new RichPropertyDescriptor(_, obj_klass)).toSet

  lazy val idProp = allProps.filter(_.id_?).headOption match {
    case Some(id) if id.autoId_? =>
      if (id.innerType != classOf[ObjectId])
        throw new Exception("only ObjectId _id fields are supported when auto = true (%s . %s)".format(obj_klass.getName, id.name))
      else id
    case Some(id) => id
    case _ => throw new Exception("no @ID on " + obj_klass)
  }

  lazy val nonIdProps = allProps - idProp

  override def toString =
    "Mapper(%s -> idProp: %s, is_auto_id: %s, allProps: %s)".format(
      obj_klass.getName, idProp.name, idProp.autoId_?,
      allProps.map(p =>
        "Prop(%s -> %s, is_option: %s)".format(p.name,
                                               p.innerType,
                                               p.option_?))
    )

  def getPropNamed(key: String) =
    nonIdProps.filter(_.name == key).toList match {
      case List(prop) => Some(prop)
      case _ => None
    }

  def getPropValue[V <: AnyRef : Manifest](o: AnyRef, prop: RichPropertyDescriptor): Option[V] = {
    val cv = manifest[V].erasure.asInstanceOf[Class[V]]
    def getPropValue0(p: AnyRef): Option[V] =
      prop.read.invoke(p) match {
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

  def asKeyValueTuples(p: P) = {
    def v(p: P, prop: RichPropertyDescriptor): Option[Any] = {
      def vEmbed(e: AnyRef) = Mapper(prop.innerType).get.asDBObject(e match {
        case Some(vv: AnyRef) if prop.option_? => vv
        case _ => e
      })

      prop.read.invoke(p) match {
        case null => {
          if (prop.id_? && prop.autoId_?) {
            val id = new ObjectId
            prop.write.get.invoke(p, id)
            Some(id)
          } else { None }
        }
        case l: List[AnyRef] if prop.embedded_? => Some(l.map(vEmbed _))
        case b: Buffer[AnyRef] if prop.embedded_? => Some(b.map(vEmbed _))
        case v if prop.embedded_? => {
          log.info("fall through embedded: %s", v)
          Some(vEmbed(v))
        }
        case Some(v: Any) if prop.option_? => Some(v)
        case None if prop.option_? => None
        case v => Some(v)
      }
    }

    allProps
    .map {
      prop => v(p, prop) match {
        case Some(value) => Some(prop.key -> value)
        case _ => None
      }
    }.filter(_.isDefined).map(_.get)
  }

  def asDBObject(p: P): DBObject = {
    val result = asKeyValueTuples(p)
    .foldLeft(MongoDBObject.newBuilder) {
      (builder, t) => builder += t
    }
    .result

    log.debug("%s: %s -> %s", obj_klass.getName, p, result)
    result
  }

  def asObject(dbo: MongoDBObject): P = {
    def writeNested(p: P, prop: RichPropertyDescriptor, nested: MongoDBObject) = {
      val e = Mapper(prop.innerType).get.asObject(nested)
      val write = prop.write.get
      log.debug("write nested '%s' to '%s'.'%s' using: %s", nested, p, prop.key, write)
      write.invoke(p, if (prop.option_?) Some(e) else e)
    }

    def writeSeq(p: P, prop: RichPropertyDescriptor, src: MongoDBObject) = {
      def init: Seq[Any] =
        if (prop.list_?) Nil
        else if (prop.buffer_?) ArrayBuffer()
        else throw new Exception("whaaa! whaa! I'm lost! %s.%s".format(p, prop.name))

      val dst = src.foldLeft(init) {
        case (list, (k, v)) =>
          init ++ (list.toList ::: (v match {
            case nested: MongoDBObject if prop.embedded_? =>
              Mapper(prop.innerType).get.asObject(nested)
            case _ => v
          }) :: Nil)
      }

      val write = prop.write.get
      log.debug("write list '%s' (%s) to '%s'.'%s' using %s",
                dst, dst.getClass.getName, p, prop.key, write)

      write.invoke(p, dst)
    }

    allProps.filter(!_.readOnly_?).foldLeft(obj_klass.newInstance) {
      (p, prop) =>
        dbo.get(prop.key) match {
          case Some(l: BasicDBList) => writeSeq(p, prop, l)
          case Some(v: MongoDBObject) if prop.embedded_? => writeNested(p, prop, v)
          case Some(v: DBObject) if prop.embedded_? => writeNested(p, prop, v)

          case Some(v) => {
            prop.write match {
              case None => log.info("discarding raw '%s' for read-only: %s . %s", v, obj_klass.getName, prop.key)
              case Some(write) => {
                log.debug("write raw '%s' (%s) to '%s'.'%s' using: %s",
                          v, v.getClass.getName, p, prop.key, write)
                write.invoke(p, v match {
                  case oid: ObjectId => oid
                  case s: String if prop.id_? && idProp.autoId_? => new ObjectId(s)
                  case x if x != null && prop.option_? => Some(x)
                  case x => x
                })
              }
            }
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

  def extractTypeParams(m: Method) = {
    m.getGenericReturnType
    .asInstanceOf[java.lang.reflect.ParameterizedType]
    .getActualTypeArguments.toList
    .map(_.asInstanceOf[Class[_]])
  }
}
