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

import scalaj.collection.Imports._

/** 
 * Helper class for creating WriteConcern instances
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @since 2.0
 * @see com.mongodb.WriteConcern
 */
object WriteConcern {
  /**
   * No exceptions are raised, even for network issues
   */
  val None = com.mongodb.WriteConcern.NONE
  /**
   * Exceptions are raised for network issues but not server errors.
   */
  val Normal = com.mongodb.WriteConcern.NORMAL
  /**
   * Exceptions are raised for network issues and server errors;
   * waits on a server for the write operation
   */
  val Safe = com.mongodb.WriteConcern.SAFE
  /**
   * Exceptions are raised for network issues and server errors;
   * waits on a majority of servers for the write operation
   */
  val Majority = com.mongodb.WriteConcern.MAJORITY
  /**
   * Exceptions are raised for network issues and server errors;
   * Write operations wait for the server to flush data to disk
   */
  val FsyncSafe = com.mongodb.WriteConcern.FSYNC_SAFE
  /**
   * Exceptions are raised for network issues, and server errors;
   * the write operation waits for the server to group commit to the journal file on disk
   */
  val JournalSafe = com.mongodb.WriteConcern.JOURNAL_SAFE
  /**
   * Exceptions are raised for network issues and server errors;
   * waits for at least 2 servers for the write operation.
   */
  val ReplicasSafe = com.mongodb.WriteConcern.REPLICAS_SAFE

  /**
   * Create a new WriteConcern object.
   *
   *	<p> w represents # of servers:
   * 		<ul>
   * 			<li>{@code w=-1} None, no checking is done</li>
   * 			<li>{@code w=0} None, network socket errors raised</li>
   * 			<li>{@code w=1} Checks server for errors as well as network socket errors raised</li>
   * 			<li>{@code w>1} Checks servers (w) for errors as well as network socket errors raised</li>
   * 		</ul>
   * 	</p>
   * @param w (Int) Specifies the number of servers to wait for on the write operation, and exception raising behavior. Defaults to {@code 0}
   * @param wTimeout (Int) Specifies the number MS to wait for the server operations to write.  Defaults to 0 (no timeout)
   * @param fsync (Boolean) Indicates whether write operations should require a sync to disk. Defaults to False
   * @param j whether writes should wait for a journaling group commit
   * @param contineInsertOnError if an error occurs during a bulk insert should the inserts continue anyway
   */
  def apply(w: Int,
            wTimeout: Int = 0,
            fsync: Boolean = false,
            j: Boolean = false,
            continueInsertOnError: Boolean = false) =
    new com.mongodb.WriteConcern(w, wTimeout, fsync, j, continueInsertOnError)

  /**
   * Create a new WriteConcern object.
   *
   *	<p> w is a String representing a valid getLastErrorMode rule (or "majority")
   * @param w (Int) Specifies the getLastErrorMode to apply to the write
   * @param wTimeout (Int) Specifies the number MS to wait for the server operations to write.  Defaults to 0 (no timeout)
   * @param fsync (Boolean) Indicates whether write operations should require a sync to disk. Defaults to False
   * @param j whether writes should wait for a journaling group commit
   * @param contineInsertOnError if an error occurs during a bulk insert should the inserts continue anyway
   */
  def withRule(w: String, 
               wTimeout: Int = 0,
               fsync: Boolean = false,
               j: Boolean = false,
               continueInsertOnError: Boolean = false) =
    new com.mongodb.WriteConcern(w, wTimeout, fsync, j, continueInsertOnError)

  /**
   * Get the WriteConcern constants by name: NONE, NORMAL, SAFE, MAJORITY, FSYNC_SAFE,
   * JOURNAL_SAFE, REPLICAS_SAFE. (matching is done case insensitively)
   *
   * NOTE: This only supports the java versions, no support for the local scala aliases.
   */
  def valueOf(name: String) = com.mongodb.WriteConcern.valueOf(name)
}

// vim: set ts=2 sw=2 sts=2 et:
