package sbtecr

import com.amazonaws.regions.Region
import sbt.Keys._
import sbt._
import sbtecr.Commands._

import scala.language.postfixOps

object EcrPlugin extends AutoPlugin {

  object autoImport {
      lazy val Ecr = config("ecr")

      lazy val region               = settingKey[Region]("Amazon EC2 region.")
      lazy val repositoryName       = settingKey[String]("Amazon ECR repository name.")
      lazy val repositoryPolicyText = settingKey[Option[String]]("Amazon ECR policy.")
      lazy val localDockerImage     = settingKey[String]("Local Docker image.")
      lazy val repositoryTags       = settingKey[Seq[String]]("Tags managed in the Amazon ECR repository.")

      lazy val repositoryDomain     = taskKey[String]("Domain of the Amazon ECR repository.")
      lazy val createRepository     = taskKey[Unit]("Create a repository in Amazon ECR.")
      lazy val login                = taskKey[Unit]("Login to Amazon ECR.")
      lazy val push                 = taskKey[Unit]("Push a Docker image to Amazon ECR.")
  }

  import autoImport._
  override lazy val projectSettings = inConfig(Ecr)(defaultSettings ++ tasks)

  lazy val defaultSettings: Seq[Def.Setting[_]] = Seq(
    repositoryTags := List("latest"),
    repositoryPolicyText := None,
    localDockerImage := s"${repositoryName.value}:${version.value}"
  )

  lazy val tasks: Seq[Def.Setting[_]] = Seq(
    repositoryDomain := {
      implicit val logger = streams.value.log
      val accountId = AwsSts.accountId(region.value)
      AwsEcr.domain(region.value, accountId)
    },
    createRepository := {
      implicit val logger = streams.value.log
      AwsEcr.createRepository(region.value, repositoryName.value, repositoryPolicyText.value)
    },
    login := {
      implicit val logger = streams.value.log
      val (user, pass) = AwsEcr.dockerCredentials(region.value)
      val cmd = s"docker login -u ${user} -p ${pass} https://${repositoryDomain.value}"
      exec(cmd) match {
        case 0 =>
        case _ =>
          sys.error(s"Login failed. Command: $cmd")
      }
    },
    push := {
      implicit val logger = streams.value.log

      val src = localDockerImage.value
      def destination(tag: String) = s"${repositoryDomain.value}/${repositoryName.value}:$tag"

      repositoryTags.value.foreach { tag =>
        val dst = destination(tag)
        exec(s"docker tag ${src} ${dst}") match {
          case 0 =>
            exec(s"docker push ${dst}") match {
              case 0 =>
              case _ =>
                sys.error(s"Pushing failed. Target image: ${dst}")
            }
          case _ =>
            sys.error(s"Tagging failed. Source image: ${src} Target image: ${dst}")
        }
      }
    }
  )
}
