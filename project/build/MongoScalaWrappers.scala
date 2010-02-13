import sbt._

class MongoScalaWrappersProject(info: ProjectInfo) extends DefaultProject(info) {
  override def compileOptions = super.compileOptions ++ Seq(Unchecked)
}