import sbt.Keys._

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.2")

Option(System.getProperty("scoverage")).getOrElse("false") match {
  case "true" => addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")
  case _ => libraryDependencies ++= Seq()
}
