/*
  * Copyright 2015 MongoDB, Inc.
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
  */

import com.typesafe.sbt.SbtScalariform.{ScalariformKeys, _}
import org.scalastyle.sbt.ScalastylePlugin._
import sbt.Keys._
import sbt._
import sbtunidoc.Plugin._

object CasbahBuild extends Build {

  import Dependencies._
  import Resolvers._

  val buildSettings = Seq(
    organization := "org.mongodb",
    organizationHomepage := Some(url("http://www.mongodb.org")),
    version := "3.1.2-SNAPSHOT",
    scalaVersion := "2.11.8",
    crossScalaVersions := Seq("2.11.8", "2.10.6", "2.12.1"),
    resolvers := casbahResolvers,
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
  )

  /*
   * ScalacOptions settings
   */
  lazy val scalacOptionsSettings = Seq(scalacOptions ++= (scalaBinaryVersion.value match {
    case "2.10" => Seq("-unchecked", "-feature", "-Xlint")
    case _ => Seq("-unchecked", "-feature", "-Xlint:-missing-interpolator")
  }))

  /*
   * Publish settings
   */
  val publishSettings = Publish.settings
  val publishAssemblySettings = Publish.publishAssemblySettings
  val noPublishingSettings = Publish.noPublishing
  val rootPublishSettings = Publish.rootPublishSettings

  /*
   * Test Settings
   */
  val testSettings = Seq(
    testFrameworks += TestFrameworks.Specs2,
    parallelExecution in Test := true
  )

  /*
   * Style and formatting
   */
  def scalariFormFormattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
  }

  val customScalariformSettings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := scalariFormFormattingPreferences,
    ScalariformKeys.preferences in Test := scalariFormFormattingPreferences
  )

  val scalaStyleSettings = Seq(
    (scalastyleConfig in Compile) := file("project/scalastyle-config.xml"),
    (scalastyleConfig in Test) := file("project/scalastyle-config.xml")
  )

  lazy val casbahCommonDependencies = Seq(libraryDependencies <++= scalaVersion (sv =>
    Seq(scalatest(sv), scalatime(sv)) ++ scalaStyle(sv) ++ specs2(sv)))

  // Check style
  val checkAlias = addCommandAlias("check", ";clean;scalastyle;coverage;test;coverageAggregate;coverageReport")

  val rootUnidocSettings = Seq(
    scalacOptions in(Compile, doc) ++= Opts.doc.title("Casbah Driver"),
    scalacOptions in(Compile, doc) ++= Seq("-diagrams", "-unchecked", "-doc-root-content", "rootdoc.txt")
  ) ++ unidocSettings

  val casbahDefaultSettings = buildSettings ++
    scalacOptionsSettings ++
    publishSettings ++
    testSettings ++
    customScalariformSettings ++
    scalaStyleSettings

  lazy val commons = Project(
    id = "casbah-commons",
    base = file("casbah-commons"),
    settings = casbahDefaultSettings ++ casbahCommonDependencies
  ).settings(libraryDependencies ++= Seq(mongoJavaDriver, slf4j, slf4jJCL))
    .settings(publishArtifact in(Test, packageBin) := true)

  lazy val core = Project(
    id = "casbah-core",
    base = file("casbah-core"),
    settings = casbahDefaultSettings
  ).dependsOn(commons % "test->test;compile")
    .dependsOn(query)
    .settings(parallelExecution in Test := false)

  lazy val query = Project(
    id = "casbah-query",
    base = file("casbah-query"),
    settings = casbahDefaultSettings
  ).dependsOn(commons % "test->test;compile")
    .settings(casbahDefaultSettings: _*)

  lazy val gridfs = Project(
    id = "casbah-gridfs",
    base = file("casbah-gridfs"),
    settings = casbahDefaultSettings
  ) dependsOn(commons % "test->test", core % "test->test;compile")

  lazy val examples = Project(
    id = "casbah-examples",
    base = file("examples")
  ).aggregate(casbah)
    .settings(casbahDefaultSettings: _*)
    .settings(noPublishingSettings: _*)
    .dependsOn(casbah)

  lazy val casbah = Project(
    id = "casbah",
    base = file("."),
    settings = casbahDefaultSettings
  ).aggregate(commons, core, query, gridfs)
    .dependsOn(commons, core, query, gridfs)
    .settings(rootUnidocSettings: _*)
    .settings(rootPublishSettings: _*)
    .settings(checkAlias: _*)
    .settings(initialCommands in console := """import com.mongodb.casbah.Imports._""")

  override def rootProject = Some(casbah)

}
