sbtPlugin     := true
name          := "sbt-ecr"
organization  := "com.mintbeans"
description   := "sbt plugin for managing Amazon ECR repositories"
startYear     := Some(2016)
licenses      += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

crossSbtVersions     := List("0.13.17", "1.1.6")
scalaVersion         := "2.12.6"
scalacOptions        := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.6" % "provided")

libraryDependencies ++= {
  val amazonSdkV = "1.11.313"
  val scalaTestV = "3.0.0"
  Seq(
    "com.amazonaws"  %  "aws-java-sdk-sts"   % amazonSdkV,
    "com.amazonaws"  %  "aws-java-sdk-ecr"   % amazonSdkV,
    "org.scalatest"  %% "scalatest"      % scalaTestV % "test"
  )
}
