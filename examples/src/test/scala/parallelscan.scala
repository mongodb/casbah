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


import java.io.{ByteArrayOutputStream, PrintStream}

import scala.language.reflectiveCalls

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON

/**
 * An example program showing an example using parallelScan
 *
 * {{{
 * parallelscan.main("mongodb://localhost/test.testData")
 * }}}
 *
 */
object parallelscan {
  val usage = """
                |An example using a parallel scan.
                |
                |Example:
                | ./parallelscan.scala -u mongodb://localhost/test.testData > test.out
                |
                |Options:
                |  --help                                produce help message
                |  --quiet                               silence all non error diagnostic
                |                                        messages
                |  -u [ --uri ] arg                      The connection URI - must contain a collection
                |                                        mongodb://[username:password@]host1[:port1][,host2[:port2]]/database.collection[?options]
                |                                        See: http://docs.mongodb.org/manual/reference/connection-string/
                |  -t [ --threads ] arg                  Number of threads / cursors to create. Defaults to 3
                |  -b [ --batchSize] arg                 Batch Size for a cursor. Defaults to 1000
              """.stripMargin

  /**
   * The main parallel scan program
   * Outputs debug information to Console.err - as Console.out is probably redirected to a file
   *
   * @param args the commandline arguments
   */
  def main(args: Array[String]) {

    /*The time when the execution of this program started, in milliseconds since 1 January 1970 UTC. */
    val executionStart: Long = currentTime

    if (args.length == 0 | args.contains("--help")) {
      Console.err.println(usage)
      sys.exit(1)
    }

    handleSLF4J()

    val optionMap = parseArgs(Map(), args.toList)
    val options = getOptions(optionMap)

    if (options.uri == None) {
      Console.err.println(s"Missing URI")
      Console.err.println(usage)
      sys.exit(1)
    }

    // Get URI
    val mongoClientURI = MongoClientURI(options.uri.get)
    if (mongoClientURI.collection == None) {
      Console.err.println(s"Missing collection name in the URI eg:  mongodb://<hostInformation>/<database>.<collection>[?options]")
      Console.err.println(s"Current URI: $mongoClientURI")
      sys.exit(1)
    }

    // Get the collection
    val mongoClient = MongoClient(mongoClientURI)
    val collection = mongoClient(mongoClientURI.database.get)(mongoClientURI.collection.get)

    val readPreference = options.slaveOK match {
      case true => ReadPreference.SecondaryPreferred
      case false => ReadPreference.Primary
    }

    val parallelScanOptions = ParallelScanOptions(options.threads.toInt, options.batchSize.toInt, Some(readPreference))
    val cursors = collection.parallelScan(parallelScanOptions)

    if (!options.quiet) Console.err.print("Parallelizing...")

    // Map each cursor to a future and with each cursor output the doc
    val futureOutput = Future.sequence(
      cursors.map(cursor => {
        Future {
          for (doc <- cursor) Console.out.println(JSON.serialize(doc))
        }
      })
    )
    showPinWheel(futureOutput)

    val total = currentTime - executionStart
    if (!options.quiet) Console.err.println(s"Finished: $total ms")
  }

  /**
   * Recursively convert the args list into a Map of options
   *
   * @param map - the initial option map
   * @param args - the args list
   * @return the parsed OptionMap
   */
  private def parseArgs(map: Map[String, Any], args: List[String]): Map[String, Any] = {
    args match {
      case Nil => map
      case "--quiet" :: tail =>
        parseArgs(map ++ Map("quiet" -> true), tail)
      case "-u" :: value :: tail =>
        parseArgs(map ++ Map("uri" -> value), tail)
      case "--uri" :: value :: tail =>
        parseArgs(map ++ Map("uri" -> value), tail)
      case "-t" :: value :: tail =>
        parseArgs(map ++ Map("threads" -> value), tail)
      case "--threads" :: value :: tail =>
        parseArgs(map ++ Map("threads" -> value), tail)
      case "-b" :: value :: tail =>
        parseArgs(map ++ Map("batchSize" -> value), tail)
      case "--batchSize" :: value :: tail =>
        parseArgs(map ++ Map("batchSize" -> value), tail)
      case "-k" :: value :: tail =>
        parseArgs(map ++ Map("slaveOk" -> value), tail)
      case "--slaveOk" :: value :: tail =>
        parseArgs(map ++ Map("slaveOk" -> value), tail)
      case option :: tail =>
        Console.err.println("Unknown option " + option)
        Console.err.println(usage)
        sys.exit(1)
    }
  }

  /**
   * Convert the optionMap to an Options instance
   * @param optionMap the parsed args options
   * @return Options instance
   */
  private def getOptions(optionMap: Map[String, _]): Options = {
    val default = Options()
    Options(
      quiet = optionMap.getOrElse("quiet", default.quiet).asInstanceOf[Boolean],
      uri = optionMap.get("uri") match {
        case None => default.uri
        case Some(value) => Some(value.asInstanceOf[String])
      },
      threads = optionMap.getOrElse("threads", default.threads).asInstanceOf[String],
      batchSize = optionMap.getOrElse("batchSize", default.batchSize).asInstanceOf[String],
      slaveOK = optionMap.getOrElse("slaveOK", default.slaveOK).asInstanceOf[Boolean],
      jsonArray = optionMap.getOrElse("jsonArray", default.jsonArray).asInstanceOf[Boolean]
    )
  }

  case class Options(quiet: Boolean = false, uri: Option[String] = None, threads: String = "3",
                     batchSize: String = "500", slaveOK: Boolean = true, jsonArray: Boolean = false)

  private def currentTime = System.currentTimeMillis()

  /**
   * Hack to hide any SLF4J NOP stderr messages
   */
  private def handleSLF4J() {
    val stderr = Console.err
    val err = new PrintStream(new ByteArrayOutputStream())
    System.setErr(err)
    MongoClientURI("mongodb://localhost")
    System.setErr(stderr)
  }

  /**
   * Shows a pinWheel in the console.err
   * @param someFuture the future we are all waiting for
   */
  private def showPinWheel(someFuture: Future[_]) {
    // Let the user know something is happening until futureOutput isCompleted
    val spinChars = List("|", "/", "-", "\\")
    while (!someFuture.isCompleted) {
      spinChars.foreach({
        case char =>
          Console.err.print(char)
          Thread sleep 200
          Console.err.print("\b")
      })
    }
    Console.err.println("")
  }

}

