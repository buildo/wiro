addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.2")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

addSbtPlugin("com.47deg"  % "sbt-microsites" % "0.7.15")

addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.5.6")

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.2")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.7")
