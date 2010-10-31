import sbt._

class CasbahProject(info: ProjectInfo) extends ParentProject(info) with posterous.Publish {

  // this was nice while it lasted
  override def parallelExecution = false

  // we want to publish POMs
  override def managedStyle = ManagedStyle.Maven

  lazy val commons = project("casbah-commons", "casbah-commons", new CasbahCommonsProject(_))
  lazy val core = project("casbah-core", "casbah-core", new CasbahCoreProject(_), commons, query)
  lazy val query = project("casbah-query", "casbah-query", new CasbahQueryProject(_), commons)
  lazy val gridfs = project("casbah-gridfs", "casbah-gridfs", new CasbahGridFSProject(_), core)

  abstract class CasbahBaseProject(info: ProjectInfo) extends DefaultProject(info) {
    override def compileOptions =
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
    // Testing Deps
    val specs = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5" % "test->default"
    val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"

    val publishTo = Resolver.sftp("repobum", "repobum", "/home/public/%s".format(
      if (projectVersion.value.toString.endsWith("-SNAPSHOT")) "snapshots"
      else "releases"
    )) as("repobum_repobum", new java.io.File(Path.userHome + "/.ssh/id_rsa"))
      
  }

  class CasbahCommonsProject(info: ProjectInfo) extends CasbahBaseProject(info) {
    // Runtime deps
    val mongodb = "org.mongodb" % "mongo-java-driver" % "2.1"
    val configgy = "net.lag" % "configgy" % "2.0.0" intransitive()
    val scalajCollection = "org.scalaj" % "scalaj-collection_2.8.0" % "1.0"
  }

  class CasbahCoreProject(info: ProjectInfo) extends CasbahBaseProject(info) {
    // Runtime Deps
    val scalaTime = "org.scala-tools.time" % "time_2.8.0" % "0.2"
  }

  class CasbahQueryProject(info: ProjectInfo) extends CasbahBaseProject(info)

  class CasbahGridFSProject(info: ProjectInfo) extends CasbahBaseProject(info)

  class CasbahMapperProject(info: ProjectInfo) extends CasbahBaseProject(info) {
    val objenesis = "org.objenesis" % "objenesis" % "1.2"
    val commonsLang = "commons-lang" % "commons-lang" % "2.5" % "test->default"
  }

  // Repositories
  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
  val bumRepo = "Bum Networks Release Repository" at "http://repo.bumnetworks.com/releases/"
  val bumSnapsRepo = "Bum Networks Snapshots Repository" at "http://repo.bumnetworks.com/snapshots/"
}
