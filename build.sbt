sbtPlugin     := true
name          := "sbt-ecr"
organization  := "com.mintbeans"
description   := "sbt plugin for managing Amazon ECR repositories"
startYear     := Some(2016)
licenses      += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

sbtVersion in Global := "1.0.3"
crossSbtVersions     := List("0.13.16", "1.1.4")
scalaVersion         := "2.12.6"
scalacOptions        := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

scalaCompilerBridgeSource := {
  val sv = appConfiguration.value.provider.id.version
  ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
}

libraryDependencies ++= {
  val amazonSdkV = "1.11.418"
  val scalaTestV = "3.0.5"
  Seq(
    "com.amazonaws"  %  "aws-java-sdk-sts"   % amazonSdkV,
    "com.amazonaws"  %  "aws-java-sdk-ecr"   % amazonSdkV,
    "org.scalatest"  %% "scalatest"      % scalaTestV % "test"
  )
}
