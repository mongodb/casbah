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

import _root_.scala.math.{BigDecimal => ScalaBigDecimal}
import java.math.{BigDecimal => JavaBigDecimal, RoundingMode, MathContext}

import annotations.raw._
import util.Logging
import Imports._

object Mapper extends Logging {
  private val _m = new java.util.concurrent.ConcurrentHashMap[String, Mapper[_]]

  def apply[P <: AnyRef : Manifest](): Mapper[P] =
    apply(manifest[P].erasure.getName).get.asInstanceOf[Mapper[P]]

  def apply[P <: AnyRef](p: String): Option[Mapper[P]] =
    if (_m.containsKey(p)) Some(_m.get(p).asInstanceOf[Mapper[P]])
    else None

  def apply[P <: AnyRef : Manifest](p: Class[P]): Option[Mapper[P]] = apply(p.getName)

  def update[P <: AnyRef](p: String, m: Mapper[P]): Unit =
    if (!_m.contains(p)) _m(p) = m.asInstanceOf[Mapper[P]]

  def update[P <: AnyRef : Manifest](p: Class[P], m: Mapper[P]): Unit = update(p.getName, m)
}

class RichPropertyDescriptor(val idx: Int, val pd: PropertyDescriptor, val parent: Class[_]) extends Logging {
  import MapperUtils._

  override def toString = "Prop/%s(%s @ %d (%s <- %s))".format(parent.getSimpleName, name, idx, innerType, outerType)

  lazy val name = pd.getName
  lazy val key = {
    (if (annotated_?(pd, classOf[ID])) "_id"
     else {
       annotation(pd, classOf[Key]) match {
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

  lazy val pid: Option[Any] = {
    // XXX ?!
    def pid0(t: Class[Any]): Option[Any] = t match {
      case _ if seq_? => None // `pid` is undefined for sequences
      case _ if embedded_? => None // ditto for embedded documents
      case _ if option_? => None // N/A to Option-s
      case _ if t.isAssignableFrom(classOf[Double]) => Some(idx.toDouble)
      case _ if t.isAssignableFrom(classOf[Float]) => Some(idx.toFloat)
      case _ if t.isAssignableFrom(classOf[Long]) => Some(idx.toLong)
      case _ if t.isAssignableFrom(classOf[Int]) => Some(idx)
      case _ if t.isAssignableFrom(classOf[Short]) => Some(idx.toShort)
      case _ if t.isAssignableFrom(classOf[Byte]) => Some(idx.toByte)
      case _ if t == classOf[String] => Some("%d".format(idx))
      case _ => None
    }
    pid0(innerType)
  }

  lazy val read = pd.getReadMethod
  lazy val write = if (pd.getWriteMethod == null) None else Some(pd.getWriteMethod)

  lazy val field = try {
    val f = parent.getDeclaredField(name)
    f.setAccessible(true)
    Some(f)
  }
  catch {
    case _ => None
  }

  def write(dest: AnyRef, value: Any): Unit =
    field match {
      case Some(field) => field.set(dest, value)
      case None => write match {
        case Some(write) => write.invoke(dest, value.asInstanceOf[AnyRef])
        case None => // NOOP
      }
    }

  lazy val innerTypes = typeParams(read)
  lazy val innerType = {
    outerType match {
      case c if c == classOf[Option[_]] => innerTypes.head
      case c if map_? => innerTypes.last
      case c if seq_? => innerTypes.head
      case c => c
    }
  }.asInstanceOf[Class[Any]]

  lazy val outerType = pd.getPropertyType.asInstanceOf[Class[Any]]

  lazy val option_? = outerType == classOf[Option[_]]
  lazy val readOnly_? = write == null
  lazy val id_? = annotated_?(pd, classOf[ID])
  lazy val autoId_? = id_? && annotation(pd, classOf[ID]).get.auto
  lazy val embedded_? = annotated_?(pd, classOf[Key]) && (annotated_?(pd, classOf[UseTypeHints]) || Mapper(innerType.getName).isDefined)

  lazy val seq_? = !map_? && (list_? || buffer_?)
  lazy val list_? = outerType.isAssignableFrom(classOf[List[_]])
  lazy val buffer_? = outerType.isAssignableFrom(classOf[Buffer[_]])
  lazy val map_? = outerType.isAssignableFrom(classOf[Map[_,_]])

  lazy val useTypeHints_? = annotation[UseTypeHints](pd, classOf[UseTypeHints]) match {
    case Some(ann) if ann.value => true
    case _ => false
  }

  def readMapper(p: AnyRef) = {
    log.trace("readMapper: %s -> %s, %s", p, Mapper(innerType.getName).isDefined, Mapper(p.getClass.getName).isDefined)
    Mapper(innerType.getName) match {
      case Some(mapper) => mapper
      case None if useTypeHints_? => Mapper(p.getClass.getName) match {
	case Some(mapper) => mapper
	case _ => throw new MissingMapper(ReadMapper, p.getClass)
      }
      case _ => throw new MissingMapper(ReadMapper, innerType)
    }
  }.asInstanceOf[Mapper[AnyRef]]

  def writeMapper(dbo: MongoDBObject) = {
    log.trace("writeMapper: %s -> %s, %s", dbo, Mapper(innerType.getName).isDefined,
              dbo.get(TYPE_HINT).isDefined && Mapper(dbo(TYPE_HINT).asInstanceOf[String]).isDefined)
    Mapper(innerType.getName) match {
      case Some(mapper) => mapper
      case None if useTypeHints_? => dbo.get(TYPE_HINT) match {
        case Some(typeHint: String) => Mapper(typeHint) match {
	  case Some(mapper) => mapper
	  case _ => throw new MissingMapper(WriteMapper, Class.forName(typeHint))
	}
        case _ => throw new MissingMapper(WriteMapper, innerType, "no @UseTypeHints on %s".format(this))
      }
      case _ => throw new MissingMapper(WriteMapper, innerType)
    }
  }.asInstanceOf[Mapper[AnyRef]]

  override def equals(o: Any): Boolean = o match {
    case other: RichPropertyDescriptor => pd.equals(other.pd)
    case _ => false
  }

  override def hashCode(): Int = pd.hashCode()
}

abstract class Mapper[P <: AnyRef : Manifest]() extends Logging with OJ {
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

  lazy val allProps = {
    info.getPropertyDescriptors.filter {
      prop => (annotated_?(prop, classOf[ID]) || annotated_?(prop, classOf[Key]))
    }
    .sortWith { case (a, b) => a.getName.compareTo(b.getName) < 0 }
    .zipWithIndex.map {
      case (pd: PropertyDescriptor, idx: Int) =>
        new RichPropertyDescriptor(idx, pd, obj_klass)
    }.toSet
  }

  lazy val propsByPid = Map.empty ++ (allProps.map {
    p => p.pid match {
      case Some(pid) => Some(pid -> p)
      case None => None
    }
  }).filter(_.isDefined).map(_.get)

  lazy val idProp = allProps.filter(_.id_?).headOption match {
    case Some(id) if id.autoId_? =>
      if (id.innerType != classOf[ObjectId])
        throw new Exception("only ObjectId _id fields are supported when auto = true (%s . %s)".format(obj_klass.getName, id.name))
      else Some(id)
    case Some(id) => Some(id)
    case _ => None
  }

  lazy val nonIdProps = idProp match {
    case Some(id) => allProps - id
    case None => allProps
  }

  override def toString =
    "Mapper(%s -> idProp: %s, is_auto_id: %s, allProps: %s)".format(
      obj_klass.getName, idProp.map(_.name).getOrElse("N/A"), idProp.map(_.autoId_?).getOrElse(false),
      allProps.map(p =>
        "Prop(%s -> %s, is_option: %s)".format(p.name,
                                               p.innerType,
                                               p.option_?))
    )

  def propNamed(key: String) =
    nonIdProps.filter(_.name == key).toList match {
      case List(prop) => Some(prop)
      case _ => None
    }

  private def embeddedPropValue(p: P, prop: RichPropertyDescriptor, embedded: AnyRef) = {
    log.trace("EMB: %s -> %s -> %s", p, prop, embedded)

    val dbo = prop.readMapper(embedded).asDBObject(embedded match {
      case Some(vv: AnyRef) if prop.option_? => vv
      case _ => embedded
    })

    if (prop.useTypeHints_?)
      dbo(TYPE_HINT) = (embedded match {
        case Some(vv: AnyRef) if prop.option_? => vv.getClass
        case _ => embedded.getClass
      }).getName

    dbo
  }

  def propValue(p: P, prop: RichPropertyDescriptor): Option[Any] = {
    log.trace("V: %s , %s with %s", p, prop, prop.read)

    val readValue = try {
      prop.read.invoke(p)
    }
    catch {
      case t =>
        throw new Exception("failed to read: V: %s , %s with %s".format(p, prop, prop.read))
    }
    (readValue match {
      case null => {
        if (prop.id_? && prop.autoId_?) {
          val id = new ObjectId
          prop.write(p, id)
          Some(id)
        } else { None }
      }
      case l: List[AnyRef] if prop.embedded_? => Some(l.map(embeddedPropValue(p, prop, _)))
      case b: Buffer[AnyRef] if prop.embedded_? => Some(b.map(embeddedPropValue(p, prop, _)))
      case m if prop.map_? => Some(m.asInstanceOf[scala.collection.Map[String, Any]].map {
        case (k, v) => {
          k -> (if (prop.embedded_?) embeddedPropValue(p, prop, v.asInstanceOf[AnyRef])
                else v)
        }
      }.asDBObject)
      case Some(v: Any) if prop.option_? => {
        log.trace("option: embedded_? %s <- %s -> %s", prop.embedded_?, prop, v)
        if (prop.embedded_?)
          Some(embeddedPropValue(p, prop, v.asInstanceOf[AnyRef]))
        else
          Some(v)
      }
      case v if prop.embedded_? => {
        log.trace("bare embedded: %s", v)
        Some(embeddedPropValue(p, prop, v))
      }
      case None if prop.option_? => None
      case None if !prop.option_? => throw new Exception("%s should be option but is not?".format(prop))
      case v => Some(v)
    }) match {
      case Some(bd: ScalaBigDecimal) => Some(bd(MATH_CONTEXT).toDouble)
      case Some(bd: JavaBigDecimal) => Some(bd.round(MATH_CONTEXT).doubleValue)
      case x => x
    }
  }

  def asKeyValueTuples(p: P) = {
    log.trace("AKVT: %s", p)

    allProps.toList
    .map {
      prop =>
        log.trace("AKVT: %s -> %s", p, prop)
      propValue(p, prop) match {
        case Some(value) => Some(prop.key -> value)
        case _ => None
      }
    }.filter(_.isDefined).map(_.get)
  }

  def asDBObject(p: P): DBObject = {
    val result = {
      asKeyValueTuples(p)
      .foldLeft(MongoDBObject.newBuilder) {
        (builder, t) => builder += t
      }
      .result
    }

    log.trace("%s: %s -> %s", obj_klass.getName, p, result)
    result
  }

  private def writeNested(p: P, prop: RichPropertyDescriptor, nested: MongoDBObject) = {
    val e = prop.writeMapper(nested).asObject(nested)
    log.trace("write nested '%s' to '%s'.'%s' using: %s -OR- %s", nested, p, prop.key, prop.write, prop.field)
    prop.write(p, if (prop.option_?) Some(e) else e)
  }

  private def writeSeq(p: P, prop: RichPropertyDescriptor, src: MongoDBObject) = {
    def init: Seq[Any] =
      if (prop.list_?) Nil
      else if (prop.buffer_?) ArrayBuffer()
      else throw new Exception("whaaa! whaa! I'm lost! %s.%s".format(p, prop.name))

    val dst = src.foldLeft(init) {
      case (list, (k, v)) =>
        init ++ (list.toList ::: (v match {
          case nested: MongoDBObject if prop.embedded_? =>
            prop.writeMapper(nested).asObject(nested)
          case nested: DBObject if prop.embedded_? =>
            prop.writeMapper(nested).asObject(nested)
          case _ => v
        }) :: Nil)
    }

    log.trace("write list '%s' (%s) to '%s'.'%s' using %s -OR- %s",
              dst, dst.getClass.getName, p, prop.key, prop.write, prop.field)

    prop.write(p, dst)
  }

  private def writeMap(p: P, prop: RichPropertyDescriptor, src: MongoDBObject) = {
    // XXX: provide sensible defaults based on (im)mutable interface used/inferred
    def init: scala.collection.Map[String, Any] = scala.collection.mutable.HashMap.empty[String, Any]

    val dst = src.map {
      case (k, v) =>
        k -> (v match {
          case nested: DBObject if prop.embedded_? =>
            prop.writeMapper(nested).asObject(nested)
          case _ => v
        })
    }

    log.trace("write ---MAP--- '%s' (%s) to '%s'.'%s' using: %s -OR- %s",
              dst, dst.getClass.getName, p, prop.key, prop.write, prop.field)

    prop.write(p, dst)
  }

  private def write(p: P, prop: RichPropertyDescriptor, v: Any): Unit =
    v match {
      case Some(l: BasicDBList) => writeSeq(p, prop, l)

      case Some(m: MongoDBObject) if prop.map_? => writeMap(p, prop, m)
      case Some(v: MongoDBObject) if prop.embedded_? => writeNested(p, prop, v)

      case Some(m: DBObject) if prop.map_? => writeMap(p, prop, m)
      case Some(v: DBObject) if prop.embedded_? => writeNested(p, prop, v)

      case Some(v) => {
        log.trace("write raw '%s' (%s) to '%s'.'%s' using: %s -OR- %s",
                  v, v.asInstanceOf[AnyRef].getClass.getName, p, prop.key, prop.write, prop.field)
        prop.write(p, (v match {
          case oid: ObjectId => oid
          case s: String if prop.id_? && idProp.map(_.autoId_?).getOrElse(false) => new ObjectId(s)
          case d: Double if prop.innerType == classOf[JavaBigDecimal] => new JavaBigDecimal(d, MATH_CONTEXT)
          case d: Double if prop.innerType == classOf[ScalaBigDecimal] => ScalaBigDecimal(d, MATH_CONTEXT)
          case _ => v
        }) match {
          case x if x != null && prop.option_? => Some(x)
          case x if x == null && prop.option_? => None
          case x => x
        })
      }
      case _ =>
    }

  def empty: P = try {
    obj_klass.newInstance
  } catch {
    case _ => newInstance[P](obj_klass)(manifest[P])
  }

  def asObject(dbo: MongoDBObject): P =
    allProps.filter(!_.readOnly_?).foldLeft(empty) {
      (p, prop) => write(p, prop, dbo.get(prop.key))
      p
    }

  def findOne(id: Any): Option[P] =
    coll.findOne("_id" -> id) match {
      case None => None
      case Some(dbo) => Some(asObject(dbo))
    }

  def example: P =
    (propsByPid.map { case (k,v) => v->k }).foldLeft(empty) {
      case (e, (prop, pid)) => {
        write(e, prop, Some(pid))
        e
      }
    }

  // XXX: if <<? returns None, does it indicate failure_?
  def upsert(p: P): P = coll <<? asDBObject(p).asDBObject match {
    case Some(dbo) => p
    case None => p
  }
}

object MapperUtils {
  val MATH_CONTEXT = new MathContext(16, RoundingMode.UNNECESSARY);
  val TYPE_HINT = "_typeHint"

  def annotation[A <: Annotation](prop: PropertyDescriptor, ak: Class[A]): Option[A] =
    (List(prop.getReadMethod, prop.getWriteMethod).filter(_ != null).filter {
      meth => meth.isAnnotationPresent(ak)
    }) match {
      case Nil => None
      case x => Some(x.head.getAnnotation(ak))
    }

  def annotated_?[A <: Annotation](prop: PropertyDescriptor, ak: Class[A]): Boolean =
    annotation(prop, ak) match { case Some(a) => true case _ => false }

  def typeParams(m: Method) = {
    m.getGenericReturnType
    .asInstanceOf[java.lang.reflect.ParameterizedType]
    .getActualTypeArguments.toList
    .map(_.asInstanceOf[Class[_]])
  }
}

trait OJ {
  import org.objenesis.ObjenesisStd

  val objenesis = new ObjenesisStd

  def newInstance[T: Manifest](clazz: Class[T]): T =
    manifest[T].erasure.cast(objenesis.newInstance(clazz)).asInstanceOf[T]
}

private[mapper] sealed trait MapperDirection
private[mapper] case object ReadMapper extends MapperDirection
private[mapper] case object WriteMapper extends MapperDirection
class MissingMapper(d: MapperDirection, c: Class[_], m: String = "no further info") extends Exception("%s is missing for: %s (%s)".format(d, c, m))
