val autowire = "com.lihaoyi" %% "autowire" % "0.2.6"
val upickle = "com.lihaoyi" %% "upickle" % "0.4.3"
val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % "10.0.1"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.1"
val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.11.0"

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
  akkaHttpCore,
  akkaHttpCirce
) ++ circeDependencies

lazy val commonSettings = Seq(
  organization := "io.buildo",
  version := "0.1.0",
  scalaVersion := "2.11.8"
)

lazy val core = (project in file("core")).
  settings(commonSettings: _*)

lazy val serverAkkaHttp = (project in file("server-akka-http")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies := commonDependencies
  ).
  dependsOn(core)

lazy val clientAkkaHttp = (project in file("client-akka-http")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies := commonDependencies
  ).
  dependsOn(core)

lazy val examples = (project in file("examples")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies := commonDependencies
  ).
  dependsOn(serverAkkaHttp, clientAkkaHttp)
