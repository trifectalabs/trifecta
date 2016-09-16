resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "Trifecta releases" at "http://nexus.trifecta.io/content/repositories/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

addSbtPlugin("com.trifectalabs" % "condor-sbt" % "0.0.1")
