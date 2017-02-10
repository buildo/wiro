import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._

enablePlugins(GitVersioning)

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
  bintrayOrganization := Some("buildo"),
  organization := "io.buildo",
  licenses += ("MIT", url("https://github.com/buildo/wiro/blob/master/LICENSE")),
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  libraryDependencies := commonDependencies,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise"
)

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

lazy val examples = project
  .settings(commonSettings: _*)
  .dependsOn(serverAkkaHttp)

lazy val docs = project
  .enablePlugins(MicrositesPlugin)
  .settings(moduleName := "wiro-docs")
  .settings(ghpages.settings)
  .settings(docSettings)
  .settings(tutScalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))))
  .dependsOn(serverAkkaHttp, examples)

lazy val docSettings = Seq(
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  micrositeName := "wiro",
  micrositeDescription := "Lightweight RPC-Http Scala library",
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
