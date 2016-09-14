bintrayOrganization  := Some("sbilinski")
bintrayRepository    := "maven"
bintrayVcsUrl        := Some("git@github.com:sbilinski/sbt-ecr.git")

publishMavenStyle       := false
publishArtifact in Test := false

publish <<= publish dependsOn (test in Test)
