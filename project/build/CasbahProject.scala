import sbt._

class CasbahProject(info: ProjectInfo) extends ParentProject(info) with posterous.Publish {

  override def parallelExecution = true

  lazy val commons = project("casbah-commons", "casbah-commons", new CasbahCommonsProject(_))
  lazy val core = project("casbah-core", "casbah-core", new CasbahCoreProject(_), commons, query)
  lazy val query = project("casbah-query", "casbah-query", new CasbahQueryProject(_), commons)
  lazy val gridfs = project("casbah-gridfs", "casbah-gridfs", new CasbahGridFSProject(_), core)
  lazy val mapper = project("casbah-mapper", "casbah-mapper", new CasbahMapperProject(_), core)

  class CasbahCommonsProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = 
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
    // Runtime deps
    val mongodb = "org.mongodb" % "mongo-java-driver" % "2.1"
    val configgy = "net.lag" % "configgy" % "2.0.0" intransitive()
    val scalajCollection = "org.scalaj" % "scalaj-collection_2.8.0" % "1.0"
    // Testing Deps
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test->default"
    val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"
  }

  class CasbahCoreProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = 
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
    // Runtime Deps
    val scalaTime = "org.scala-tools" % "time" % "2.8.0-0.2-SNAPSHOT"
    // Testing Deps
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test->default"
    val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"
  }

  class CasbahQueryProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = 
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
    // Testing Deps
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test->default"
    val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"

  }

  class CasbahGridFSProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = 
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
    // Testing Deps
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test->default"
    val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"
  }

  class CasbahMapperProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions = 
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
    val objenesis = "org.objenesis" % "objenesis" % "1.2"
    // Testing Deps
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test->default"
    val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"
  }

  // Repositories
  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
  val bumRepo = "Bum Networks Release Repository" at "http://repo.bumnetworks.com/releases/"
  val bumSnapsRepo = "Bum Networks Snapshots Repository" at "http://repo.bumnetworks.com/snapshots/"

  // Build / Dist Config
  def rsyncRepo = "repobum_repobum@repobum:/home/public/releases"
}
