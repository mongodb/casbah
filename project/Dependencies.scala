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

import sbt._

object Dependencies {
  val mongoJavaDriver  = "org.mongodb" % "mongo-java-driver" % "3.2.2"
  val slf4j            = "org.slf4j" % "slf4j-api" % "1.6.0"
  val junit            = "junit" % "junit" % "4.10" % "test"
  val slf4jJCL         = "org.slf4j" % "slf4j-jcl" % "1.6.0" % "test"

  def scalatime(scalaVersion: String) =
    scalaVersion match {
      case "2.12.0-M2"   => "com.github.nscala-time" %% "nscala-time" % "2.2.0"
      case _ => "com.github.nscala-time" %% "nscala-time" % "1.0.0"
    }

  def scalatest(scalaVersion: String) =
    scalaVersion match {
      case "2.12.0-M2"   => "org.scalatest" %% "scalatest"  % "2.2.5-M2"  % "test"
      case _ => "org.scalatest" %% "scalatest" % "2.1.3"  % "test"
    }

  def specs2(scalaVersion: String) =
    scalaVersion match {
      case "2.12.0-M2"   => Seq("org.specs2" %% "specs2-core" % "3.6.4" % "test",
        "org.specs2" %% "specs2-junit" % "3.6.4" % "test")
      case _ => Seq("org.specs2" %% "specs2-core" % "3.6.4" % "test",
        "org.specs2" %% "specs2-junit" % "3.6.4" % "test")
    }

  def scalaStyle(scalaVersion: String) =
    scalaVersion match {
      case "2.10.5" =>  Seq("org.scalastyle" %% "scalastyle" % "0.4.0"  % "test")
      case _   => Seq()
    }
}
