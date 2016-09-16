package com.trifectalabs.condor.sbt

import sbt._
import Keys._
import play.PlayScala
import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager._

trait TrifectaBuild extends Build { 
  val TrifectaNexusBaseURL = "[removed]"
  val TrifectaNexusSnapshotsURL = TrifectaNexusBaseURL + "snapshots"
  val TrifectaNexusReleasesURL = TrifectaNexusBaseURL + "releases"
  val TrifectaMavenReleases = "trifecta-releases-maven" at TrifectaNexusReleasesURL

  val trifectaDockerRegistry = settingKey[String]("Trifecta private docker registry hostname")
  val dockerRepoName = settingKey[String]("Name of docker repository")

  def projectSettings: Seq[Project.Setting[_]] = Seq()
  
  def commonSettings: Seq[Project.Setting[_]] = Seq(
    version := IO.read(file(".").getParentFile() / "VERSION").stripLineEnd,
    organization := "com.trifectalabs",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    fork in run := true,
    resolvers ++= Seq(
      TrifectaMavenReleases
    )
  )

  def commonDockerSettings: Seq[Project.Setting[_]] = Seq(
    trifectaDockerRegistry := "[removed]",
    dockerRepoName := "trifecta/" + name.value,

    dockerRepository := Some(trifectaDockerRegistry.value),
    packageName in Docker := packageName.value,
    version in Docker := version.value
  )

  def readVersion: String =
    IO.read(file(".").getParentFile() / "VERSION").stripLineEnd

  credentials += Credentials("Sonatype Nexus Repository Manager",
    TrifectaNexusBaseURL, "[removed]", "[removed]")

  publishTo <<= version { v: String =>
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at TrifectaNexusSnapshotsURL)
    else
      Some("releases" at TrifectaNexusReleasesURL)
  }

  def DockerPlayProject(name: String) =
    Project(name, file(name))
      .enablePlugins(PlayScala)
      .enablePlugins(DockerPlugin)
      .settings(commonSettings:_*)
      .settings(commonDockerSettings:_*)
      .settings(projectSettings:_*)
}
