tutSettings

val autowire = "com.lihaoyi" %% "autowire" % "0.2.6"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.1"
val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.11.0"

val scalaReflect = "org.scala-lang" % "scala-reflect" % "2.11.8"

val circeVersion = "0.7.0"

val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val commonDependencies = Seq(
  autowire,
  akkaHttp,
  akkaHttpCirce,
  scalaReflect
) ++ circeDependencies

lazy val commonSettings = Seq(
  organization := "io.buildo",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  libraryDependencies := commonDependencies,
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M5" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise"
)

lazy val core = project
  .settings(commonSettings: _*)

lazy val serverAkkaHttp = project
  .settings(commonSettings: _*)
  .dependsOn(core)

lazy val examples = project
  .settings(commonSettings: _*)
  .dependsOn(serverAkkaHttp)

