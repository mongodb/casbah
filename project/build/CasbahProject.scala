import sbt._
import growl._ 
import com.github.olim7t.sbtscalariform._

class CasbahProject(info: ProjectInfo) 
    extends ParentProject(info) 
    with IdeaProject
    with posterous.Publish {

  /*import ScalaProject.CompoundDocOption */

  override def parallelExecution = true 

  override def managedStyle = ManagedStyle.Maven

  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/%s/".format( 
    if (projectVersion.value.toString.endsWith("-SNAPSHOT"))
      "snapshots"
    else
      "releases"
  )

  Credentials(Path.userHome / ".ivy2" / ".scalatools_credentials", log)



  lazy val commons = project("casbah-commons", "casbah-commons", new CasbahCommonsProject(_))
  lazy val core = project("casbah-core", "casbah-core", new CasbahCoreProject(_), commons, query)
  lazy val query = project("casbah-query", "casbah-query", new CasbahQueryProject(_), commons)
  lazy val gridfs = project("casbah-gridfs","casbah-gridfs", new CasbahGridFSProject(_), core)

  val allSource: PathFinder = commons.mainSourcePath ** "*.scala" +++
                              core.mainSourcePath ** "*.scala" +++
                              query.mainSourcePath ** "*.scala" +++
                              gridfs.mainSourcePath ** "*.scala" 


  abstract class CasbahBaseProject(info: ProjectInfo) 
      extends DefaultProject(info) 
      with AutoCompilerPlugins 
      with IdeaProject
      with GrowlingTests 
      with ScalariformPlugin {

    override def packageDocsJar = defaultJarPath("-javadoc.jar")
    override def packageSrcJar= defaultJarPath("-sources.jar")
    lazy val sourceArtifact = Artifact.sources(artifactID)
    lazy val docsArtifact = Artifact.javadoc(artifactID)
    override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageDocs, packageSrc)
    override def scalariformOptions = Seq(VerboseScalariform)

   /**
     * SXR Support 
     */
    //val sxr = compilerPlugin("org.scala-tools.sxr" % "sxr_2.8.0" % "0.2.6")

    override val growlResultFormatter = (res: GroupResult) =>
      GrowlResultFormat(
        Some(res.name),
        (res.status match {
         case Result.Error  => "[Casbah] Some Tests Had Errors: %s"
         case Result.Passed => "[Casbah] All Tests Passed:  %s"
         case Result.Failed => "[Casbah] Some Tests Failed: %s"
        }) format res.name, 
        "Tests %s, Failed %s, Errors %s, Skipped %s".format(
          res.count, res.failures, res.errors, res.skipped
        ),  
        res.status match {
          case Result.Error | Result.Failed => true
          case _ => false
        },
        res.status match {
          case Result.Error  => growlTestImages.error
          case Result.Passed => growlTestImages.pass
          case Result.Failed => growlTestImages.fail
        }
      )

    override def compileOptions =
      //CompileOption("-P:sxr:base-directory:" + mainScalaSourcePath) ::
      super.compileOptions ++ Seq(Unchecked, ExplainTypes, Deprecation)

    override def documentOptions = Seq(
      CompoundDocOption("-doc-source-url", "http://api.mongodb.org/scala/casbah-%s/%s/sxr/€{FILE_PATH}".format(projectVersion.value, projectName.value)),
      CompoundDocOption("-doc-version", "v%s".format(projectVersion.value)),
      CompoundDocOption("-doc-title", "Casbah %s".format(projectName.value))
    ) 
    // Testing Deps
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.8" % "test->default"


      
  }

  class CasbahCommonsProject(info: ProjectInfo) extends CasbahBaseProject(info) {
    // Runtime deps
    val mongodb = "org.mongodb" % "mongo-java-driver" % "2.5.3"
    val scalajCollection = "org.scalaj" %% "scalaj-collection" % "1.1"
    val slf4j = "org.slf4j" % "slf4j-api" % "1.6.0"
    // JCL bindings for testing only
    val slf4jJCL = "org.slf4j" % "slf4j-jcl" % "1.6.0" % "test"
    val scalaTime = "org.scala-tools.time" %% "time" % "0.4"
  }

  class CasbahCoreProject(info: ProjectInfo) extends CasbahBaseProject(info) { 
    lazy val allDocs = scaladocTask("Casbah Documentation", allSource, "scaladocBuild", docClasspath, documentOptions)
  }

  class CasbahQueryProject(info: ProjectInfo) extends CasbahBaseProject(info)

  class CasbahGridFSProject(info: ProjectInfo) extends CasbahBaseProject(info)

  // Repositories
  val scalaToolsRepo = "Scala Tools Release Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapRepo = "Scala Tools Snapshot Repository" at "http://scala-tools.org/repo-snapshots"
  val mavenOrgRepo = "Maven.Org Repository" at "http://repo1.maven.org/maven2/org/"
/*  val bumRepo = "Bum Networks Release Repository" at "http://repo.bumnetworks.com/releases/"
  val bumSnapsRepo = "Bum Networks Snapshots Repository" at "http://repo.bumnetworks.com/snapshots/"*/
}
