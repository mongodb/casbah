class Plugins(info: sbt.ProjectInfo) extends sbt.PluginDefinition(info) {
  val posterousRepo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
  val repo_with_snuggletex = "ph.ed.ac.uk" at "http://www2.ph.ed.ac.uk/maven2"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.6"

  val lessRepo = "lessis repo" at "http://repo.lessis.me"
  val growl = "me.lessis" % "sbt-growl-plugin" % "0.0.5"
}

// vim: set ts=2 sw=2 sts=2 et:
