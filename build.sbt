sbtPlugin     := true
name          := "sbt-ecr"
organization  := "com.mintbeans"
description   := "sbt plugin for managing Amazon ECR repositories"
startYear     := Some(2016)
version       := "0.1.0"
scalaVersion  := "2.10.6"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val amazonSdkV = "1.11.33"
  val scalaTestV = "3.0.0"
  Seq(
    "com.amazonaws"  %  "aws-java-sdk"   % amazonSdkV,
    "org.scalatest"  %% "scalatest"      % scalaTestV % "test"
  )
}
