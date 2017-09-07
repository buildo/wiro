import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._

enablePlugins(GitVersioning)

val autowire = "com.lihaoyi" %% "autowire" % "0.2.6"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.6"
val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.16.0"
val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % "10.0.3" % "test"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"

val circeVersion = "0.8.0"

val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val commonDependencies = Seq(
  autowire,
  akkaHttp,
  akkaHttpCirce,
  akkaHttpTestKit,
  scalaTest
) ++ circeDependencies

lazy val commonSettings = Seq(
  bintrayOrganization := Some("buildo"),
  organization := "io.buildo",
  licenses += ("MIT", url("https://github.com/buildo/wiro/blob/master/LICENSE")),
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  libraryDependencies :=
    commonDependencies :+
    scalaOrganization.value % "scala-reflect" % scalaVersion.value % "provided",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  releaseCrossBuild := true
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
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

lazy val examples = project
  .settings(commonSettings: _*)
  .dependsOn(serverAkkaHttp)
  .dependsOn(clientAkkaHttp)

lazy val docs = project
  .enablePlugins(MicrositesPlugin)
  .settings(moduleName := "wiro-docs")
  .settings(ghpages.settings)
  .settings(docSettings)
  .settings(tutScalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))))
  .settings(libraryDependencies ++= Seq(scalaTest, akkaHttpTestKit, akkaHttpCirce))
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
