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
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
    )
  )

  lazy val root = DockerPlayProject("social-weaver-api")
    .settings(
      libraryDependencies ++= Seq(
        "com.trifectalabs" %% "myriad" % "0.0.5",
        "com.google.api-client" % "google-api-client" % "1.19.1",
        "com.google.apis" % "google-api-services-calendar" % "v3-rev168-1.21.0",
        "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2",
        ws
      ),
      playDefaultPort := 9003,
      routesImport += "com.trifectalabs.social.weaver.v0.Bindables._",
      routesImport += "com.trifectalabs.raven.v0.Bindables._",
      routesImport += "com.trifectalabs.osprey.v0.Bindables._",
      routesGenerator := play.routes.compiler.InjectedRoutesGenerator
    )
}

