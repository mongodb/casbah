import sbt._

class CasbahProject(info: ProjectInfo) extends DefaultProject(info) {
  override def compileOptions = super.compileOptions ++ Seq(Unchecked, Deprecation)

  val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.RC3-SNAPSHOT"
  val scalajCollection = "org.scalaj" % "scalaj-collection_2.8.0.Beta1" % "1.0.Beta2"

  val mongodb = "org.mongodb" % "mongo-java-driver" % "1.4"

  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
}
