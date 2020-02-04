enablePlugins(GitVersioning)
import microsites._

val autowire = "com.lihaoyi" %% "autowire" % "0.2.6"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.11"
val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.5.26"
val akkaStreams = "com.typesafe.akka" %% "akka-stream" % "2.5.26"
val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.30.0"
val akkaHttpTestKitBase = "com.typesafe.akka" %% "akka-http-testkit" % "10.1.11"
val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % "2.5.26" % "test"
val scalaTestBase = "org.scalatest" %% "scalatest" % "3.0.1"
val akkaHttpTestKit = akkaHttpTestKitBase % "test"
val scalaTest = scalaTestBase % "test"
val cats = "org.typelevel" %% "cats-core" % "1.1.0"

val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.10.1"

val circeVersion = "0.12.2"

val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

val loggingBackendDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
)

val commonDependencies = Seq(
  scalaLogging,
  autowire,
  akkaHttp,
  akkaActor,
  akkaStreams,
  akkaHttpCirce,
  akkaHttpTestKit,
  akkaTestKit,
  scalaTest,
  pureConfig,
  cats
) ++ circeDependencies ++ loggingBackendDependencies

lazy val commonSettings = Seq(
  bintrayOrganization := Some("buildo"),
  organization := "io.buildo",
  licenses += ("MIT", url("https://github.com/buildo/wiro/blob/master/LICENSE")),
  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.12.2"),
  libraryDependencies :=
    commonDependencies :+
    scalaOrganization.value % "scala-reflect" % scalaVersion.value % "provided",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  scalacOptions ++= Seq(
    "-Xplugin-require:macroparadise",
    "-encoding", "utf8",
    "-deprecation", "-feature", "-unchecked", "-Xlint",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Xfuture",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-Yrangepos"
  ),
  releaseCrossBuild := true,
  releaseIgnoreUntrackedFiles := true
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(core, serverAkkaHttp, clientAkkaHttp)
  .dependsOn(core, serverAkkaHttp, clientAkkaHttp)

lazy val core = project
  .settings(commonSettings: _*)
  .settings(
    name := "wiro-core",
    bintrayPackageLabels := Seq("buildo", "wiro", "wiro-core")
  )

lazy val serverAkkaHttp = project
  .settings(commonSettings: _*)
  .settings(
    name := "wiro-http-server",
    bintrayPackageLabels := Seq("buildo", "wiro", "wiro-http-server")
  )
  .dependsOn(core)

lazy val clientAkkaHttp = project
  .settings(commonSettings: _*)
  .settings(
    name := "wiro-http-client",
    bintrayPackageLabels := Seq("buildo", "wiro", "wiro-http-client")
  )
  .dependsOn(core)
  .dependsOn(serverAkkaHttp % "test->test")

lazy val examples = project
  .settings(commonSettings: _*)
  .dependsOn(serverAkkaHttp)
  .dependsOn(clientAkkaHttp)

lazy val docs = project
  .enablePlugins(MicrositesPlugin)
  .settings(moduleName := "wiro-docs")
  .settings(docSettings)
  .settings(scalacOptions in Tut ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))))
  .settings(libraryDependencies ++= Seq(scalaTestBase, akkaHttpTestKitBase, akkaHttpCirce))
  .dependsOn(serverAkkaHttp, examples)

lazy val docSettings = Seq(
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  micrositeName := "wiro",
  micrositeDescription := "A Scala library for writing HTTP routes",
  micrositeHighlightTheme := "atom-one-light",
  micrositeHomepage := "http://buildo.github.io/wiro/",
  micrositeBaseUrl := "/wiro",
  micrositeGithubOwner := "buildo",
  micrositeGithubRepo := "wiro",
  autoAPIMappings := false,
  fork in tut := true,
  git.remoteRepo := "git@github.com:buildo/wiro.git",
  ghpagesNoJekyll := false,
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
)
