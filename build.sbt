import sbt.Keys._
import sbt._

import Dependencies._

lazy val casbahCommonDependencies = Seq(libraryDependencies ++= {
  val sv = scalaVersion.value
  Seq(scalatest(sv), scalatime(sv)) ++ scalaStyle(sv) ++ specs2(sv) ++ specs2Mock(sv)
})

// Check style
val checkAlias = addCommandAlias("check", ";clean;coverage;test;coverageAggregate;coverageReport")

val casbahDefaultSettings = Seq(
  scalaVersion := "2.13.2"
)

lazy val commons = Project(
  id = "casbah-commons",
  base = file("casbah-commons"))
  .settings(casbahDefaultSettings ++ casbahCommonDependencies)
  .settings(libraryDependencies ++= Seq(mongoJavaDriver, slf4j, slf4jJCL))
  .settings(publishArtifact in(Test, packageBin) := true)

lazy val core = Project(
  id = "casbah-core",
  base = file("casbah-core"))
  .settings(casbahDefaultSettings)
  .dependsOn(commons % "test->test;compile")
  .dependsOn(query)
  .settings(parallelExecution in Test := false)

lazy val query = Project(
  id = "casbah-query",
  base = file("casbah-query"))
  .settings(casbahDefaultSettings)
  .dependsOn(commons % "test->test;compile")
  .settings(casbahDefaultSettings: _*)

lazy val gridfs = Project(
  id = "casbah-gridfs",
  base = file("casbah-gridfs"))
  .settings(casbahDefaultSettings)
  .dependsOn(commons % "test->test", core % "test->test;compile")

lazy val examples = Project(
  id = "casbah-examples",
  base = file("examples")
).aggregate(casbah)
  .settings(casbahDefaultSettings: _*)
  .dependsOn(casbah)

lazy val casbah = Project(
  id = "casbah",
  base = file("."))
  .settings(casbahDefaultSettings)
  .aggregate(commons, core, query, gridfs)
  .dependsOn(commons, core, query, gridfs)
  .settings(checkAlias: _*)
  .settings(initialCommands in console := """import com.mongodb.casbah.Imports._""")
