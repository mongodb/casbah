import sbt._
class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val posterousRepo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
  val repo_with_snuggletex = "ph.ed.ac.uk" at "http://www2.ph.ed.ac.uk/maven2"
  val sbtIdeaRepo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

  val lessRepo = "lessis repo" at "http://repo.lessis.me"
  val growl = "me.lessis" % "sbt-growl-plugin" % "0.0.5"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.6"
  val sbtIdea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.4.0"
  val formatter = "com.github.olim7t" % "sbt-scalariform" % "1.0.2"

}

// vim: set ts=2 sw=2 sts=2 et:
