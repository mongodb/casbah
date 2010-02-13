import sbt._

class MongoScalaWrappersProject(info: ProjectInfo) extends DefaultProject(info) {
  override def compileOptions = super.compileOptions ++ Seq(Unchecked)

  val scalatest = "org.scalatest" % "scalatest" % "1.0"
  val mongodb = "org.mongodb" %  "mongo-java-driver" % "1.2"
  val javautils = "org.scala-tools" % "javautils" % "2.7.4-0.1"
  //val configgy = "net.lag" % "configgy" % "1.5" from "file:///lib/configgy-1.5.jar"

  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
}