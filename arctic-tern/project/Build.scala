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
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
    )
  )

  lazy val root = DockerPlayProject("arctic-tern")
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "anorm" % "2.4.0",
        "org.postgresql" %  "postgresql"  % "9.4-1201-jdbc41",
        ws,
        jdbc 
      ),
      playDefaultPort := 9005
    )
}
