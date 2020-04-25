/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/mongodb/casbah
 *
 */

package com.mongodb.casbah.commons
package conversions
package scala

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.commons.Logging

import org.bson.{ BSON, Transformer }

import com.github.nscala_time.time.Imports._

/**
 * " Register" Object, calls the registration methods.
 *
 * By default does not include JodaDateTime as this may be undesired behavior.
 * If you want JodaDateTime support, please use the RegisterJodaTimeConversionHelpers Object
 *
 * @since 1.0
 * @see RegisterJodaTimeConversionHelpers
 */
object RegisterConversionHelpers extends Serializers
    with Deserializers {
  def apply() {
    log.debug("Registering Scala Conversions.")
    super.register()
  }
}

/**
 * "DeRegister" Object, calls the unregistration methods.
 *
 * @since 1.0
 */
@deprecated("Be VERY careful using this - it will remove ALL of Casbah's loaded BSON Encoding & Decoding hooks at " +
  "runtime. If you need to clear Joda Time use DeregisterJodaTimeConversionHelpers.", "2.0")
object DeregisterConversionHelpers extends Serializers
    with Deserializers {
  def apply() {
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
 * @since 1.0
 */
trait Deserializers extends MongoConversionHelper {
  override def register() {
    log.debug("Deserializers for Scala Conversions registering")
    super.register()
  }

  override def unregister() {
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
 * @since 1.0
 */
trait Serializers extends MongoConversionHelper
    with ScalaRegexSerializer
    with ScalaCollectionSerializer
    with ScalaProductSerializer
    with OptionSerializer {
  override def register() {
    log.debug("Serializers for Scala Conversions registering")
    super.register()
  }

  override def unregister() {
    super.unregister()
  }
}

trait OptionSerializer extends MongoConversionHelper {
  private val transformer = new Transformer {
    log.trace("Encoding a Scala Option[].")

    // scalastyle:off null
    def transform(o: AnyRef): AnyRef = o match {
      case Some(x) => x.asInstanceOf[AnyRef]
      case None    => null
      case _       => o
    }
    // scalastyle:on null

  }

  override def register() {
    log.debug("Setting up OptionSerializer")

    BSON.addEncodingHook(classOf[_root_.scala.Option[_]], transformer)

    super.register()
  }
}

object RegisterJodaTimeConversionHelpers extends JodaDateTimeHelpers {
  def apply() {
    log.debug("Registering  Joda Time Scala Conversions.")
    super.register()
  }
}

object DeregisterJodaTimeConversionHelpers extends JodaDateTimeHelpers {
  def apply() {
    log.debug("Unregistering Joda Time Scala Conversions.")
    super.unregister()
  }
}

trait JodaDateTimeHelpers extends JodaDateTimeSerializer with JodaDateTimeDeserializer

trait JodaDateTimeSerializer extends MongoConversionHelper {

  private val encodeTypeDateTime = classOf[DateTime]
  private val encodeTypeLocalDateTime = classOf[LocalDateTime]

  /** Encoding hook for MongoDB To be able to persist JodaDateTime DateTime to MongoDB */
  private val transformer = new Transformer {
    log.trace("Encoding a JodaDateTime DateTime.")

    def transform(o: AnyRef): AnyRef = o match {
      case d: DateTime      => d.toDate // Return a JDK Date object which BSON can encode
      case l: LocalDateTime => l.toDateTime.toDate
      case _                => o
    }

  }

  override def register() {
    log.debug("Hooking up Joda DateTime serializer.")

    /** Encoding hook for MongoDB To be able to persist JodaDateTime DateTime to MongoDB */
    BSON.addEncodingHook(encodeTypeDateTime, transformer)
    BSON.addEncodingHook(encodeTypeLocalDateTime, transformer)
    super.register()
  }

  override def unregister() {
    log.debug("De-registering Joda DateTime serializer.")
    BSON.removeEncodingHooks(encodeTypeDateTime)
    BSON.removeEncodingHooks(encodeTypeLocalDateTime)
    super.unregister()
  }
}

trait JodaDateTimeDeserializer extends MongoConversionHelper {

  private val encodeType = classOf[java.util.Date]
  private val transformer = new Transformer {
    log.trace("Decoding JDK Dates .")

    def transform(o: AnyRef): AnyRef = o match {
      case jdkDate: java.util.Date => new DateTime(jdkDate)
      case d: DateTime             => d
      case l: LocalDateTime        => l.toDateTime
      case _                       => o
    }
  }

  override def register() {
    log.debug("Hooking up Joda DateTime deserializer")

    /** Encoding hook for MongoDB To be able to read JodaDateTime DateTime from MongoDB's BSON Date */
    BSON.addDecodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() {
    log.debug("De-registering Joda DateTime deserializer.")
    BSON.removeDecodingHooks(encodeType)
    super.unregister()
  }
}

trait JodaLocalDateTimeHelpers extends JodaLocalDateTimeSerializer with JodaLocalDateTimeDeserializer

trait JodaLocalDateTimeSerializer extends MongoConversionHelper {

  private val encodeTypeDateTime = classOf[DateTime]
  private val encodeTypeLocalDateTime = classOf[LocalDateTime]

  /** Encoding hook for MongoDB To be able to persist JodaDateTime DateTime to MongoDB */
  private val transformer = new Transformer {
    log.trace("Encoding a JodaLocalDateTime DateTime.")

    def transform(o: AnyRef): AnyRef = o match {
      case d: DateTime      => d.toLocalDateTime.toDate // Return a JDK Date object which BSON can encode
      case l: LocalDateTime => l.toDate
      case _                => o
    }

  }

  override def register() {
    log.debug("Hooking up Joda LocalDateTime serializer.")

    /** Encoding hook for MongoDB To be able to persist JodaLocalDateTime DateTime to MongoDB */
    BSON.addEncodingHook(encodeTypeDateTime, transformer)
    BSON.addEncodingHook(encodeTypeLocalDateTime, transformer)
    super.register()
  }

  override def unregister() {
    log.debug("De-registering Joda LocalDateTime serializer.")
    BSON.removeEncodingHooks(encodeTypeDateTime)
    BSON.removeEncodingHooks(encodeTypeLocalDateTime)
    super.unregister()
  }
}

trait JodaLocalDateTimeDeserializer extends MongoConversionHelper {

  private val encodeType = classOf[java.util.Date]
  private val transformer = new Transformer {
    log.trace("Decoding JDK Dates .")

    def transform(o: AnyRef): AnyRef = o match {
      case jdkDate: java.util.Date => new LocalDateTime(jdkDate)
      case d: DateTime             => d.toLocalDateTime
      case l: LocalDateTime        => l
      case _                       => o
    }
  }

  override def register() {
    log.debug("Hooking up Joda DateTime deserializer")

    /** Encoding hook for MongoDB To be able to read JodaDateTime DateTime from MongoDB's BSON Date */
    BSON.addDecodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() {
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
      case _                                     => o
    }

  }

  override def register() {
    log.debug("Setting up ScalaRegexSerializers")

    log.debug("Hooking up scala.util.matching.Regex serializer")

    /** Encoding hook for MongoDB to translate a Scala Regex to a JAva Regex (which Mongo will understand) */
    BSON.addEncodingHook(classOf[_root_.scala.util.matching.Regex], transformer)

    super.register()
  }
}

object RegisterJodaLocalDateTimeConversionHelpers extends JodaLocalDateTimeHelpers {
  def apply() {
    log.debug("Registering  Joda Time Scala Conversions.")
    super.register()
  }
}

object DeregisterJodaLocalDateTimeConversionHelpers extends JodaLocalDateTimeHelpers {
  def apply() {
    log.debug("Unregistering Joda Time Scala Conversions.")
    super.unregister()
  }
}

/**
 * Implementation which is aware of the possible conversions in scalaj-collection and attempts to Leverage it...
 * Not all of these may be serializable by Mongo However... this is a first pass attempt at moving them to Java types
 */
trait ScalaCollectionSerializer extends MongoConversionHelper {

  private val transformer = new Transformer {

    import _root_.scala.jdk.CollectionConverters._

    def transform(o: AnyRef): AnyRef = o match {
      case mdbo: MongoDBObject => mdbo.underlying // MongoDBObject is a custom Iterable
      case mdbl: MongoDBList => mdbl.underlying // MongoDBList is a custom Iterable
      case m: _root_.scala.collection.mutable.Map[_, _] => m.asJava // Maps are Iterable but we need to keep them Map like
      case m: _root_.scala.collection.Map[_, _] => m.asJava // Maps are Iterable but we need to keep them Map like
      case i: _root_.scala.collection.Iterable[_] => i.asJava
      case i: _root_.scala.collection.Iterator[_] => i.asJava
      case _ => o
    }
  }

  override def register() {
    log.debug("Setting up ScalaCollectionSerializer")
    BSON.addEncodingHook(classOf[_root_.scala.collection.Iterator[_]], transformer)
    BSON.addEncodingHook(classOf[_root_.scala.collection.Iterable[_]], transformer)
    super.register()
  }
}

trait ScalaProductSerializer extends MongoConversionHelper {

  import _root_.scala.jdk.CollectionConverters._

  private val transformer = new Transformer {
    log.trace("Encoding a Scala Product.")

    def transform(o: AnyRef): AnyRef = o match {
      case l: java.util.List[_] => l // Ignore converted Products that are wrapped java.util.Lists
      case p: Product           => p.productIterator.toList.asJava
      case _                    => o
    }
  }

  override def register() {
    log.debug("Setting up ScalaProductSerializer")
    BSON.addEncodingHook(classOf[Product], transformer)
    super.register()
  }
}
