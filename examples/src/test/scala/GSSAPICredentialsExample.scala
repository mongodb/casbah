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

import com.mongodb.casbah.Imports._


/**
 * An example program showing you how to connect using GSSAPI authentication
 *
 * {{{
 * GSSAPICredentialsExample.main("mongodb://drivers%40EXAMPLE.ME@kdc.example.me/<database>.<collection>/?authMechanism=GSSAPI")
 * }}}
 *
 */
object GSSAPICredentialsExample {
  val usage = """
                | An example program showing you how to connect using GSSAPI authentication
                |
                | Steps:
                |   1. Add casbah-alldep jar to your path or add to ./lib directory and then run as a shell program
                |   2. Install unlimited strength encryption jar files in jre/lib/security
                |       (http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html)
                |
                | Example:
                |  ./GSSAPICredentialsExample.scala -u mongodb://dev1%EXAMPLE.ME@kdc.example.me/database.collection/?authMechanism=GSSAPI
                |
                | Options:
                |  --help                                produce help message
                |  --quiet                               silence all non error diagnostic
                |                                        messages
                |  -u [ --uri ] arg                      The connection URI - must contain a collection
                |                                        mongodb://[username:password@]host1[:port1][,host2[:port2]]/database.collection[?options]
                |                                        See: http://docs.mongodb.org/manual/reference/connection-string/
              """.stripMargin

  /**
   * The main export program
   * @param args the commandline arguments
   */
  def main(args: Array[String]) {

    /*The time when the execution of this program started, in milliseconds since 1 January 1970 UTC. */
    val executionStart: Long = currentTime

    if (args.length == 0 | args.contains("--help")) {
      Console.println(usage)
      sys.exit(1)
    }

    val optionMap = parseArgs(Map(), args.toList)
    val options = getOptions(optionMap)

    if (options.uri == None) {
      Console.println(s"Missing URI")
      Console.println(usage)
      sys.exit(1)
    }

    handleSLF4J()

    val mongoClientURI = MongoClientURI(options.uri.get)
    val credentials = mongoClientURI.credentials

    credentials match {
      case None =>
        Console.println(s"Missing Credentials in the URI")
        Console.println(usage)
        sys.exit(1)
      case Some(credential) =>
        if (credential.getMechanism != "GSSAPI") {
          Console.println(s"Wrong Credential mechanism in the URI it should be GSSAPI")
          Console.println(usage)
          sys.exit(1)
        }
    }

    val realm = credentials.get.getUserName.split("@")(1)
    val kdc = mongoClientURI.hosts(0)

    // Set any system properties not already set.
    Option(System.getProperty("java.security.krb5.realm")) match {
      case None => System.setProperty("java.security.krb5.realm", realm)
      case _ =>
    }

    Option(System.getProperty("java.security.krb5.kdc")) match {
      case None => System.setProperty("java.security.krb5.kdc", kdc)
      case _ =>
    }

    Option(System.getProperty("javax.security.auth.useSubjectCredsOnly")) match {
      case None => System.setProperty("javax.security.auth.useSubjectCredsOnly", "false")
      case _ =>
    }

    Option(System.getProperty("java.security.krb5.conf")) match {
      case None => System.setProperty("java.security.krb5.conf", "/dev/null")
      case _ =>
    }

    if (!options.quiet) Console.println("Connecting....")
    val mongoClient =  MongoClient(mongoClientURI)
    if (!options.quiet) Console.println(s"Connected: $mongoClientURI")

    if (mongoClientURI.collection != None) {
      val collection = mongoClient(mongoClientURI.database.get)(mongoClientURI.collection.get)
      Console.println(s"$collection count: ${collection.count()}")
    } else if (mongoClientURI.collection != None) {
      Console.println(mongoClient(mongoClientURI.database.get).collectionNames())
    } else {
      Console.println(mongoClient.databaseNames())
    }

    val total = currentTime - executionStart
    if (!options.quiet) Console.println(s"Finished: $total ms")
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
      case option :: tail =>
        Console.println("Unknown option " + option)
        Console.println(usage)
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
      }
    )
  }

  case class Options(quiet: Boolean = false, uri: Option[String] = None)

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

}
