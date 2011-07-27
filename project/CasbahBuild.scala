import sbt._
import Keys._

object HammersmithBuild extends Build {
  import Dependencies._
  import Resolvers._
  import Publish._

  lazy val buildSettings = Seq(
    organization := "com.mongodb.casbah",
    version      := "2.2.0-SNAPSHOT",
    crossScalaVersions := Seq("2.9.0-1", "2.8.1")
  )

  /**
   * Import some sample data for testing
   */
  "mongoimport -d casbahIntegration -c yield_historical.in --drop ./casbah-core/src/test/resources/yield_historical_in.json" !

  "mongoimport -d casbahIntegration -c books --drop ./casbah-core/src/test/resources/bookstore.json" ! 


  override lazy val settings = super.settings ++ buildSettings

  lazy val baseSettings = Defaults.defaultSettings ++ Publish.settings ++ Seq(
    posterous.Publish.posterousEmail := "ask n8han",
    posterous.Publish.posterousPassword := "where to properly set this"
  )

  lazy val parentSettings = baseSettings 
  lazy val defaultSettings = baseSettings ++ Seq(
    libraryDependencies ++= Seq(scalatest(scalaVersion), specs2, slf4j, slf4jJCL),
    resolvers ++= Seq(scalaToolsReleases, scalaToolsSnapshots, mavenOrgRepo),
    autoCompilerPlugins := true,
    parallelExecution in Test := true,
    testFrameworks += TestFrameworks.Specs2
  )

  lazy val casbah = Project(
    id        = "casbah",
    base      = file("."),
    settings  = parentSettings,
    aggregate = Seq(util, commons, core, query, gridfs)
  )

  lazy val util = Project(
    id       = "casbah-util",
    base     = file("casbah-util"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(mongoJavaDriver, scalajCollection, slf4j, slf4jJCL, scalaTime)
    )
  )

  lazy val commons = Project(
    id       = "casbah-commons",
    base     = file("casbah-commons"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(mongoJavaDriver, scalajCollection, slf4j, slf4jJCL, scalaTime, scalatestCompile(scalaVersion), specs2Compile)
    )
  ) dependsOn(util)

  lazy val core = Project(
    id       = "casbah-core",
    base     = file("casbah-core"),
    settings = defaultSettings 
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
  // TODO - Fix maven artifact

  //val bson = "org.mongodb" % "bson" % "2.5.3"

  val mongoJavaDriver  = "org.mongodb" % "mongo-java-driver" % "2.6.3"
  val scalajCollection = "org.scalaj" %% "scalaj-collection" % "1.1"
  val slf4j            = "org.slf4j" % "slf4j-api" % "1.6.0"

  val specs2 = "org.specs2" %% "specs2" % "1.5" % "test"
  val specs2Compile = "org.specs2" %% "specs2" % "1.5" 

  def scalaVersionString(scalaVer: sbt.SettingKey[String]): String = {
    var result = ""
    scalaVer { sv => result = sv }
    if (result == "") result = "2.8.1"
    result
  }

  def scalatest(scalaVer: sbt.SettingKey[String]) =
    scalaVersionString(scalaVer) match {
      case "2.8.1" => "org.scalatest" % "scalatest_2.8.1" % "1.5.1" % "test"
      case _ => "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test"
    }

  def scalatestCompile(scalaVer: sbt.SettingKey[String]) =
    scalaVersionString(scalaVer) match {
      case "2.8.1" => "org.scalatest" % "scalatest_2.8.1" % "1.5.1"
      case _ => "org.scalatest" % "scalatest_2.9.0" % "1.6.1"
    }

  // JCL bindings for testing only
  val slf4jJCL         = "org.slf4j" % "slf4j-jcl" % "1.6.0" % "test"
  val scalaTime        = "org.scala-tools.time" % "time_2.8.0" % "0.2"

}

object Publish {
  lazy val settings = Seq(
    publishTo <<= version(v => Some(publishTarget(v))),
    credentials += Credentials(Path.userHome / ".ivy2" / ".scalatools_credentials")
  )

  private def publishTarget(version: String) = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/%s/".format(
    if (version.endsWith("-SNAPSHOT"))
      "snapshots"
    else
      "releases"
  )
     /*override def documentOptions = Seq(
      CompoundDocOption("-doc-source-url", "http://api.mongodb.org/scala/casbah-%s/%s/sxr/€{FILE_PATH}".format(projectVersion.value, projectName.value)),
      CompoundDocOption("-doc-version", "v%s".format(projectVersion.value)),
      CompoundDocOption("-doc-title", "Casbah %s".format(projectName.value))
    )*/
}

object Resolvers {
  val scalaToolsSnapshots = "snapshots" at "http://scala-tools.org/repo-snapshots"
  val scalaToolsReleases  = "releases" at "http://scala-tools.org/repo-releases"

  val jbossRepo = "JBoss Public Repo" at "https://repository.jboss.org/nexus/content/groups/public-jboss/"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
  val twttrRepo = "Twitter Public Repo" at "http://maven.twttr.com"
}

