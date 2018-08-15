# sbt-ecr

An [SBT](http://www.scala-sbt.org/) plugin for managing [Docker](http://docker.io) images within [Amazon ECR](https://aws.amazon.com/ecr/).

[ ![Download](https://api.bintray.com/packages/sbilinski/sbt-plugins/sbt-ecr/images/download.svg) ](https://bintray.com/sbilinski/sbt-plugins/sbt-ecr/_latestVersion)

## Features

Enable the use of the [sbt-native-packager DockerPlugin](https://www.scala-sbt.org/sbt-native-packager/formats/docker.html) with [Amazon ECR](https://aws.amazon.com/ecr/).

Prerequisites
-------------

The plugin assumes that [sbt-native-packager](https://github.com/sbt/sbt-native-packager) has been included in your SBT build configuration.    
This can be done by adding the plugin following instructions at http://www.scala-sbt.org/sbt-native-packager/ or by adding
another plugin that includes and initializes it (e.g. the SBT plugin for Play 2.6.x).

## Installation

Add the following to your `project/plugins.sbt` file:

    addSbtPlugin("com.mintbeans" % "sbt-ecr" % "0.11.0")

Add `sbt-ecr` settings to your `build.sbt`:   

    import com.amazonaws.regions.Regions
    
    enablePlugins(EcrPlugin)

    Ecr / region := Regions.US_EAST_1
    
That's all ! :tada:

:warning: This plugins will set the `Docker / dockerRepository` value for you, so you **SHOULD NOT** set it in your `build.sbt`.

Now you can use the normal workflow of the [sbt-native-packager DockerPlugin](https://www.scala-sbt.org/sbt-native-packager/formats/docker.html).