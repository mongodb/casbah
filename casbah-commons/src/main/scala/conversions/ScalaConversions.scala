/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
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
 * For questions and comments about this product, please see the project page at:
 *
 *     http://github.com/mongodb/casbah
 * 
 */

package com.mongodb.casbah.commons
package conversions
package scala

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.commons.Logging 

import org.bson.{BSON, Transformer}

import org.scala_tools.time.Imports._

/** 
 * " Register" Object, calls the registration methods.
 * 
 * By default does not include JodaDateTime as this may be undesired behavior.
 * If you want JodaDateTime support, please use the RegisterJodaTimeConversionHelpers Object
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 * @see RegisterJodaTimeConversionHelpers
 */
object RegisterConversionHelpers extends Serializers
                                    with Deserializers  {
  def apply() = {
    log.debug("Registering Scala Conversions.")
    super.register()
  }
}

/** 
 * "DeRegister" Object, calls the unregistration methods.
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 */
@deprecated("Be VERY careful using this - it will remove ALL of Casbah's loaded BSON Encoding & Decoding hooks at runtime. If you need to clear Joda Time use DeregisterJodaTimeConversionHelpers.")
object DeregisterConversionHelpers extends Serializers
                                     with Deserializers {
  def apply() = {
    log.debug("Deregistering Scala Conversions.")
    // TODO - Figure out how to clear specific hooks as this clobbers everything.
    log.warning("Clobbering Casbah's Registered BSON Type Hooks (EXCEPT Joda Time).  Reregister any specific ones you may need.")
    super.unregister()
  }
}

/** 
 * Converters for reading Scala types from MongoDB
 *
 * These should be setup in a way which requires
 * an explicit invocation / registration of individual
 * deserializers, else unexpected behavior will occur.
 *
 * Because it's likely to be controversial, JodaDateTime is NOT mixed in by default.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 */
trait Deserializers extends MongoConversionHelper {
  override def register() =  {
    log.debug("Deserializers for Scala Conversions registering")
    super.register()
  }
  override def unregister() = {
    super.unregister()
  }
}

/** 
 * Converters for saving Scala types to MongoDB
 *
 * For the most part these are 'safe' to enable automatically,
 * as they won't break existing code.
 * Be very careful with the deserializers however as they can come with
 * unexpected behavior.
 *
 * Because it's likely to be controversial, JodaDateTime is NOT mixed in by default.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 1.0
 */
trait Serializers extends MongoConversionHelper 
                     with ScalaRegexSerializer 
                     with ScalaJCollectionSerializer {
  override def register() =  {
    log.debug("Serializers for Scala Conversions registering")
    super.register()
  }
  override def unregister() = {
    super.unregister()
  }
}


object RegisterJodaTimeConversionHelpers extends JodaDateTimeHelpers {
  def apply() = {
    log.debug("Registering  Joda Time Scala Conversions.")
    super.register()
  }
}

object DeregisterJodaTimeConversionHelpers extends JodaDateTimeHelpers {
  def apply() = {
    log.debug("Unregistering Joda Time Scala Conversions.")
    super.unregister()
  }
}

trait JodaDateTimeHelpers extends JodaDateTimeSerializer with JodaDateTimeDeserializer 

trait JodaDateTimeSerializer extends MongoConversionHelper {

  private val encodeType = classOf[DateTime]
  /** Encoding hook for MongoDB To be able to persist JodaDateTime DateTime to MongoDB */
  private val transformer = new Transformer {
    log.trace("Encoding a JodaDateTime DateTime.")

    def transform(o: AnyRef): AnyRef = o match {
      case d: DateTime => d.toDate // Return a JDK Date object which BSON can encode
      case _ => o
    }
       
  }

  override def register() = {
    log.debug("Hooking up Joda DateTime serializer.")
    /** Encoding hook for MongoDB To be able to persist JodaDateTime DateTime to MongoDB */
    BSON.addEncodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() = {
    log.debug("De-registering Joda DateTime serializer.")
    BSON.removeEncodingHooks(encodeType)
    super.unregister()
  }
}

trait JodaDateTimeDeserializer extends MongoConversionHelper {

  private val encodeType = classOf[java.util.Date]
  private val transformer = new Transformer {
    log.trace("Decoding JDK Dates .")

    def transform(o: AnyRef): AnyRef = o match {
      case jdkDate: java.util.Date => new DateTime(jdkDate)
      case d: DateTime => d
      case _ => o
    }
  }

  override def register() = {
    log.debug("Hooking up Joda DateTime deserializer")
    /** Encoding hook for MongoDB To be able to read JodaDateTime DateTime from MongoDB's BSON Date */
    BSON.addDecodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() = {
    log.debug("De-registering Joda DateTime deserializer.")
    BSON.removeDecodingHooks(encodeType)
    super.unregister()
  }
}
  
trait ScalaRegexSerializer extends MongoConversionHelper {
  private val transformer = new Transformer { 
    log.trace("Encoding a Scala RegEx.")

    def transform(o: AnyRef): AnyRef = o match {
      case sRE: _root_.scala.util.matching.Regex => sRE.pattern
      case _ => o
    }
       
  }

  override def register() = {
    log.debug("Setting up ScalaRegexSerializers")

    log.debug("Hooking up scala.util.matching.Regex serializer")
    /** Encoding hook for MongoDB to translate a Scala Regex to a JAva Regex (which Mongo will understand)*/
    BSON.addEncodingHook(classOf[_root_.scala.util.matching.Regex], transformer)

    super.register()
  }
}



/**
 * Implementation which is aware of the possible conversions in scalaj-collection and attempts to Leverage it...
 * Not all of these may be serializable by Mongo However... this is a first pass attempt at moving them to Java types
 */
trait ScalaJCollectionSerializer extends MongoConversionHelper {
  
  private val transformer = new Transformer {
    import scalaj.collection.Imports._

    def transform(o: AnyRef): AnyRef = o match {
      case mdbo: MongoDBObject => mdbo.underlying
      case b: _root_.scala.collection.mutable.Buffer[_] => b.asJava
      case s: _root_.scala.collection.mutable.Seq[_] => s.asJava
      case s: _root_.scala.collection.Seq[_] => s.asJava
      case s: _root_.scala.collection.mutable.Set[_] => s.asJava
      case s: _root_.scala.collection.Set[_] => s.asJava
      case i: _root_.scala.collection.Iterable[_] => i.asJava
      case i: _root_.scala.collection.Iterator[_] => i.asJava
      case p: Product => p.productIterator.toList.asJava
      case _ => o // don't warn because we get EVERYTHING
    }
  }

  override def register() = {
    log.debug("Setting up ScalaJCollectionSerializer")
    BSON.addEncodingHook(classOf[_root_.scala.collection.mutable.Buffer[_]], transformer)
    BSON.addEncodingHook(classOf[_root_.scala.collection.mutable.ArrayBuffer[_]], transformer)
    BSON.addEncodingHook(classOf[_root_.scala.collection.mutable.ObservableBuffer[_]], transformer)
    BSON.addEncodingHook(classOf[_root_.scala.collection.mutable.SynchronizedBuffer[_]], transformer)
    BSON.addEncodingHook(classOf[_root_.scala.collection.mutable.ListBuffer[_]], transformer)
    BSON.addEncodingHook(classOf[_root_.scala.collection.Iterator[_]], transformer)
    BSON.addEncodingHook(classOf[_root_.scala.collection.Iterable[_]], transformer) 
    BSON.addEncodingHook(classOf[_root_.scala.collection.mutable.Seq[_]], transformer)
    BSON.addEncodingHook(classOf[_root_.scala.collection.Seq[_]], transformer) 
    BSON.addEncodingHook(classOf[_root_.scala.collection.mutable.Set[_]], transformer) 
    BSON.addEncodingHook(classOf[_root_.scala.collection.Set[_]], transformer)
    BSON.addEncodingHook(classOf[Product], transformer)
    super.register()
  }
}

// vim: set ts=2 sw=2 sts=2 et:
