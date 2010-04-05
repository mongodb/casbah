import sbt._

class MongoScalaWrappersProject(info: ProjectInfo) extends DefaultProject(info) {
  override def compileOptions = super.compileOptions ++ Seq(Unchecked, Deprecation)

  val scalatest = "org.scalatest" % "scalatest" % "1.0.1-for-scala-2.8.0.Beta1-with-test-interfaces-0.3-SNAPSHOT"
  val mongodb = "org.mongodb" %  "mongo-java-driver" % "1.2"
//  val javautils = "org.scalaj" % "scalaj-collection_2.8.0.Beta1" % "1.0.RC1-SNAPSHOT"
  //val configgy = "net.lag" % "configgy" % "1.5" from "file:///lib/configgy-1.5.jar"
  val scalajCollection = "org.scalaj" % "scalaj-collection_2.8.0.Beta1" % "1.0.Beta1"

  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
}
