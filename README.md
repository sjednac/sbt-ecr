# sbt-ecr 

An [SBT](http://www.scala-sbt.org/) plugin for managing [Docker](http://docker.io) images within [Amazon ECR](https://aws.amazon.com/ecr/).

## Features

* Create ECR repositories using `ecr:createRepository`
* Login to the remote registry using `ecr:login`
* Push local images using `ecr:push`

## Installation

Add the following to your `project/plugins.sbt` file:

    addSbtPlugin("com.mintbeans" % "sbt-ecr" % "0.4.0")

Add ECR settings to your `build.sbt`. The following snippet assumes a Docker image build using [sbt-native-packager](https://github.com/sbt/sbt-native-packager):

    import com.amazonaws.regions.{Region, Regions}
    
    enablePlugins(EcrPlugin)

    region           in ecr := Region.getRegion(Regions.US_EAST_1)
    repositoryName   in ecr := (packageName in Docker).value
    localDockerImage in ecr := (packageName in Docker).value + ":" + (version in Docker).value

    // Create the repository before authentication takes place (optional)
    login in ecr <<= (login in ecr) dependsOn (createRepository in ecr)

    // Authenticate and publish a local Docker image before pushing to ECR
    push in ecr <<= (push in ecr) dependsOn (publishLocal in Docker, login in ecr)

