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

package com.mongodb.casbah
package gridfs

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._

import scalaj.collection.Imports._


trait Implicits {
  implicit def wrapDBFile(in: com.mongodb.gridfs.GridFSDBFile) = new GridFSDBFile(in)
  implicit def wrapInFile(in: com.mongodb.gridfs.GridFSInputFile) = new GridFSInputFile(in)
}

object Implicits extends Implicits
object Imports extends Imports
object BaseImports extends BaseImports 
object TypeImports extends TypeImports

trait Imports extends BaseImports with TypeImports with Implicits 

trait BaseImports {
  val GridFS = com.mongodb.casbah.gridfs.GridFS
}

trait TypeImports {
  type GridFS = com.mongodb.casbah.gridfs.GridFS
  type GridFSDBFile = com.mongodb.casbah.gridfs.GridFSDBFile
  type GridFSInputFile = com.mongodb.casbah.gridfs.GridFSInputFile
  type GridFSFile = com.mongodb.casbah.gridfs.GridFSFile
}
// vim: set ts=2 sw=2 sts=2 et:
