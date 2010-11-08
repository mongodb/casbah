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
package commons 

import org.slf4j.{Logger => SLFLogger,LoggerFactory => SLFLoggerFactory}

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
  @transient @volatile protected[casbah] var log = Logger(this.getClass.getName)
}

/**
 * Scala SLF4J wrapper
 *
 * Example:
 * <pre>
 * class Foo extends Logging {
 *   log.info("My foo is %s","alive")
 *   log.error(new Exception(),"My foo is %s","broken")
 * }
 * </pre>
 *
 * The logger uses String.format:
 * http://download-llnw.oracle.com/javase/6/docs/api/java/lang/String.html#format(java.lang.String,%20java.lang.Object...)
 */
class Logger(val logger: SLFLogger) {
  def name      = logger.getName

  def trace_?   = logger.isTraceEnabled
  def debug_?   = logger.isDebugEnabled
  def info_?    = logger.isInfoEnabled
  def warning_? = logger.isWarnEnabled
  def error_?   = logger.isErrorEnabled

  //Trace
  def trace(t: Throwable, fmt: => String, arg: Any, argN: Any*) {
    trace(t,message(fmt,arg,argN:_*))
  }

  def trace(t: Throwable, msg: => String) {
    if (trace_?) logger.trace(msg,t)
  }

  def trace(fmt: => String, arg: Any, argN: Any*) {
     trace(message(fmt,arg,argN:_*))
  }

  def trace(msg: => String) {
     if (trace_?) logger trace msg
  }

  //Debug
  def debug(t: Throwable, fmt: => String, arg: Any, argN: Any*) {
    debug(t,message(fmt,arg,argN:_*))
  }

  def debug(t: Throwable, msg: => String) {
    if (debug_?) logger.debug(msg,t)
  }

  def debug(fmt: => String, arg: Any, argN: Any*) {
     debug(message(fmt,arg,argN:_*))
  }

  def debug(msg: => String) {
     if (debug_?) logger debug msg
  }

  //Info
  def info(t: Throwable, fmt: => String, arg: Any, argN: Any*) {
    info(t,message(fmt,arg,argN:_*))
  }

  def info(t: Throwable, msg: => String) {
    if (info_?) logger.info(msg,t)
  }

  def info(fmt: => String, arg: Any, argN: Any*) {
     info(message(fmt,arg,argN:_*))
  }

  def info(msg: => String) {
     if (info_?) logger info msg
  }

  //Warning
  def warning(t: Throwable, fmt: => String, arg: Any, argN: Any*) {
    warning(t,message(fmt,arg,argN:_*))
  }

  def warn(t: Throwable, fmt: => String, arg: Any, argN: Any*) = warning(t, fmt, arg, argN)

  def warning(t: Throwable, msg: => String) {
    if (warning_?) logger.warn(msg,t)
  }

  def warn(t: Throwable, msg: => String) = warning(t, msg)

  def warning(fmt: => String, arg: Any, argN: Any*) {
     warning(message(fmt,arg,argN:_*))
  }

  def warn(fmt: => String, arg: Any, argN: Any*) = warning(fmt, arg, argN:_*)

  def warning(msg: => String) {
     if (warning_?) logger warn msg
  }

  def warn(msg: => String) = warning(msg)

  //Error
  def error(t: Throwable, fmt: => String, arg: Any, argN: Any*) {
    error(t,message(fmt,arg,argN:_*))
  }

  def error(t: Throwable, msg: => String) {
    if (error_?) logger.error(msg,t)
  }

  def error(fmt: => String, arg: Any, argN: Any*) {
     error(message(fmt,arg,argN:_*))
  }

  def error(msg: => String) {
     if (error_?) logger error msg
  }

  protected def message(fmt: String, arg: Any, argN: Any*) : String = {
    if ((argN eq null) || argN.isEmpty) fmt.format(arg)
    else fmt.format((arg +: argN):_*)
  }
}

/**
 * Logger factory
 *
 * ex.
 *
 * val logger = Logger("my.cool.logger")
 * val logger = Logger(classOf[Banana])
 * val rootLogger = Logger.root
 *
 */
object Logger {

  /* Uncomment to be able to debug what logging configuration will be used
  {
  import org.slf4j.LoggerFactory
  import ch.qos.logback.classic.LoggerContext
  import ch.qos.logback.core.util.StatusPrinter

  // print logback's internal status
  StatusPrinter.print(LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext])
  }*/

  def apply(logger: String)  : Logger = new Logger(SLFLoggerFactory getLogger logger)
  def apply(clazz: Class[_]) : Logger = apply(clazz.getName)
  def root                   : Logger = apply(SLFLogger.ROOT_LOGGER_NAME)
}
