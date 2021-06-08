bintrayOrganization  := Some("sbilinski")
bintrayRepository    := "sbt-plugins"
bintrayVcsUrl        := Some("git@github.com:sjednac/sbt-ecr.git")

publishMavenStyle      := false
Test / publishArtifact := false

// Release
import ReleaseTransformations._

releaseCrossBuild := true
releaseVersionBump := sbtrelease.Version.Bump.Minor

