import sbt.Keys._

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.2")

Option(System.getProperty("scoverage")).getOrElse("false") match {
  case "true" => addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")
  case _ => libraryDependencies ++= Seq()
}
