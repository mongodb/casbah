class Plugins(info: sbt.ProjectInfo) extends sbt.PluginDefinition(info) {
  val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
  val rsyncSBT = "com.codahale" % "rsync-sbt" % "0.1.0"
  val posterousRepo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.6"
}

// vim: set ts=2 sw=2 sts=2 et:
