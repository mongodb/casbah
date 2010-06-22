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

import com.novus.casbah.util.Logging 

import com.mongodb._
import org.bson.{BSON, Transformer}

import org.scala_tools.time.Imports._

package object scala {
  implicit object Converters extends Serializers
                                with Deserializers

  /** 
   * Converters for reading Scala types from MongoDB
   *
   * These should be setup in a way which requires
   * an explicit invocation / registration of individual
   * deserializers, else unexpected behavior will occur.
   *
   * @author Brendan W. McAdams <bmcadams@novus.com>
   * @version 1.0, 06/22/10
   * @since 1.0
   */
  trait Deserializers

  /** 
   * Converters for saving Scala types to MongoDB
   *
   * For the most part these are 'safe' to enable automatically,
   * as they won't break existing code.
   * Be very careful with the deserializers however as they can come with
   * unexpected behavior.
   *
   * @author Brendan W. McAdams <bmcadams@novus.com>
   * @version 1.0, 06/22/10
   * @since 1.0
   */
  trait Serializers extends MongoSerializationHelper 
                       with JodaTimeSerializer
                       with ScalaRegexSerializer {
    override def register() =  {
      log.info("Serializers for Scala Conversions registering")
      super.register()
    }
  }


  trait JodaTimeSerializer extends MongoSerializationHelper {

    override def register() = {
      log.info("Setting up Joda Time Serializers")

      log.info("Hooking up Joda DateTime serializer")
      /** Encoding hook for MongoDB To be able to persist JodaTime DateTime to MongoDB */
      BSON.addEncodingHook(classOf[DateTime], new Transformer {
        val fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

        def transform(o: AnyRef): AnyRef = o match {
          case d: DateTime => "\"%s\"".format(fmt.print(d))
          case _ => o
        }
           
      })

      super.register()
    }
  }
    
  trait ScalaRegexSerializer extends MongoSerializationHelper {

    override def register() = {
      log.info("Setting up ScalaRegexSerializers")

      log.info("Hooking up scala.util.matching.Regex serializer")
      /** Encoding hook for MongoDB to translate a Scala Regex to a JAva Regex (which Mongo will understand)*/
      BSON.addEncodingHook(classOf[_root_.scala.util.matching.Regex], new Transformer {

        def transform(o: AnyRef): AnyRef = o match {
          case sRE: _root_.scala.util.matching.Regex => sRE.pattern
          case _ => o
        }
           
      })
      super.register()
    }
  }
}

// vim: set ts=2 sw=2 sts=2 et:
