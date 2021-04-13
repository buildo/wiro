## Getting Started

Wiro is published on [Bintray](https://bintray.com/buildo/maven/wiro-http-server) and cross-built for Scala 2.11, and Scala 2.12.

How to add dependency:

```scala
libraryDependencies ++= Seq(
  "io.buildo" %% "wiro-http-server" % "X.X.X",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)
```


Wiro uses scala-logging, so you need to include an SLF4J backend. We include slf4j-nop to disable logging, but you can replace this with the logging framework you prefer (log4j2, logback).
