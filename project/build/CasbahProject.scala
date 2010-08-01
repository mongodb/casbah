import sbt._

class CasbahProject(info: ProjectInfo) extends DefaultProject(info) with rsync.RsyncPublishing with  posterous.Publish {
  override def compileOptions = super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)

  val scalaTime = "org.scala-tools" % "time" % "2.8.0-0.2-SNAPSHOT"

  val configgy = "net.lag" % "configgy_2.8.0" % "1.5.2"
  val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"

  val scalajCollection = "org.scalaj" % "scalaj-collection_2.8.0" % "1.0"

  val mongodb = "org.mongodb" % "mongo-java-driver" % "2.0"

  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
  val bumRepo = "Bum Networks Release Repository" at "http://repo.bumnetworks.com/releases/"
  val bumSnapsRepo = "Bum Networks Snapshots Repository" at "http://repo.bumnetworks.com/snapshots/"

  def rsyncRepo = "repobum:/home/public/repo-rels"
}
