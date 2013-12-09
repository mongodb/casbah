import sbt.File
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.scalastyle._

object StyleChecker {
  val maxResult = 100

  class CustomTextOutput[T <: FileSpec]() extends Output[T] {
    private val messageHelper = new MessageHelper(this.getClass().getClassLoader())

    var fileCount: Int = _
    override def message(m: Message[T]): Unit = m match {
      case StartWork() =>
      case EndWork() =>
      case StartFile(file) =>
        print("Checking file " + file + "...")
        fileCount = 0
      case EndFile(file) =>
        if (fileCount == 0) println(" OK!")
      case StyleError(file, clazz, key, level, args, line, column, customMessage) =>
        report(line, column, messageHelper.text(level.name),
               Output.findMessage(messageHelper, clazz, key, args, customMessage))
      case StyleException(file, clazz, message, stacktrace, line, column) =>
        report(line, column, "error", message)
    }

    private def report(line: Option[Int], column: Option[Int], level: String, message: String) {
      if (fileCount == 0) println("")
      fileCount += 1
      println("  " + fileCount + ". " + level + pos(line, column) + ":")
      println("     " + message)
    }

    private def pos(line: Option[Int], column: Option[Int]): String = line match {
      case Some(line) => " at line " + line + (column match {
        case Some(column) => " character " + column
        case None => ""
      })
      case None => ""
    }
  }

  def score(outputResult: OutputResult) = {
    val penalties = outputResult.errors + outputResult.warnings
    scala.math.max(maxResult - penalties, 0)
  }

  def assess(sources: Seq[File]): (String, Int) = {

    val configFile = new File("./project/scalastyle-config.xml").getAbsolutePath

    val messages = new ScalastyleChecker().checkFiles(
      ScalastyleConfiguration.readFromXml(configFile),
      Directory.getFiles(None, sources))

    val output = new ByteArrayOutputStream()
    val outputResult = Console.withOut(new PrintStream(output)) {
      new CustomTextOutput().output(messages)
    }

    val msg =
      output.toString +
      "Processed " + outputResult.files + " file(s)\n" +
      "Found " + outputResult.errors + " errors\n" +
      "Found " + outputResult.warnings + " warnings\n" +
      (if (outputResult.errors+outputResult.warnings > 0) "Consult the scala style guide at http://www.scalastyle.org/" else "")

    (msg, score(outputResult))
  }
}
