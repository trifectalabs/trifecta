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
      "Websudos" at "https://dl.bintray.com/websudos/oss-releases/"
    )
  )

  lazy val root = DockerPlayProject("osprey-api")
    .settings(
      libraryDependencies ++= Seq(
        "kiambogo" %% "scrava" % "1.1.5",
        "com.typesafe.akka" %% "akka-actor" % "2.4-M3",
        "com.websudos" % "phantom-dsl_2.11" % "1.11.0",
        "com.websudos"  %% "util-parsers"                  % "0.9.11",
        "com.typesafe.akka" %% "akka-remote" % "2.4-M3",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "org.postgresql" % "postgresql" % "9.3-1100-jdbc41",
        "com.typesafe.play" %% "anorm" % "2.4.0",
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2",
        ws,
        cache
      ),
      playDefaultPort := 9001,
      routesImport += "com.trifectalabs.osprey.v0.Bindables._",
      routesGenerator := play.routes.compiler.InjectedRoutesGenerator
    )
}
