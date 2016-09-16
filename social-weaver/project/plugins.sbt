// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Sonatype snapshots repository
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

// Trifecta Repos
resolvers += "Trifecta snapshots" at "http://nexus.trifecta.io/content/repositories/snapshots/"

resolvers += "Trifecta releases" at "http://nexus.trifecta.io/content/repositories/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0-M4")

addSbtPlugin("com.trifectalabs" % "condor-sbt" % "0.0.1")

