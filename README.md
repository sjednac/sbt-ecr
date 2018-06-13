# sbt-ecr

An [SBT](http://www.scala-sbt.org/) plugin for managing [Docker](http://docker.io) images within [Amazon ECR](https://aws.amazon.com/ecr/).

[ ![Download](https://api.bintray.com/packages/sbilinski/sbt-plugins/sbt-ecr/images/download.svg) ](https://bintray.com/sbilinski/sbt-plugins/sbt-ecr/_latestVersion)

## Features

* Create ECR repositories using `ecr:createRepository`
* Login to the remote registry using `ecr:login`
* Push local images using `ecr:push`

## Installation

Add the following to your `project/plugins.sbt` file:

    addSbtPlugin("com.mintbeans" % "sbt-ecr" % "0.10.0")

Add ECR settings to your `build.sbt`. The following snippet assumes a Docker image build using [sbt-native-packager](https://github.com/sbt/sbt-native-packager):

    import com.amazonaws.regions.{Region, Regions}
    
    enablePlugins(EcrPlugin)

    region           in Ecr := Region.getRegion(Regions.US_EAST_1)
    repositoryName   in Ecr := (packageName in Docker).value
    localDockerImage in Ecr := (packageName in Docker).value + ":" + (version in Docker).value

    // Create the repository before authentication takes place (optional)
    login in Ecr := ((login in Ecr) dependsOn (createRepository in Ecr)).value

    // Authenticate and publish a local Docker image before pushing to ECR
    push in Ecr := ((push in Ecr) dependsOn (publishLocal in Docker, login in Ecr)).value

## Tagging

By default, the produced image will be tagged as "latest". It is possible to provide arbitrary additional tags,
 for example to add the version tag to the image:
    
    repositoryTags in Ecr ++= Seq(version.value)
    
If you don't want latest tag on your image you could override the ```repositoryTags``` value completely:
 
    repositoryTags in Ecr := Seq(version.value)

If you want to make the tag environment-dependent you can use the following template:

    repositoryTags in Ecr := sys.env.get("VERSION_TAG").map(Seq(_)).getOrElse(Seq("latest"))

And trigger the process using:

    VERSION_TAG=myfeature sbt ecr:push

