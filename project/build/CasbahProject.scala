import sbt._

class CasbahProject(info: ProjectInfo) extends ParentProject(info) with posterous.Publish {

  override def parallelExecution = true

  lazy val util = project("casbah-util", "Casbah Utils", new CasbahUtilProject(_))
  lazy val core = project("casbah-core", "Casbah Core", new CasbahCoreProject(_), query)
  lazy val query = project("casbah-query", "Casbah Query Engine", new CasbahQueryProject(_), util)
  lazy val gridfs = project("casbah-gridfs", "Casbah GridFS Tools", new CasbahGridFSProject(_), core)

  class CasbahUtilProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = 
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
    // Runtime deps
    val mongodb = "org.mongodb" % "mongo-java-driver" % "2.1"
    val configgy = "net.lag" % "configgy" % "2.0.0" intransitive()
    val scalajCollection = "org.scalaj" % "scalaj-collection_2.8.0" % "1.0"
  }

  class CasbahCoreProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = 
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
    // Runtime Deps
    val scalaTime = "org.scala-tools" % "time" % "2.8.0-0.2-SNAPSHOT"
  }

  class CasbahQueryProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = 
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)

  }

  class CasbahGridFSProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = 
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
  }

  // Testing Deps
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test->default"
  val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"

  // Repositories
  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
  val bumRepo = "Bum Networks Release Repository" at "http://repo.bumnetworks.com/releases/"
  val bumSnapsRepo = "Bum Networks Snapshots Repository" at "http://repo.bumnetworks.com/snapshots/"

  // Build / Dist Config
  def rsyncRepo = "repobum:/home/public/repo-rels"

}
