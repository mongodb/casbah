import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Publish {

  lazy val settings = Seq(
    crossPaths := false,
    pomExtra := casbahPomExtra,
    publishTo <<= sonatypePublishTo,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    pomIncludeRepository := { x => false },
    publishMavenStyle := true,
    publishArtifact in Test := false
  )

  def sonatypePublishTo: Initialize[Option[Resolver]] = {
    version { v: String =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  }

  def casbahPomExtra = {
    (<url>http://github.com/mongodb/casbah</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:mongodb/casbah.git</url>
      <connection>scm:git:git@github.com:mongodb/casbah.git</connection>
    </scm>
    <developers>
      <developer>
           <id>ross</id>
           <name>Ross Lawley</name>
           <url>http://rosslawley.co.uk</url>
      </developer>
      <developer>
          <id>brendan</id>
          <name>Brendan W. McAdams</name>
          <url>http://blog.evilmonkeylabs.net</url>
      </developer>
    </developers>)
  }

}