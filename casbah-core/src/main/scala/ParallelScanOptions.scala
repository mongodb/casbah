/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
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

package com.mongodb.casbah

import com.mongodb.casbah.Imports._

/**
 * The options to use for a parallel collection scan.
 *
 * @since 2.7
 * @see com.mongodb.ParallelScanOptions
 */
object ParallelScanOptions {

  /**
   * Helper to build the parallel scan options.
   *
   * @param numCursors The number of cursors to return, must be greater than 0 and less than 10001
   * @param batchSize The cursor batch size, must be greater than 0
   * @param readPreference A read preference.
   */
  def apply(numCursors: Int, batchSize: Int, readPreference: ReadPreference): ParallelScanOptions = {
    ParallelScanOptions(numCursors, batchSize, Some(readPreference))
  }
}

/**
 * The options to use for a parallel collection scan.
 *
 * @param numCursors The number of cursors to return, must be greater than 0
 * @param batchSize The cursor batch size, must be greater than 0 and less than 10001
 * @param readPreference The optional read preference.
 * @since 2.7
 * @see com.monogdb.ParallelScanOptions
 */
case class ParallelScanOptions(numCursors: Int, batchSize: Int, readPreference: Option[ReadPreference] = None)
