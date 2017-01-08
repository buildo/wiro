val autowire = "com.lihaoyi" %% "autowire" % "0.2.6"
val upickle = "com.lihaoyi" %% "upickle" % "0.4.3"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.1"
val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.11.0"

val scalaReflect = "org.scala-lang" % "scala-reflect" % "2.11.8"
val scalaMeta = "org.scalameta" %% "scalameta" % "1.4.0"

val circeVersion = "0.6.1"

val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val commonDependencies = Seq(
  autowire,
  upickle,
  akkaHttp,
  akkaHttpCirce,
  scalaReflect,
  scalaMeta
) ++ circeDependencies

lazy val commonSettings = Seq(
  organization := "io.buildo",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  libraryDependencies := commonDependencies,
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M5" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise"
)

lazy val core = (project in file("core")).
  settings(commonSettings: _*)

lazy val serverAkkaHttp = (project in file("server-akka-http")).
  settings(commonSettings: _*).
  dependsOn(core)

lazy val clientAkkaHttp = (project in file("client-akka-http")).
  settings(commonSettings: _*).
  dependsOn(core)

lazy val examples = (project in file("examples")).
  settings(commonSettings: _*).
  dependsOn(serverAkkaHttp, clientAkkaHttp)
