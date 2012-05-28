import sbt._
import Keys._

object CasbahBuild extends Build {
  import Dependencies._
  import Resolvers._

  lazy val buildSettings = Seq(
    organization := "org.mongodb",
    version      := "2.2.0-SNAPSHOT",
    crossScalaVersions := Seq("2.9.2", "2.9.1", "2.9.0-1", "2.9.0", "2.8.1")
  )

  /**
   * Import some sample data for testing
   */
  "mongoimport -d casbahIntegration -c yield_historical.in --drop ./casbah-core/src/test/resources/yield_historical_in.json" !

  "mongoimport -d casbahIntegration -c books --drop ./casbah-core/src/test/resources/bookstore.json" ! 


  override lazy val settings = super.settings ++ buildSettings

  lazy val baseSettings = Defaults.defaultSettings  ++ Seq(
      resolvers ++= Seq(sonatypeRels, sonatypeSnaps, sonatypeSTArch, mavenOrgRepo),
      testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")
    )


  lazy val parentSettings = baseSettings ++ Seq(
    publishArtifact := false
  )

  lazy val defaultSettings = baseSettings ++ Seq(
    libraryDependencies ++= Seq(scalatest(scalaVersion),  slf4j, slf4jJCL, junit),
    libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
      sv match {
        case "2.9.2" => 
          deps :+ ("org.scalaj" % "scalaj-collection_2.9.1" % "1.2") 
          deps :+ ("org.scala-tools.time" % "time_2.9.1" % "0.5")
        case x => {
          deps :+ ("org.scalaj" %%  "scalaj-collection" % "1.2") 
          deps :+ ("org.scala-tools.time" %% "time" % "0.5")
        }
      }

    },
    libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
      val versionMap = Map("2.8.1" -> ("specs2_2.8.1", "1.5"),
                           "2.9.0" -> ("specs2_2.9.0", "1.7.1"),
                           "2.9.0-1" -> ("specs2_2.9.0", "1.7.1"),
                           "2.9.1" -> ("specs2_2.9.1", "1.7.1"),
                           "2.9.2" -> ("specs2_2.9.2", "1.10"))
      val tuple = versionMap.getOrElse(sv, sys.error("Unsupported Scala version for Specs2"))
      deps :+ ("org.specs2" % tuple._1 % tuple._2)
    },
    autoCompilerPlugins := true,
    parallelExecution in Test := true,
    testFrameworks += TestFrameworks.Specs2
  )


  lazy val casbah = Project(
    id        = "casbah",
    base      = file("."),
    settings  = parentSettings,
    aggregate = Seq(util, commons, core, query, gridfs)
  ) dependsOn(util, commons, core, query, gridfs)

  lazy val util = Project(
    id       = "casbah-util",
    base     = file("casbah-util"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(mongoJavaDriver, slf4j, slf4jJCL)
    )
  )

  lazy val commons = Project(
    id       = "casbah-commons",
    base     = file("casbah-commons"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(mongoJavaDriver, slf4j, slf4jJCL)
    )
  ) dependsOn(util)

  lazy val core = Project(
    id       = "casbah-core",
    base     = file("casbah-core"),
    settings = defaultSettings ++ Seq(parallelExecution in Test := false) 
  ) dependsOn(commons, query)

  lazy val query = Project(
    id       = "casbah-query",
    base     = file("casbah-query"),
    settings = defaultSettings 
  ) dependsOn(commons)

  lazy val gridfs = Project(
    id       = "casbah-gridfs",
    base     = file("casbah-gridfs"),
    settings = defaultSettings 
  ) dependsOn(core)
  
}

object Dependencies {

  val mongoJavaDriver  = "org.mongodb" % "mongo-java-driver" % "2.7.3"
  val slf4j            = "org.slf4j" % "slf4j-api" % "1.6.0"

  val specs2 = "org.specs2" %% "specs2" % "1.5.1" % "provided"
  //val specs2 = specs2Compile % "test"
  val junit = "junit" % "junit" % "4.10" % "test"

  def specs2ScalazCore(scalaVer: sbt.SettingKey[String]) =
    scalaVersionString(scalaVer) match {
      case "2.8.1" => "org.specs2" %% "specs2-scalaz-core" % "5.1-SNAPSHOT" % "test"
      case _ => "org.specs2" %% "specs2-scalaz-core" % "6.0.RC2" % "test"
    }

  def scalaVersionString(scalaVer: sbt.SettingKey[String]): String = {
    var result = ""
    scalaVersion { sv => result = sv }
    if (result == "") result = "2.8.1"
    result
  }

  /*def scalatest(scalaVer: sbt.SettingKey[String]) =
    scalaVersionString(scalaVer) match {
      case "2.8.1" => "org.scalatest" % "scalatest_2.8.1" % "1.5.1" % "test"
      case _ => "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test"
    }*/

  def scalatest(scalaVer: sbt.SettingKey[String]) =
    scalaVersionString(scalaVer) match {
      case "2.8.1" => "org.scalatest" % "scalatest_2.8.1" % "1.5.1" % "provided"
      case _ => "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "provided"
    }

  // JCL bindings for testing only
  val slf4jJCL         = "org.slf4j" % "slf4j-jcl" % "1.6.0" % "test"

}

object Resolvers {
  val sonatypeSnaps = "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  val sonatypeRels = "releases" at "https://oss.sonatype.org/content/repositories/releases"
  val sonatypeSTArch = "scalaTools Archive" at "https://oss.sonatype.org/content/groups/scala-tools/"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
}

