import sbt._

class CasbahProject(info: ProjectInfo) 
    extends ParentProject(info) 
    with posterous.Publish {


  // this was nice while it lasted
  override def parallelExecution = false

  override def managedStyle = ManagedStyle.Maven

  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/%s/".format( 
    if (projectVersion.value.toString.endsWith("-SNAPSHOT"))
      "snapshots"
    else
      "releases"
  )

  Credentials(Path.userHome / ".ivy2" / ".scalatools_credentials", log)

  lazy val commons = project("casbah-commons", "commons", new CasbahCommonsProject(_))
  lazy val core = project("casbah-core", "core", new CasbahCoreProject(_), commons, query)
  lazy val query = project("casbah-query", "query", new CasbahQueryProject(_), commons)
  lazy val gridfs = project("casbah-gridfs","gridfs", new CasbahGridFSProject(_), core)

  abstract class CasbahBaseProject(info: ProjectInfo) 
      extends DefaultProject(info) 
      with AutoCompilerPlugins {

    /**
     * SXR Support 
     */
    //val sxr = compilerPlugin("org.scala-tools.sxr" % "sxr_2.8.0" % "0.2.6")

    override def compileOptions =
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)
      //CompileOption("-P:sxr:base-directory:" + mainScalaSourcePath.absolutePath) ::

    override def documentOptions = Seq(
      CompoundDocOption("-doc-source-url", "http://api.mongodb.org/scala/casbah/source.sxr/") 
    ) ++ super.documentOptions 

    // Testing Deps
    val specs = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5" % "test->default"
    val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"


      
  }

  class CasbahCommonsProject(info: ProjectInfo) extends CasbahBaseProject(info) {
    // Runtime deps
    val mongodb = "org.mongodb" % "mongo-java-driver" % "2.3"
    val scalajCollection = "org.scalaj" % "scalaj-collection_2.8.0" % "1.0"
    val slf4j = "org.slf4j" % "slf4j-api" % "1.6.0"
  }

  class CasbahCoreProject(info: ProjectInfo) extends CasbahBaseProject(info) {
    // Runtime Deps
    val scalaTime = "org.scala-tools.time" % "time_2.8.0" % "0.2"
  }

  class CasbahQueryProject(info: ProjectInfo) extends CasbahBaseProject(info)

  class CasbahGridFSProject(info: ProjectInfo) extends CasbahBaseProject(info)

  // Repositories
  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
  val bumRepo = "Bum Networks Release Repository" at "http://repo.bumnetworks.com/releases/"
  val bumSnapsRepo = "Bum Networks Snapshots Repository" at "http://repo.bumnetworks.com/snapshots/"
}
