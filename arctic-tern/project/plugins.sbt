resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Trifecta Repos
resolvers += "Trifecta snapshots" at "http://nexus.trifecta.io/content/repositories/snapshots/"

resolvers += "Trifecta releases" at "http://nexus.trifecta.io/content/repositories/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0-M4")

addSbtPlugin("com.trifectalabs" % "condor-sbt" % "0.0.1")

