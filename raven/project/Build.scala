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

  lazy val root = DockerPlayProject("raven-api")
    .settings(
      libraryDependencies ++= Seq(
        "com.trifectalabs" %% "myriad" % "0.0.4",
        "com.websudos" % "phantom-dsl_2.11" % "1.11.0",
        "com.websudos"  %% "util-parsers" % "0.9.11",
        "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2",
        ws
      ),
      playDefaultPort := 9002,
      routesImport += "com.trifectalabs.raven.v0.Bindables._",
      routesImport += "com.trifectalabs.osprey.v0.Bindables._",
      routesGenerator := play.routes.compiler.InjectedRoutesGenerator
    )
}
