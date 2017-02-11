## Getting Started

Wiro is published to [Bintray][https://bintray.com/buildo/maven/wiro-http-server] and cross-built for Scala 2.11, and Scala 2.12.

How to add dependency:

```scala
libraryDependencies += "io.buildo" %% "wiro-http-server" % "0.1.2"
```

Wiro uses scala macro annotations.  You'll also need to include the [Macro Paradise][http://docs.scala-lang.org/overviews/macros/paradise.html] compiler plugin in your build:

```scala
addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full
)
```
