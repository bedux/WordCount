name := "WordCound"

version := "0.1"

scalaVersion := "2.13.8"

libraryDependencies += "co.fs2" %% "fs2-core" % "3.2.4"
libraryDependencies += "co.fs2" %% "fs2-io" % "3.2.4"

libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.3"

val circeVersion = "0.14.1"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "com.github.blemale" %% "scaffeine" % "5.1.2"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "0.20.0-M4"
libraryDependencies += "org.http4s" %% "http4s-blaze-server" % "0.23.7"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "log4cats-core"    % "2.1.1",
  "org.typelevel" %% "log4cats-slf4j"   % "2.1.1"
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime


libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.10"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test"
libraryDependencies += "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test
libraryDependencies += "org.scalamock" %% "scalamock" % "5.1.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % Test