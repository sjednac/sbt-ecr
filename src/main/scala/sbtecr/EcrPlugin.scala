package sbtecr

import com.amazonaws.regions.Region
import sbt.Keys._
import sbt._
import sbtecr.Commands._

import scala.language.postfixOps

object EcrPlugin extends AutoPlugin {

  object autoImport {
      lazy val Ecr = config("ecr")

      lazy val region                         = settingKey[Region]("Amazon EC2 region.")
      lazy val registryIds                    = settingKey[Seq[String]]("AWS account IDs that correspond to the Amazon ECR registries.")
      lazy val repositoryName                 = settingKey[String]("Amazon ECR repository name.")
      lazy val repositoryDomain               = settingKey[Option[String]]("Overwrites Amazon ECR domain.")
      lazy val repositoryPolicyText           = settingKey[Option[String]]("Amazon ECR access policy.")
      lazy val repositoryLifecyclePolicyText  = settingKey[Option[String]]("Amazon ECR lifecycle policy.")
      lazy val localDockerImage               = settingKey[String]("Local Docker image.")
      lazy val repositoryTags                 = settingKey[Seq[String]]("Tags managed in the Amazon ECR repository.")
      lazy val imageTagsMutable               = settingKey[Boolean]("Boolean as to whether to make image tags mutable or not")
      lazy val scanOnPush                     = settingKey[Boolean]("Enable vunerabilities scan on image push")

      lazy val fetchDomain                    = taskKey[String]("Fetch active domain for Amazon ECR access.")
      lazy val createRepository               = taskKey[Unit]("Create a repository in Amazon ECR.")
      lazy val login                          = taskKey[Unit]("Login to Amazon ECR.")
      lazy val push                           = taskKey[Unit]("Push a Docker image to Amazon ECR.")
  }

  import autoImport._
  override lazy val projectSettings = inConfig(Ecr)(defaultSettings ++ tasks)

  lazy val defaultSettings: Seq[Def.Setting[_]] = Seq(
    repositoryTags := List("latest"),
    registryIds := Nil,
    repositoryPolicyText := None,
    repositoryLifecyclePolicyText := None,
    localDockerImage := s"${repositoryName.value}:${version.value}",
    repositoryDomain := None,
    imageTagsMutable := true,
    scanOnPush       :=true,
  )

  lazy val tasks: Seq[Def.Setting[_]] = Seq(
    fetchDomain := {
      implicit val logger = streams.value.log

      val domain = repositoryDomain.value.getOrElse {
        val accountId = AwsSts.accountId(region.value)
        AwsEcr.domain(region.value, accountId)
      }

      logger.info(s"ECR domain: ${domain}")
      domain
    },
    createRepository := {
      implicit val logger = streams.value.log
      AwsEcr.createRepository(
        region.value, repositoryName.value,
        imageTagsMutable.value,
        scanOnPush.value,
        repositoryPolicyText.value,
        repositoryLifecyclePolicyText.value
      )
    },
    login := {
      implicit val logger = streams.value.log
      val (user, pass) = AwsEcr.dockerCredentials(region.value, registryIds.value)
      val domain = fetchDomain.value
      val cmd = s"docker login -u ${user} -p ${pass} https://${domain}"
      exec(cmd) match {
        case 0 =>
        case _ =>
          sys.error(s"Login failed. Command: $cmd")
      }
    },
    push := {
      implicit val logger = streams.value.log

      val src = localDockerImage.value
      val domain = fetchDomain.value

      def destination(tag: String) = s"${domain}/${repositoryName.value}:$tag"

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
