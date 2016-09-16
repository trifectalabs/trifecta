import sbt._
import Keys._
import play.Play.autoImport._
import play.PlayImport.PlayKeys._
import play.sbt.routes.RoutesKeys._
import sbt.{ Build => SbtBuild }
import com.trifectalabs.condor.sbt.TrifectaBuild

object Build extends SbtBuild with TrifectaBuild {
  override def projectSettings = Seq(
    version := readVersion,
    resolvers ++= Seq(
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      "Trifecta releases" at "http://nexus.trifecta.io/content/repositories/releases/"
    )
  )

  lazy val root = DockerPlayProject("peacock-server")
    .settings(
      libraryDependencies ++= Seq(
        "com.mohiva" %% "play-silhouette" % "3.0.6",
        "net.codingwell" %% "scala-guice" % "4.0.0",
        "com.mohiva" %% "play-silhouette-testkit" % "3.0.0" % "test",
        "net.ceedubs" %% "ficus" % "1.1.2",
        "org.scalaj" %% "scalaj-http" % "2.2.1",
        jdbc,
        cache,
        ws
      )
    )
}
