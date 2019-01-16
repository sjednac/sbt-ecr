# sbt-ecr

An [SBT](http://www.scala-sbt.org/) plugin for managing [Docker](http://docker.io) images within [Amazon ECR](https://aws.amazon.com/ecr/).

[ ![Download](https://api.bintray.com/packages/sbilinski/sbt-plugins/sbt-ecr/images/download.svg) ](https://bintray.com/sbilinski/sbt-plugins/sbt-ecr/_latestVersion)

## Features

* Create ECR repositories using `ecr:createRepository`
* Login to the remote registry using `ecr:login`
* Push local images using `ecr:push`

## Installation

Add the following to your `project/plugins.sbt` file:

    addSbtPlugin("com.mintbeans" % "sbt-ecr" % "0.14.1")

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
    
## Usage

The plugin [follows](https://github.com/sbilinski/sbt-ecr/blob/master/src/main/scala/sbtecr/Aws.scala) standard AWS conventions in terms of security and authentication. That is, you can use [environment variables](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/EnvironmentVariableCredentialsProvider.html), an [EC2 instance profile](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/EC2ContainerCredentialsProviderWrapper.html) or a "local" profile from `~/.aws/credentials` to authenticate your publishing process. 

To make it work **locally**, you may configure an AWS profile according to the [reference page](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html) and spawn the `push` process as such:

    AWS_DEFAULT_PROFILE="<your_profile_name>" sbt ecr:push

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

## Repository security policy configuration

By default, when the `createRepository` task is executed, the new repository does not have a **security policy**
attached. 

When you set `repositoryPolicyText` in your `build.sbt` file, and the `createRepository` is called, the created
repository will have the configured policy. 

Example usage:
    
    repositoryPolicyText in Ecr := Some(IO.read(file("project") / "ecrpolicy.json")) 
    
Then in the `project/ecrpolicy.json` you can set your policy text. For example:
    
    {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Sid": "BuildServerAccess",
          "Effect": "Allow",
          "Principal": {
            "AWS": [
              "arn:aws:iam::YOUR_ACCOUNT_ID_HERE:role/YOUR_IAM_ROLE_NAME_HERE"
            ]
          },
          "Action": [
            "ecr:*"
          ]
        }
      ]
    }
 
Configuring `repositoryPolicyText` will not affect existing repositories.

## Repository lifecycle policy configuration

Configuring the repository lifecycle policy works the same as configuring the policy in the previous chapter.

By default, when the `createRepository` task is executed, the new repository does not have a **lifecycle 
policy** attached. 

When you set `repositoryLifecyclePolicyText` in your `build.sbt` file, and the `createRepository` is called, the created
repository will have the configured lifecycle policy. 

Example usage:
    
    repositoryLifecyclePolicyText in Ecr := Some(IO.read(file("project") / "ecrlifecyclepolicy.json")) 
    
Then in the `project/ecrlifecyclepolicy.json` you can set your policy text. For example:
    
    {
      "rules": [
        {
          "rulePriority": 10,
          "description": "Lifecycle of release branch images",
          "selection": {
            "tagStatus": "tagged",
            "tagPrefixList": [
              "release"
            ],
            "countType": "imageCountMoreThan",
            "countNumber": 20
          },
          "action": {
            "type": "expire"
          }
        }
      ]
    }
 
Configuring `repositoryLifecyclePolicyText` will not affect existing repositories.


