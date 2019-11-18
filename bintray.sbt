bintrayOrganization  := Some("sbilinski")
bintrayRepository    := "sbt-plugins"
bintrayVcsUrl        := Some("git@github.com:sbilinski/sbt-ecr.git")

publishMavenStyle       := false
publishArtifact in Test := false

// Release
import ReleaseTransformations._

releaseCrossBuild := true
releaseVersionBump := sbtrelease.Version.Bump.Minor

