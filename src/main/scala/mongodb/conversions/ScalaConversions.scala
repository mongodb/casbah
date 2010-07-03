/**
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
 *     http://bitbucket.org/novus/casbah
 * 
 * NOTICE: Portions of this work are derived from the Apache License 2.0 "mongo-scala-driver" work
 * by Alexander Azarov <azarov@osinka.ru>, available from http://github.com/alaz/mongo-scala-driver
 */

package com.novus.casbah.mongodb
package conversions
package scala

import com.novus.casbah.util.Logging 

import com.mongodb._
import org.bson.{BSON, Transformer}

import org.scala_tools.time.Imports._

/** 
 * " Register" Object, calls the registration methods.
 * 
 * By default does not include JodaDateTime as this may be undesired behavior.
 * If you want JodaDateTime support, please use the RegisterJodaTimeConversionHelpers Object
 * 
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0, 06/22/10
 * @since 1.0
 * @see RegisterJodaTimeConversionHelpers
 */
object RegisterConversionHelpers extends Serializers
                                    with Deserializers  {
  def apply() = {
    log.info("Registering Scala Conversions.")
    super.register()
  }
}

/** 
 * "DeRegister" Object, calls the unregistration methods.
 * 
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0, 06/22/10
 * @since 1.0
 */
@deprecated("Be VERY careful using this - it will remove ALL of Casbah's loaded BSON Encoding & Decoding hooks at runtime. If you need to clear Joda Time use DeregisterJodaTimeConversionHelpers.")
object DeregisterConversionHelpers extends Serializers
                                     with Deserializers {
  def apply() = {
    log.info("Deregistering Scala Conversions.")
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
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0, 06/22/10
 * @since 1.0
 */
trait Deserializers extends MongoConversionHelper {
  override def register() =  {
    log.info("Deserializers for Scala Conversions registering")
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
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.0, 06/22/10
 * @since 1.0
 */
trait Serializers extends MongoConversionHelper 
                     with ScalaRegexSerializer 
                     with ScalaArrayBufferSerializer {
  override def register() =  {
    log.info("Serializers for Scala Conversions registering")
    super.register()
  }
  override def unregister() = {
    super.unregister()
  }
}


object RegisterJodaTimeConversionHelpers extends JodaDateTimeHelpers {
  def apply() = {
    log.info("Registering  Joda Time Scala Conversions.")
    super.register()
  }
}

object DeregisterJodaTimeConversionHelpers extends JodaDateTimeHelpers {
  def apply() = {
    log.info("Unregistering Joda Time Scala Conversions.")
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
      case unknownRef: AnyRef => throw new IllegalArgumentException("Don't know how to serialize an object of type '" + unknownRef.getClass + "'") 
      case unknownVal => throw new IllegalArgumentException("Don't know how to serialize '" + unknownVal + "'")
    }
       
  }

  override def register() = {
    log.info("Hooking up Joda DateTime serializer.")
    /** Encoding hook for MongoDB To be able to persist JodaDateTime DateTime to MongoDB */
    BSON.addEncodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() = {
    log.info("De-registering Joda DateTime serializer.")
    org.bson.BSONEncoders.remove(encodeType)
    super.unregister()
  }
}

trait JodaDateTimeDeserializer extends MongoConversionHelper {

  private val encodeType = classOf[java.util.Date]
  private val transformer = new Transformer {
    log.trace("Decoding JDK Dates .")

    def transform(o: AnyRef): AnyRef = o match {
      case jdkDate: java.util.Date => new DateTime(jdkDate)
      case d: DateTime => {
        log.warning("Transformer got an actual JodaDateTime DateTime?")
        d
      }
      case unknownRef: AnyRef => throw new IllegalArgumentException("Don't know how to serialize an object of type '" + unknownRef.getClass + "'") 
      case unknownVal => throw new IllegalArgumentException("Don't know how to serialize '" + unknownVal + "'")
    }
  }

  override def register() = {
    log.info("Hooking up Joda DateTime deserializer")
    /** Encoding hook for MongoDB To be able to read JodaDateTime DateTime from MongoDB's BSON Date */
    BSON.addDecodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() = {
    log.info("De-registering Joda DateTime dserializer.")
    org.bson.BSONDecoders.remove(encodeType)
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
    log.info("Setting up ScalaRegexSerializers")

    log.info("Hooking up scala.util.matching.Regex serializer")
    /** Encoding hook for MongoDB to translate a Scala Regex to a JAva Regex (which Mongo will understand)*/
    BSON.addEncodingHook(classOf[_root_.scala.util.matching.Regex], transformer)

    super.register()
  }
}

trait ScalaArrayBufferSerializer extends MongoConversionHelper {

  private val transformer = new Transformer {
    import scalaj.collection.Imports._
    log.debug("Encoding a Scala ArrayBuffer.")

    def transform(o: AnyRef): AnyRef = o match {
      case ab: _root_.scala.collection.mutable.ArrayBuffer[_] => ab.asJava
      case _ => o
    }
  }

  override def register() = {
    log.debug("Setting up ScalaArrayBufferSerializers")

    log.debug("Hooking up scala.collection.mutable.ArrayBuffer serializer")
    /** Encoding hook for MongoDB to translate a Scala Regex to a JAva Regex (which Mongo will understand)*/
    BSON.addEncodingHook(classOf[_root_.scala.collection.mutable.ArrayBuffer[_]], transformer)

    super.register()
  }
}

// vim: set ts=2 sw=2 sts=2 et:
