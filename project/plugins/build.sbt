resolvers += "less is" at "http://repo.lessis.me"

libraryDependencies ++= Seq(
  "me.lessis" %% "sbt-growl-plugin" % "0.1.0",
  "net.databinder" %% "posterous-sbt" % "0.2.0"
)

resolvers += {
  val typesafeRepoUrl = new java.net.URL("http://repo.typesafe.com/typesafe/releases")
  val pattern = Patterns(false, "[organisation]/[module]/[sbtversion]/[revision]/[type]s/[module](-[classifier])-[revision].[ext]")
  Resolver.url("Typesafe Repository", typesafeRepoUrl)(pattern)
}

libraryDependencies <<= (libraryDependencies, sbtVersion) { (deps, version) => 
  deps :+ ("com.typesafe.sbteclipse" %% "sbteclipse" % "1.2" extra("sbtversion" -> version))
}
