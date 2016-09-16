import sbt._
import Keys._

object CondorBuild extends Build {
  val TrifectaNexusBaseURL = "[removed]"
  val TrifectaNexusSnapshotsURL = TrifectaNexusBaseURL + "snapshots"
  val TrifectaNexusReleasesURL = TrifectaNexusBaseURL + "releases"

  lazy val root = Project("condor-sbt", file("."))
    .settings(
      name := "condor-sbt",
      version := IO.read(file(".").getParentFile() / "VERSION").stripLineEnd,
      organization := "com.trifectalabs",
      scalaVersion := "2.10.4",
      sbtPlugin := true,

      libraryDependencies <++= (sbtBinaryVersion in update, scalaBinaryVersion) { (sbtV, scalaV) =>
        Seq(
          Defaults.sbtPluginExtra("com.typesafe.sbt" % "sbt-native-packager" % "1.0.4", sbtV, scalaV),
          Defaults.sbtPluginExtra("com.typesafe.play" % "sbt-plugin" % "2.4.0", sbtV, scalaV))
      },

      credentials += Credentials(file("sbt_credentials")),

      publishTo <<= version { v: String =>
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at TrifectaNexusSnapshotsURL)
        else
          Some("releases" at TrifectaNexusReleasesURL)
      }
    )
}
