# sbt-ecr 

An [SBT](http://www.scala-sbt.org/) plugin for managing [Docker](http://docker.io) images within [Amazon ECR](https://aws.amazon.com/ecr/).

## Features

* Create ECR repositories using `ecr:createRepository`
* Login to the remote registry using `ecr:login`
* Push local images using `ecr:push`

## Installation

Add the following to your `project/plugins.sbt` file:

    addSbtPlugin("com.mintbeans" % "sbt-ecr" % "0.3.0")

Add ECR settings to your `build.sbt`. The following snippet assumes a Docker image build using [sbt-native-packager](https://github.com/sbt/sbt-native-packager):

    import com.amazonaws.regions.{Region, Regions}

    region           in ecr := Region.getRegion(Regions.US_EAST_1)
    repositoryName   in ecr := (packageName in Docker).value
    localDockerImage in ecr := (packageName in Docker).value + ":" + (version in Docker).value

    //Run `docker:publishLocal` before `ecr:push`
    push in ecr <<= (push in ecr) dependsOn (publishLocal in Docker)
