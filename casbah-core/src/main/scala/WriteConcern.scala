/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
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

package com.mongodb.casbah

import com.mongodb.{WriteConcern => JWriteConcern}

/**
 * Helper class for creating WriteConcern instances
 *
 * @since 2.0
 * @see JWriteConcern
 */
object WriteConcern {
  /**
   * No exceptions are raised, even for network issues
   */
  val None: JWriteConcern = JWriteConcern.NONE
  /**
   * Exceptions are raised for network issues but not server errors.
   */
  val Normal: JWriteConcern = JWriteConcern.NORMAL
  /**
   * Exceptions are raised for network issues and server errors;
   * waits on a server for the write operation
   */
  val Safe: JWriteConcern = JWriteConcern.SAFE
  /**
   * Exceptions are raised for network issues and server errors;
   * waits on a majority of servers for the write operation
   */
  val Majority: JWriteConcern = JWriteConcern.MAJORITY
  /**
   * Exceptions are raised for network issues and server errors;
   * Write operations wait for the server to flush data to disk
   */
  val FsyncSafe: JWriteConcern = JWriteConcern.FSYNC_SAFE
  /**
   * Exceptions are raised for network issues, and server errors;
   * the write operation waits for the server to group commit to the journal file on disk
   */
  val JournalSafe: JWriteConcern = JWriteConcern.JOURNAL_SAFE
  /**
   * Exceptions are raised for network issues and server errors;
   * waits for at least 2 servers for the write operation.
   */
  val ReplicasSafe: JWriteConcern = JWriteConcern.REPLICAS_SAFE

  /**
   * No exceptions are raised, even for network issues.
   * @since 2.7
   */
  val ErrorsIgnored: JWriteConcern = JWriteConcern.ERRORS_IGNORED
  /**
   * Write operations that use this write concern will wait for acknowledgement from the primary server before returning.
   * Exceptions are raised for network issues, and server errors.
   * @since 2.7
   */
  val Acknowledged: JWriteConcern = JWriteConcern.ACKNOWLEDGED
  /**
   * Write operations that use this write concern will return as soon as the message is written to the socket.
   * Exceptions are raised for network issues, but not server errors.
   * @since 2.7
   */
  val Unacknowledged: JWriteConcern = JWriteConcern.UNACKNOWLEDGED
  /**
   * Exceptions are raised for network issues, and server errors; the write operation waits for the server to flush
   * the data to disk.
   * @since 2.7
   */
  val Fsynced: JWriteConcern = JWriteConcern.FSYNCED
  /**
   * Exceptions are raised for network issues, and server errors; the write operation waits for the server to
   * group commit to the journal file on disk.
   * @since 2.7
   */
  val Journaled: JWriteConcern = JWriteConcern.JOURNALED
  /**
   * Exceptions are raised for network issues, and server errors; waits for at least 2 servers for the write operation.
   * @since 2.7
   */
  val ReplicaAcknowledged: JWriteConcern = JWriteConcern.REPLICA_ACKNOWLEDGED

  /**
   * Create a new WriteConcern object.
   *
   * <p> w represents # of servers:
   * <ul>
   * <li><code>w=-1</code> None, no checking is done</li>
   * <li><code>w=0</code> None, network socket errors raised</li>
   * <li><code>w=1</code> Checks server for errors as well as network socket errors raised</li>
   * <li><code>w>1</code> Checks servers (w) for errors as well as network socket errors raised</li>
   * </ul>
   * </p>
   * @param w (Int) Specifies the number of servers to wait for on the write operation, and exception raising behavior. Defaults to <code>0</code>
   * @param wTimeout (Int) Specifies the number MS to wait for the server operations to write.  Defaults to 0 (no timeout)
   * @param fsync (Boolean) Indicates whether write operations should require a sync to disk.
   *              When using a Journal this is the same as using `j=true`.
   *              Will msync() only if mongod is *not* running with a journal.
   *              Cannot be used with `j`. Defaults to False
   * @param j whether writes should wait for a journaling group commit. Cannot be used with `fsync`.
   * @param continueInsertOnError if an error occurs during a bulk insert should the inserts continue anyway
   */
  def apply(w: Int,
            wTimeout: Int = 0,
            fsync: Boolean = false,
            j: Boolean = false,
            continueInsertOnError: Boolean = false): JWriteConcern =
    new JWriteConcern(w, wTimeout, fsync, j, continueInsertOnError)

  /**
   * Create a new WriteConcern object.
   *
   * <p> w is a String representing a valid getLastErrorMode rule (or "majority")
   * @param w (Int) Specifies the getLastErrorMode to apply to the write
   * @param wTimeout (Int) Specifies the number MS to wait for the server operations to write.  Defaults to 0 (no timeout)
   * @param fsync (Boolean) Indicates whether write operations should require a sync to disk.
   *              When using a Journal this is the same as using `j=true`.
   *              Will msync() only if mongod is *not* running with a journal.
   *              Cannot be used with `j`. Defaults to False
   * @param j whether writes should wait for a journaling group commit. Cannot be used with `fsync`.
   * @param continueInsertOnError if an error occurs during a bulk insert should the inserts continue anyway
   */
  def withRule(w: String,
               wTimeout: Int = 0,
               fsync: Boolean = false,
               j: Boolean = false,
               continueInsertOnError: Boolean = false): JWriteConcern =
    new JWriteConcern(w, wTimeout, fsync, j, continueInsertOnError)

  /**
   * Get the WriteConcern constants by name: NONE, NORMAL, SAFE, MAJORITY, FSYNC_SAFE,
   * JOURNAL_SAFE, REPLICAS_SAFE. (matching is done case insensitively)
   *
   * NOTE: This only supports the java versions, no support for the local scala aliases.
   */
  def valueOf(name: String): Option[JWriteConcern] = Option(JWriteConcern.valueOf(name))
}
