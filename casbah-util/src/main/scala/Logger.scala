/**
 * Copyright (c) 2010, Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: This Logging Class is derived from the Apache License 2.0 Akka Project,
 * available from http://akkasource.org/
 */

package com.novus.casbah 
package util

import net.lag.logging.Logger

import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Base trait for all classes that wants to be able use the logging infrastructure.
 *
 * @author <a href="http://jonasboner.com">Jonas Bon&#233;r</a>
 */
trait Logging {
  @transient @volatile protected[casbah] var log = Logger.get(this.getClass.getName)
}

/**
 * LoggableException is a subclass of Exception and can be used as the base exception
 * for application specific exceptions.
 * <p/>
 * It keeps track of the exception is logged or not and also stores the unique id,
 * so that it can be carried all along to the client tier and displayed to the end user.
 * The end user can call up the customer support using this number.
 *
 * @author <a href="http://jonasboner.com">Jonas Bon&#233;r</a>
 */
class LoggableException extends Exception with Logging {
  private val uniqueId = getExceptionID
  private var originalException: Option[Exception] = None
  private var isLogged = false

  def this(baseException: Exception) = {
    this()
    originalException = Some(baseException)
  }

  def logException = synchronized {
    if (!isLogged) {
      originalException match {
        case Some(e) => log.error("Logged Exception [%s] %s", uniqueId, getStackTraceAsString(e))
        case None => log.error("Logged Exception [%s] %s", uniqueId, getStackTraceAsString(this))
      }
      isLogged = true
    }
  }

  private def getExceptionID: String = {
    val hostname: String = try {
      InetAddress.getLocalHost.getHostName
    } catch {
      case e: UnknownHostException =>
        log.error("Could not get hostname to generate loggable exception")
        "N/A"
    }
    hostname + "_" + System.currentTimeMillis
  }

  private def getStackTraceAsString(exception: Throwable): String = {
    val sw = new StringWriter
    val pw = new PrintWriter(sw)
    exception.printStackTrace(pw)
    sw.toString
  }
}
