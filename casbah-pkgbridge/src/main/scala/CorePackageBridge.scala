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


/** 
 * BRIDGE CODE.
 * Type aliases for deprecated object names (aka the "Old" object naming)
 * These should not be used and are provided for backwards compatibility only.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.1
 * @since 1.0
 * @deprecated The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.
 */
package com.novus.casbah {
  package object mongodb {
    import com.mongodb.Mongo

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val BaseImports = com.mongodb.casbah.BaseImports
    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type BaseImports = com.mongodb.casbah.BaseImports

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val MongoTypeImports = com.mongodb.casbah.TypeImports
    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoTypeImports = com.mongodb.casbah.TypeImports

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val TypeImports = com.mongodb.casbah.TypeImports
    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type TypeImports = com.mongodb.casbah.TypeImports
    
    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val Implicits = com.mongodb.casbah.Implicits
    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type Implicits = com.mongodb.casbah.Implicits

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val Imports = com.mongodb.casbah.Imports
    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type Imports = com.mongodb.casbah.Imports

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoCollection = com.mongodb.casbah.MongoCollection
    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoCollectionWrapper = com.mongodb.casbah.MongoCollectionWrapper

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val MongoConnection = com.mongodb.casbah.MongoConnection
    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoConnection = com.mongodb.casbah.MongoConnection

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoCursor = com.mongodb.casbah.MongoCursor

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoCursorWrapper[A <: com.mongodb.DBObject] = com.mongodb.casbah.MongoCursorWrapper[A]

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoDB = com.mongodb.casbah.MongoDB

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val MongoDBAddress = com.mongodb.casbah.MongoDBAddress

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val MongoDBObject = com.mongodb.casbah.commons.MongoDBObject
    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoDBObject = com.mongodb.casbah.commons.MongoDBObject

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoDBObjectBuilder = com.mongodb.casbah.commons.MongoDBObjectBuilder

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoTypedCollection[A <: com.mongodb.DBObject] = com.mongodb.casbah.MongoTypedCollection[A]

    @deprecated("The com.novus.casbah.mongodb package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoTypedCursor[A <: com.mongodb.DBObject] = com.mongodb.casbah.MongoTypedCursor[A]
  }
  package mongodb {
    package object conversions {
      @deprecated("The com.novus.casbah.mongodb.conversions package has been changed to com.mongodb.casbah.conversions; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type MongoConversionHelper = com.mongodb.casbah.conversions.MongoConversionHelper
    }

    package object scala {
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      val DeregisterConversionHelpers = com.mongodb.casbah.conversions.scala.DeregisterConversionHelpers

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      val DeregisterJodaTimeConversionHelpers = com.mongodb.casbah.conversions.scala.DeregisterJodaTimeConversionHelpers

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type Deserializers = com.mongodb.casbah.conversions.scala.Deserializers

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type JodaDateTimeDeserializer = com.mongodb.casbah.conversions.scala.JodaDateTimeDeserializer
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type JodaDateTimeHelpers = com.mongodb.casbah.conversions.scala.JodaDateTimeHelpers
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type JodaDateTimeSerializer = com.mongodb.casbah.conversions.scala.JodaDateTimeSerializer

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      val RegisterConversionHelpers = com.mongodb.casbah.conversions.scala.RegisterConversionHelpers
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      val RegisterJodaTimeConversionHelpers = com.mongodb.casbah.conversions.scala.RegisterJodaTimeConversionHelpers

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type ScalaJCollectionSerializer = com.mongodb.casbah.conversions.scala.ScalaJCollectionSerializer
        
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type ScalaRegexSerializer = com.mongodb.casbah.conversions.scala.ScalaRegexSerializer

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type Serializers = com.mongodb.casbah.conversions.scala.Serializers
    }


    package object map_reduce {
      @deprecated("The com.novus.casbah.mongodb.map_reduce package has been changed to com.mongodb.casbah.map_reduce; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      val MapReduceCommand = com.mongodb.casbah.map_reduce.MapReduceCommand
      @deprecated("The com.novus.casbah.mongodb.map_reduce package has been changed to com.mongodb.casbah.map_reduce; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type MapReduceCommand = com.mongodb.casbah.map_reduce.MapReduceCommand
  
      @deprecated("The com.novus.casbah.mongodb.map_reduce package has been changed to com.mongodb.casbah.map_reduce; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type MapReduceError = com.mongodb.casbah.map_reduce.MapReduceError

      @deprecated("The com.novus.casbah.mongodb.map_reduce package has been changed to com.mongodb.casbah.map_reduce; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type MapReduceResult = com.mongodb.casbah.map_reduce.MapReduceResult
    }
  }

  package object conversions {
    @deprecated("The com.novus.casbah.conversions package has been changed to com.mongodb.casbah.conversions; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoConversionHelper = com.mongodb.casbah.conversions.MongoConversionHelper
  }

  package object scala {
    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val DeregisterConversionHelpers = com.mongodb.casbah.conversions.scala.DeregisterConversionHelpers

    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val DeregisterJodaTimeConversionHelpers = com.mongodb.casbah.conversions.scala.DeregisterJodaTimeConversionHelpers

    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type Deserializers = com.mongodb.casbah.conversions.scala.Deserializers

    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type JodaDateTimeDeserializer = com.mongodb.casbah.conversions.scala.JodaDateTimeDeserializer
    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type JodaDateTimeHelpers = com.mongodb.casbah.conversions.scala.JodaDateTimeHelpers
    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type JodaDateTimeSerializer = com.mongodb.casbah.conversions.scala.JodaDateTimeSerializer

    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val RegisterConversionHelpers = com.mongodb.casbah.conversions.scala.RegisterConversionHelpers
    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val RegisterJodaTimeConversionHelpers = com.mongodb.casbah.conversions.scala.RegisterJodaTimeConversionHelpers

    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type ScalaJCollectionSerializer = com.mongodb.casbah.conversions.scala.ScalaJCollectionSerializer
      
    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type ScalaRegexSerializer = com.mongodb.casbah.conversions.scala.ScalaRegexSerializer

    @deprecated("The com.novus.casbah.conversions.scala package has been changed to com.mongodb.casbah.conversions.scala; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type Serializers = com.mongodb.casbah.conversions.scala.Serializers
  }

  package object map_reduce {
    @deprecated("The com.novus.casbah.map_reduce package has been changed to com.mongodb.casbah.map_reduce; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val MapReduceCommand = com.mongodb.casbah.map_reduce.MapReduceCommand

    @deprecated("The com.novus.casbah.map_reduce package has been changed to com.mongodb.casbah.map_reduce; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MapReduceCommand = com.mongodb.casbah.map_reduce.MapReduceCommand

    @deprecated("The com.novus.casbah.map_reduce package has been changed to com.mongodb.casbah.map_reduce; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MapReduceError = com.mongodb.casbah.map_reduce.MapReduceError

    @deprecated("The com.novus.casbah.map_reduce package has been changed to com.mongodb.casbah.map_reduce; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MapReduceResult = com.mongodb.casbah.map_reduce.MapReduceResult
  }

}


package com.novus {
  package object casbah {
    import com.mongodb.Mongo

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val BaseImports = com.mongodb.casbah.BaseImports

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type BaseImports = com.mongodb.casbah.BaseImports

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val MongoTypeImports = com.mongodb.casbah.TypeImports
    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoTypeImports = com.mongodb.casbah.TypeImports

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val TypeImports = com.mongodb.casbah.TypeImports
    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type TypeImports = com.mongodb.casbah.TypeImports
    
    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val Implicits = com.mongodb.casbah.Implicits
    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type Implicits = com.mongodb.casbah.Implicits

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val Imports = com.mongodb.casbah.Imports
    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type Imports = com.mongodb.casbah.Imports

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoCollection = com.mongodb.casbah.MongoCollection
    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoCollectionWrapper = com.mongodb.casbah.MongoCollectionWrapper

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val MongoConnection = com.mongodb.casbah.MongoConnection
    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoConnection = com.mongodb.casbah.MongoConnection

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoCursor = com.mongodb.casbah.MongoCursor

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoCursorWrapper[A <: com.mongodb.DBObject] = com.mongodb.casbah.MongoCursorWrapper[A]

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoDB = com.mongodb.casbah.MongoDB

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val MongoDBAddress = com.mongodb.casbah.MongoDBAddress

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val MongoDBObject = com.mongodb.casbah.commons.MongoDBObject
    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoDBObject = com.mongodb.casbah.commons.MongoDBObject

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoDBObjectBuilder = com.mongodb.casbah.commons.MongoDBObjectBuilder

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoTypedCollection[A <: com.mongodb.DBObject] = com.mongodb.casbah.MongoTypedCollection[A]

    @deprecated("The com.novus.casbah package has been changed to com.mongodb.casbah; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type MongoTypedCursor[A <: com.mongodb.DBObject] = com.mongodb.casbah.MongoTypedCursor[A]
  }
}

// vim: set ts=2 sw=2 sts=2 et:
