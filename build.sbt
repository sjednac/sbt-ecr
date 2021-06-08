enablePlugins(SbtPlugin)

name          := "sbt-ecr"
organization  := "com.mintbeans"
description   := "sbt plugin for managing Amazon ECR repositories"
startYear     := Some(2016)
licenses      += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

//https://www.scala-sbt.org/1.x/docs/Cross-Build-Plugins.html
ThisBuild / crossScalaVersions := Seq("2.10.7", "2.12.10")

pluginCrossBuild / sbtVersion := {
  scalaBinaryVersion.value match {
    case "2.10" => "0.13.18"
    case "2.12" => "1.2.8"
  }
}

scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val amazonSdkV = "1.11.672"
  val scalaTestV = "3.0.8"
  val awsSsoSdkV = "2.16.63"
  Seq(
    "com.amazonaws"          %  "aws-java-sdk-sts" % amazonSdkV,
    "com.amazonaws"          %  "aws-java-sdk-ecr" % amazonSdkV,
    "software.amazon.awssdk" %  "sso"              % awsSsoSdkV,
    "org.scalatest"          %% "scalatest"        % scalaTestV % "test"
  )
}
