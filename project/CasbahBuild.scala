import sbt._
import Keys._
import Project.Initialize

import com.typesafe.sbt.SbtSite._
import org.scalastyle.sbt.ScalastylePlugin
import sbtassembly.Plugin._
import AssemblyKeys._


object CasbahBuild extends Build {
  import Dependencies._
  import Resolvers._

  lazy val buildSettings = Seq(
    organization := "org.mongodb",
    organizationHomepage := Some(url("http://www.mongodb.org")),
    version      := "2.6.3-SNAPSHOT",
    scalaVersion := "2.10.2",
    crossScalaVersions := Seq("2.10.2", "2.10.1", "2.10.0", "2.9.3", "2.9.2", "2.9.1")
  )

  val allSourceDirectories = SettingKey[Seq[Seq[File]]]("all-source-directories")

  def sxrOptions(baseDir: File, sourceDirs: Seq[Seq[File]], scalaVersion: String): Seq[String] = {
    if (scalaVersion.startsWith("2.10"))
      Seq()
    else {
      val sxrBaseDir = "-P:sxr:base-directory:" + sourceDirs.flatten.mkString(";").replaceAll("\\\\","/")
      Seq(sxrBaseDir)
    }
  }

  override lazy val settings = super.settings ++ buildSettings

  val scalac210Options = Seq("-feature",
                               "-language:reflectiveCalls",
                               "-language:implicitConversions",
                               "-language:postfixOps")

  lazy val baseSettings = Defaults.defaultSettings ++ Publish.settings ++ Seq(
      resolvers ++= Seq(sonatypeRels, sonatypeSnaps, sonatypeSTArch, mavenOrgRepo),
      testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml"),
      crossPaths := true,
      autoCompilerPlugins := true,
      libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
        sv match {
          case sv if sv.startsWith("2.10") => deps
          case _ => deps :+ compilerPlugin("org.scala-tools.sxr" % "sxr_2.9.0" % "0.2.7")
        }
      },
      scalacOptions <++= scalaVersion map { sv =>
        sv match {
          case sv if sv.startsWith("2.10") => scalac210Options
          case _ => Seq()
        }
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

  lazy val defaultSettings = baseSettings ++ Seq(
    libraryDependencies <++= (scalaVersion)(sv => Seq(
      scalatest(sv), scalatime(sv), specs2(sv),
      slf4j, slf4jJCL, junit
    )),
    parallelExecution in Test := true,
    testFrameworks += TestFrameworks.Specs2
  )

  lazy val casbah = Project(
    id        = "casbah",
    base      = file("."),
    settings  = parentSettings ++ Unidoc.settings ++ site.settings ++
                site.sphinxSupport() ++ ScalastylePlugin.settings ++
                assemblySettings ++
                addArtifact(Artifact("casbah-alldep", "pom", "jar"), assembly),
    aggregate = Seq(commons, core, query, gridfs)
  ) dependsOn(commons, core, query, gridfs)

  lazy val commons = Project(
    id       = "casbah-commons",
    base     = file("casbah-commons"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(mongoJavaDriver, slf4j, slf4jJCL),
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
  ) dependsOn(commons % "test->test;compile" )

  lazy val gridfs = Project(
    id       = "casbah-gridfs",
    base     = file("casbah-gridfs"),
    settings = defaultSettings
  ) dependsOn(commons % "test->test", core)

}

object Dependencies {

  val mongoJavaDriver  = "org.mongodb" % "mongo-java-driver" % "2.11.2"
  val slf4j            = "org.slf4j" % "slf4j-api" % "1.6.0"
  val junit            = "junit" % "junit" % "4.10" % "test"
  val slf4jJCL         = "org.slf4j" % "slf4j-jcl" % "1.6.0" % "test"

  def scalatest(scalaVersion: String) =
    (scalaVersion match {
      case _ => "org.scalatest" %% "scalatest" % "1.9.1"
    }) % "test"

  def scalatime(scalaVersion: String) =
      scalaVersion match {
        case "2.9.3" => "com.github.nscala-time" % "nscala-time_2.9.2" % "0.2.0"
        case _ => "com.github.nscala-time" %% "nscala-time" % "0.2.0"
      }

  def specs2(scalaVersion: String) =
      (scalaVersion match {
          case "2.9.1"   => "org.specs2" %% "specs2" % "1.12.2"
          case "2.9.2"   => "org.specs2" %% "specs2" % "1.12.3"
          case "2.9.3"   => "org.specs2" % "specs2_2.9.2" % "1.12.3"
          case scalaVersion if scalaVersion.startsWith("2.10") => "org.specs2" %% "specs2" % "1.14"
      }) % "test"
}

object Resolvers {
  val sonatypeSnaps = "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  val sonatypeRels = "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
  val sonatypeSTArch = "scalaTools Archive" at "https://oss.sonatype.org/content/groups/scala-tools/"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
}
