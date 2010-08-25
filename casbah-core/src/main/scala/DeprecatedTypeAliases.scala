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
 *     http://github.com/novus/casbah
 * 
 */

import com.novus.casbah.Imports._

/** 
 * BRIDGE CODE.
 * Type aliases for deprecated object names (aka the "Old" object naming)
 * These should not be used and are provided for backwards compatibility only.
 *
 * @author Brendan W. McAdams <bmcadams@novus.com>
 * @version 1.1
 * @since 1.0
 * @deprecated The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.
 */

package com.novus.casbah {
  package object mongodb {
    import com.mongodb.Mongo

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    val BaseImports = com.novus.casbah.BaseImports
    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type BaseImports = com.novus.casbah.BaseImports

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    val MongoTypeImports = com.novus.casbah.TypeImports
    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoTypeImports = com.novus.casbah.TypeImports

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    val TypeImports = com.novus.casbah.TypeImports
    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type TypeImports = com.novus.casbah.TypeImports
    
    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    val Implicits = com.novus.casbah.Implicits
    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type Implicits = com.novus.casbah.Implicits

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    val Imports = com.novus.casbah.Imports
    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type Imports = com.novus.casbah.Imports

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoCollection = com.novus.casbah.MongoCollection
    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoCollectionWrapper = com.novus.casbah.MongoCollectionWrapper

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    val MongoConnection = com.novus.casbah.MongoConnection
    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoConnection = com.novus.casbah.MongoConnection

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoCursor = com.novus.casbah.MongoCursor

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoCursorWrapper[A <: DBObject] = com.novus.casbah.MongoCursorWrapper[A]

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoDB = com.novus.casbah.MongoDB

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    val MongoDBAddress = com.novus.casbah.MongoDBAddress

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    val MongoDBObject = com.novus.casbah.commons.MongoDBObject
    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoDBObject = com.novus.casbah.commons.MongoDBObject

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoDBObjectBuilder = com.novus.casbah.commons.MongoDBObjectBuilder

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoTypedCollection[A <: DBObject] = com.novus.casbah.MongoTypedCollection[A]

    @deprecated("The com.novus.casbah.mongodb package has been deprecated in favor of com.novus.casbah; please update your code accordingly.")
    type MongoTypedCursor[A <: DBObject] = com.novus.casbah.MongoTypedCursor[A]
  }
  package mongodb {
    package object conversions {
      @deprecated("The com.novus.casbah.mongodb.conversions package has been deprecated in favor of com.novus.casbah.conversions; please update your code accordingly.")
      type MongoConversionHelper = com.novus.casbah.conversions.MongoConversionHelper
    }
    package object scala {
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      val DeregisterConversionHelpers = com.novus.casbah.conversions.scala.DeregisterConversionHelpers

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      val DeregisterJodaTimeConversionHelpers = com.novus.casbah.conversions.scala.DeregisterJodaTimeConversionHelpers

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      type Deserializers = com.novus.casbah.conversions.scala.Deserializers

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      type JodaDateTimeDeserializer = com.novus.casbah.conversions.scala.JodaDateTimeDeserializer
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      type JodaDateTimeHelpers = com.novus.casbah.conversions.scala.JodaDateTimeHelpers
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      type JodaDateTimeSerializer = com.novus.casbah.conversions.scala.JodaDateTimeSerializer

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      val RegisterConversionHelpers = com.novus.casbah.conversions.scala.RegisterConversionHelpers
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      val RegisterJodaTimeConversionHelpers = com.novus.casbah.conversions.scala.RegisterJodaTimeConversionHelpers

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      type ScalaJCollectionSerializer = com.novus.casbah.conversions.scala.ScalaJCollectionSerializer
        
      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      type ScalaRegexSerializer = com.novus.casbah.conversions.scala.ScalaRegexSerializer

      @deprecated("The com.novus.casbah.mongodb.conversions.scala package has been deprecated in favor of com.novus.casbah.conversions.scala; please update your code accordingly.")
      type Serializers = com.novus.casbah.conversions.scala.Serializers
    }

    /*package object gridfs {
      @deprecated("The com.novus.casbah.mongodb.gridfs package has been deprecated in favor of com.novus.casbah.gridfs; please update your code accordingly.")
      val GridFS = com.novus.casbah.gridfs.GridFS
      @deprecated("The com.novus.casbah.mongodb.gridfs package has been deprecated in favor of com.novus.casbah.gridfs; please update your code accordingly.")
      type GridFS = com.novus.casbah.gridfs.GridFS

      @deprecated("The com.novus.casbah.mongodb.gridfs package has been deprecated in favor of com.novus.casbah.gridfs; please update your code accordingly.")
      type GridFSDBFile = com.novus.casbah.gridfs.GridFSDBFile

      @deprecated("The com.novus.casbah.mongodb.gridfs package has been deprecated in favor of com.novus.casbah.gridfs; please update your code accordingly.")
      type GridFSFile = com.novus.casbah.gridfs.GridFSFile

      @deprecated("The com.novus.casbah.mongodb.gridfs package has been deprecated in favor of com.novus.casbah.gridfs; please update your code accordingly.")
      type GridFSInputFile = com.novus.casbah.gridfs.GridFSInputFile
    }*/

    package object map_reduce {
      @deprecated("The com.novus.casbah.mongodb.map_reduce package has been deprecated in favor of com.novus.casbah.map_reduce; please update your code accordingly.")
      val MapReduceCommand = com.novus.casbah.map_reduce.MapReduceCommand
      @deprecated("The com.novus.casbah.mongodb.map_reduce package has been deprecated in favor of com.novus.casbah.map_reduce; please update your code accordingly.")
      type MapReduceCommand = com.novus.casbah.map_reduce.MapReduceCommand
  
      @deprecated("The com.novus.casbah.mongodb.map_reduce package has been deprecated in favor of com.novus.casbah.map_reduce; please update your code accordingly.")
      type MapReduceError = com.novus.casbah.map_reduce.MapReduceError

      @deprecated("The com.novus.casbah.mongodb.map_reduce package has been deprecated in favor of com.novus.casbah.map_reduce; please update your code accordingly.")
      type MapReduceResult = com.novus.casbah.map_reduce.MapReduceResult
    }
  }

}
