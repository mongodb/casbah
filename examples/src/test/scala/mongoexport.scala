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

import java.io.{ByteArrayOutputStream, PrintStream, PrintWriter}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON

/**
 * An example program providing similar functionality as the ``mongoexport`` program
 *
 * As there is no core CSV library for Scala CSV export is an exercise left to the reader.
 *
 * {{{
 *  mongoexport.main("mongodb://localhost/test.testData")
 * }}}
 *
 */
object mongoexport {
  val usage = """
                |Export MongoDB data to JSON files.
                |
                |Example:
                |  ./mongoexport.scala -u mongodb://localhost/test.testData > ./data/testData.json
                |
                |Options:
                |  --help                                produce help message
                |  --quiet                               silence all non error diagnostic
                |                                        messages
                |  -u [ --uri ] arg                      The connection URI - must contain a collection
                |                                        mongodb://[username:password@]host1[:port1][,host2[:port2]]/database.collection[?options]
                |                                        See: http://docs.mongodb.org/manual/reference/connection-string/
                |  -f [ --fields ] arg                   comma separated list of field names
                |                                        e.g. -f name,age
                |  -q [ --query ] arg                    query filter, as a JSON string, e.g.,
                |                                        '{x:{$gt:1}}'
                |  -o [ --out ] arg                      output file; if not specified, stdout
                |                                        is used
                |  --jsonArray                           output to a json array rather than one
                |                                        object per line
                |  -k [ --slaveOk ] arg (=1)             use secondaries for export if
                |                                        available, default true
                |  --skip arg (=0)                       documents to skip, default 0
                |  --limit arg (=0)                      limit the numbers of documents
                |                                        returned, default all
                |  --sort arg                            sort order, as a JSON string, e.g.,
                |                                        '{x:1}'
              """.stripMargin

  /**
   * The main export program
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
    // output
    val output: PrintWriter = options.out match {
      case None => new PrintWriter(System.out)
      case Some(fileName) => new PrintWriter(fileName)
    }

    // Export JSON
    val exporter = future { exportJson(collection, output, options) }

    if (!options.quiet) Console.err.print("Exporting...")
    showPinWheel(exporter)
    val total = currentTime - executionStart
    if (!options.quiet) Console.err.println(s"Finished: $total ms")
  }

  /**
   * Imports JSON into the collection
   *
   * @param collection the collection to import into
   * @param output the data source
   * @param options the configuration options
   */
  private def exportJson(collection: MongoCollection, output: PrintWriter, options: Options) {

    val (startString, endString, lineEndString) = options.jsonArray match {
      case true => ("[", "]\r\n", ",")
      case false => ("", "\r\n", "\r\n")
    }

    options.slaveOK match {
      case true => collection.setReadPreference(ReadPreference.SecondaryPreferred)
      case false => collection
    }

    val cursor = collection.find(options.query)
    options.skip match {
      case None => cursor
      case Some(value) => cursor.skip(value)
    }
    options.limit match {
      case None => cursor
      case Some(value) => cursor.limit(value)
    }
    options.sort match {
      case None => cursor
      case Some(value) => cursor.sort(value)
    }

    output.write(startString)
    for (doc <- cursor) {
      val jsonDoc = JSON.serialize(doc)
      val lineEnd = cursor.hasNext match {
        case true => lineEndString
        case false => endString
      }
      output.write(s"$jsonDoc$lineEnd")
    }
    output.close()
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
      case "-f" :: value :: tail =>
        parseArgs(map ++ Map("fields" -> value.split(",").toList), tail)
      case "--file" :: value :: tail =>
        parseArgs(map ++ Map("fields" -> value.split(",").toList), tail)
      case "-q" :: value :: tail =>
        parseArgs(map ++ Map("query" -> value), tail)
      case "--query" :: value :: tail =>
        parseArgs(map ++ Map("query" -> value), tail)
      case "-o" :: value :: tail =>
        parseArgs(map ++ Map("out" -> value), tail)
      case "--out" :: value :: tail =>
        parseArgs(map ++ Map("out" -> value), tail)
      case "-k" :: value :: tail =>
        parseArgs(map ++ Map("slaveOk" -> value), tail)
      case "--slaveOk" :: value :: tail =>
        parseArgs(map ++ Map("slaveOk" -> value), tail)
      case "--skip" :: value :: tail =>
        parseArgs(map ++ Map("skip" -> value), tail)
      case "--limit" :: value :: tail =>
        parseArgs(map ++ Map("limit" -> value), tail)
      case "--sort" :: value :: tail =>
        parseArgs(map ++ Map("sort" -> value), tail)
      case "--jsonArray" :: tail =>
        parseArgs(map ++ Map("jsonArray" -> true), tail)
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
    val options = Options(
      quiet = optionMap.getOrElse("quiet", default.quiet).asInstanceOf[Boolean],
      uri = optionMap.get("uri") match {
        case None => default.uri
        case Some(value) => Some(value.asInstanceOf[String])
      },
      out = optionMap.get("out") match {
        case None => default.out
        case Some(value) => Some(value.asInstanceOf[String])
      },
      slaveOK = optionMap.getOrElse("slaveOK", default.slaveOK).asInstanceOf[Boolean],
      fields = optionMap.get("fields") match {
        case None => default.fields
        case Some(value) => Some(value.asInstanceOf[List[String]])
      },
      query = optionMap.get("query") match {
        case None => default.query
        case Some(value) => JSON.parse(value.asInstanceOf[String]).asInstanceOf[DBObject]
      },
      sort = optionMap.get("sort") match {
        case None => default.sort
        case Some(value) => Some(JSON.parse(value.asInstanceOf[String]).asInstanceOf[DBObject])
      },
      skip = optionMap.get("skip") match {
        case None => default.skip
        case Some(value) => Some(value.asInstanceOf[Int])
      },
      limit = optionMap.get("limit") match {
        case None => default.limit
        case Some(value) => Some(value.asInstanceOf[Int])
      },
      jsonArray = optionMap.getOrElse("jsonArray", default.jsonArray).asInstanceOf[Boolean]
    )
    if (options.out == None && !options.quiet) options.copy(quiet=true)
    else options
  }

  case class Options(quiet: Boolean = false, uri: Option[String] = None, out: Option[String] = None,
                     slaveOK: Boolean = true, fields: Option[List[String]] = None, query: DBObject = MongoDBObject(),
                     sort: Option[DBObject] = None, skip: Option[Int] = None, limit: Option[Int] = None,
                     jsonArray: Boolean = false)

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
