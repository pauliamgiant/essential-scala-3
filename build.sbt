import sbtwelcome.*

lazy val root = project
  .in(file("."))
  .enablePlugins(MdocPlugin)
  .settings(
    logo := List(
      "",
      "Essential Scala 3 (v" + version.value + ")",
      ""
    ).mkString("\n"),
    usefulTasks := Seq(
      UsefulTask("clean", "run clean").alias("cln"),
      UsefulTask("mdoc", "run mdoc").alias("md"),
      UsefulTask("reload", "run reload").alias("rl"),
      UsefulTask("compile", "run compile").alias("c"),
      UsefulTask("test", "Run test").alias("t"),
      UsefulTask("scalafixAll", "Run scalafixAll on the entire project")
        .alias("fix"),
      UsefulTask("scalafmtAll", "Run scalafmtAll on the entire project")
        .alias("fmt"),
      UsefulTask("pdf", "Build the PDF version of the book").alias("p"),
      UsefulTask("html", "Build the HTML version of the book").alias("h"),
      UsefulTask("epub", "Build the ePub version of the book").alias("e"),
      UsefulTask("json", "Build the JSON version of the book").alias("j"),
      UsefulTask("all", "Build all versions of the book").alias("a")
    ),
    logoColor := scala.Console.MAGENTA,
    aliasColor := scala.Console.BLUE,
    commandColor := scala.Console.CYAN,
    descriptionColor := scala.Console.WHITE
  )

mdocIn := sourceDirectory.value / "pages"

mdocOut := target.value / "pages"

ThisBuild / scalaVersion := "3.7.0"

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature"
)

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

libraryDependencies ++= Seq()

import scala.sys.process._

lazy val pdf = taskKey[Unit]("Build the PDF version of the book")
lazy val html = taskKey[Unit]("Build the HTML version of the book")
lazy val epub = taskKey[Unit]("Build the ePub version of the book")
lazy val json = taskKey[Unit]("Build the JSON version of the book")
lazy val all = taskKey[Unit]("Build all versions of the book")

pdf := Def.sequential(mdoc.toTask(""), Def.task { "grunt pdf".! }).value
html := Def.sequential(mdoc.toTask(""), Def.task { "grunt html".! }).value
epub := Def.sequential(mdoc.toTask(""), Def.task { "grunt epub".! }).value
json := Def.sequential(mdoc.toTask(""), Def.task { "grunt json".! }).value

all := Def
  .sequential(
    mdoc.toTask(""),
    Def.task { "grunt pdf".! },
    Def.task { "grunt html".! },
    Def.task { "grunt epub".! }
  )
  .value
