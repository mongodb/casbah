import sbt._
import Keys._
import Project.Initialize

import sbtassembly.Plugin._
import org.scalastyle.sbt.ScalastylePlugin
import AssemblyKeys._


object CasbahBuild extends Build {
  import Dependencies._
  import Resolvers._

  lazy val buildSettings = Seq(
    organization := "org.mongodb",
    organizationHomepage := Some(url("http://www.mongodb.org")),
    version      := "3.0.0-SNAPSHOT",
    scalaVersion := "2.10.5",
    crossScalaVersions := Seq("2.11.7", "2.10.5", "2.12.0-M2"),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
  )

  val allSourceDirectories = SettingKey[Seq[Seq[File]]]("all-source-directories")

  def sxrOptions(baseDir: File, sourceDirs: Seq[Seq[File]], sv: String): Seq[String] = {
    sv match {
      case sv if sv.startsWith("2.10") =>
        val sxrBaseDir = "-P:sxr:base-directory:" + sourceDirs.flatten.mkString(";").replaceAll("\\\\","/")
        Seq(sxrBaseDir)
      case _ => Seq()
    }
  }

  override lazy val settings = super.settings ++ buildSettings

  val scalac210Options = Seq("-feature",
    "-language:reflectiveCalls",
    "-language:implicitConversions",
    "-language:postfixOps") // ++ Seq("-unchecked", "-deprecation")

  lazy val baseSettings = Defaults.defaultSettings ++ Publish.settings ++ Seq(
    resolvers ++= Seq(mavenLocalRepo, sonatypeRels, sonatypeSnaps, sonatypeSTArch, typeSafeRels, mavenOrgRepo),
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml"),
    crossPaths := true,
    autoCompilerPlugins := true,
    libraryDependencies <<= (scalaBinaryVersion, libraryDependencies) { (sv, deps) =>
      sv match {
        case "2.10" => deps :+ compilerPlugin("org.scala-sbt.sxr" %% "sxr" % "0.3.0")
        case _ => deps
      }
    },
    scalacOptions <++= scalaBinaryVersion map {
      case _ => scalac210Options
    },
    allSourceDirectories <<= projects.map(sourceDirectories in Compile in _).join,
    scalacOptions in (Compile, doc) <++=  (baseDirectory, allSourceDirectories, scalaVersion, version, baseDirectory in LocalProject("casbah")).map {
      (bd, asd, sv, v, rootBase) =>
        val docSourceUrl = "http://{{WEBSITE_ROOT}}api.sxr/€{FILE_PATH}.scala.html"
        val docSourceOpts = Seq("-sourcepath", rootBase.getAbsolutePath, "-doc-source-url", docSourceUrl)
        val sxrOpts = sxrOptions(bd, asd, sv)
        docSourceOpts ++ sxrOpts
    }
  )

  lazy val parentSettings = baseSettings ++ Seq(
    publishArtifact in (Compile, packageBin) := false,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false
  )

  lazy val defaultSettings = baseSettings ++ ScalastylePlugin.Settings ++ styleCheckSetting ++ Seq(
    libraryDependencies <++= scalaVersion (sv => Seq(
      scalatest(sv), scalatime(sv),
      slf4j, slf4jJCL, junit
    ) ++ scalaStyle(sv) ++  specs2(sv)),
    parallelExecution in Test := true,
    testFrameworks += TestFrameworks.Specs2,
    org.scalastyle.sbt.PluginKeys.config := file("project/scalastyle-config.xml")
  )

  lazy val casbah = Project(
    id        = "casbah",
    base      = file("."),
    settings  = parentSettings ++ Unidoc.settings ++ assemblySettings ++
                addArtifact(Artifact("casbah-alldep", "pom", "jar"), assembly)
                ++ Seq(initialCommands := "import com.mongodb.casbah.Imports._"),
    aggregate = Seq(commons, core, query, gridfs)
  ) dependsOn(commons, core, query, gridfs)

  lazy val commons = Project(
    id       = "casbah-commons",
    base     = file("casbah-commons"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(mongoJavaDriver, slf4j, slf4jJCL),
      unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile, scalaBinaryVersion){ (s, v) => s / ("scala_"+v) },
      publishArtifact in (Test, packageBin) := true
    )
  )

  lazy val core = Project(
    id       = "casbah-core",
    base     = file("casbah-core"),
    settings = defaultSettings ++ Seq(parallelExecution in Test := false)
  ) dependsOn(commons % "test->test;compile", query)

  lazy val query = Project(
    id       = "casbah-query",
    base     = file("casbah-query"),
    settings = defaultSettings
  ) dependsOn(commons % "test->test;compile")

  lazy val gridfs = Project(
    id       = "casbah-gridfs",
    base     = file("casbah-gridfs"),
    settings = defaultSettings
  ) dependsOn(commons % "test->test", core % "test->test;compile")

  /*
   * Coursera styleCheck command
   */

  val styleCheck = TaskKey[Unit]("styleCheck")

  /**
   * depend on compile to make sure the sources pass the compiler
   */
  val styleCheckSetting = styleCheck <<= (compile in Compile, sources in Compile, streams) map { (_, sourceFiles, s) =>
    val logger = s.log
    val (feedback, score) = StyleChecker.assess(sourceFiles)
    logger.info(feedback)
    logger.info("Style Score: "+ score +" out of "+ StyleChecker.maxResult)
  }


}

object Dependencies {
  val mongoJavaDriver  = "org.mongodb" % "mongo-java-driver" % "3.0.4"
  val slf4j            = "org.slf4j" % "slf4j-api" % "1.6.0"
  val junit            = "junit" % "junit" % "4.10" % "test"
  val slf4jJCL         = "org.slf4j" % "slf4j-jcl" % "1.6.0" % "test"

  def scalatest(scalaVersion: String) =
    (scalaVersion match {
      case "2.12.0-M2"   => "org.scalatest" %% "scalatest"  % "2.2.5-M2"
      case _ => "org.scalatest" %% "scalatest" % "2.1.3"
    }) % "test"

  def scalatime(scalaVersion: String) =
    scalaVersion match {
      case "2.12.0-M2"   => "com.github.nscala-time" %% "nscala-time" % "2.2.0"
      case _ => "com.github.nscala-time" %% "nscala-time" % "1.0.0"
    }

  def specs2(scalaVersion: String) =
    (scalaVersion match {
      case "2.12.0-M2"   => Seq("org.specs2" %% "specs2-core" % "3.6.4" % "test",
                               "org.specs2" %% "specs2-junit" % "3.6.4" % "test")
      case _ => Seq("org.specs2" %% "specs2-core" % "3.6.4" % "test",
                    "org.specs2" %% "specs2-junit" % "3.6.4" % "test")
    })

  def scalaStyle(scalaVersion: String) =
    scalaVersion match {
      case "2.10.5" =>  Seq("org.scalastyle" %% "scalastyle" % "0.4.0"  % "test")
      case _   => Seq()
    }
}

object Resolvers {
  val sonatypeSnaps = "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  val sonatypeRels = "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
  val sonatypeSTArch = "scalaTools Archive" at "https://oss.sonatype.org/content/groups/scala-tools"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org"
  val mavenLocalRepo = "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
  val typeSafeRels =  Resolver.url("Typesafe Releases", url("http://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
}
