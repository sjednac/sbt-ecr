bintrayOrganization  := Some("sbilinski")
bintrayRepository    := "sbt-plugins"
bintrayVcsUrl        := Some("git@github.com:sbilinski/sbt-ecr.git")

publishMavenStyle       := false
publishArtifact in Test := false

// Release
import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("^ test"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("^ publish"),
  setNextVersion,
  commitNextVersion
)
