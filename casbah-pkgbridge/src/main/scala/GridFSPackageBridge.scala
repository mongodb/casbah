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
  package mongodb {
    import com.mongodb.Mongo
    package object gridfs {
      @deprecated("The com.novus.casbah.mongodb.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      val GridFS = com.mongodb.casbah.gridfs.GridFS
      @deprecated("The com.novus.casbah.mongodb.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type GridFS = com.mongodb.casbah.gridfs.GridFS

      @deprecated("The com.novus.casbah.mongodb.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type GridFSDBFile = com.mongodb.casbah.gridfs.GridFSDBFile

      @deprecated("The com.novus.casbah.mongodb.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type GridFSFile = com.mongodb.casbah.gridfs.GridFSFile

      @deprecated("The com.novus.casbah.mongodb.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
      type GridFSInputFile = com.mongodb.casbah.gridfs.GridFSInputFile
    }
  }

  package object gridfs {
    @deprecated("The com.novus.casbah.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    val GridFS = com.mongodb.casbah.gridfs.GridFS
    @deprecated("The com.novus.casbah.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type GridFS = com.mongodb.casbah.gridfs.GridFS

    @deprecated("The com.novus.casbah.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type GridFSDBFile = com.mongodb.casbah.gridfs.GridFSDBFile

    @deprecated("The com.novus.casbah.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type GridFSFile = com.mongodb.casbah.gridfs.GridFSFile

    @deprecated("The com.novus.casbah.gridfs package has been changed to com.mongodb.casbah.gridfs; please update your code accordingly. This package alias is provided as a convenience and will be going away in the next major release.")
    type GridFSInputFile = com.mongodb.casbah.gridfs.GridFSInputFile
  }
}
// vim: set ts=2 sw=2 sts=2 et:
