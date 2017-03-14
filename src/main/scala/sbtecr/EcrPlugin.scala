package sbtecr

import com.amazonaws.regions.Region
import sbt.Keys._
import sbt.Keys.version
import sbt._

import scala.language.postfixOps

object EcrPlugin extends AutoPlugin {

  object autoImport {
      lazy val ecr = config("ecr")

      lazy val region           = settingKey[Region]("Amazon EC2 region.")
      lazy val repositoryName   = settingKey[String]("Amazon ECR repository name.")
      lazy val localDockerImage = settingKey[String]("Local Docker image.")
      lazy val additionalTags   = settingKey[Seq[String]]("Additional tags")

      lazy val createRepository = taskKey[Unit]("Create a repository in Amazon ECR.")
      lazy val login            = taskKey[Unit]("Login to Amazon ECR.")
      lazy val push             = taskKey[Unit]("Push a Docker image to Amazon ECR.")
  }

  import autoImport._
  override lazy val projectSettings = inConfig(ecr)(defaultSettings ++ tasks)

  lazy val defaultSettings: Seq[Def.Setting[_]] = Seq(
    version := "latest",
    additionalTags := Seq(),
    localDockerImage := s"${repositoryName.value}:${version.value}"
  )

  lazy val tasks: Seq[Def.Setting[_]] = Seq(
    createRepository := {
      implicit val logger = streams.value.log
      Ecr.createRepository(region.value, repositoryName.value)
    },
    login := {
      implicit val logger = streams.value.log
      val accountId = Sts.accountId(region.value)
      val (user, pass) = Ecr.dockerCredentials(region.value)
      val cmd = List("docker", "login", "-u", user, "-p", pass, "-e", "none", s"https://${Ecr.domain(region.value, accountId)}")
      Process(cmd)! match {
        case 0 =>
        case _ =>
          sys.error(s"Login failed. Command: ${cmd.mkString(" ")}")
      }
    },
    push := {
      implicit val logger = streams.value.log

      val accountId = Sts.accountId(region.value)

      val src = localDockerImage.value
      def destination(tag: String) = s"${Ecr.domain(region.value, accountId)}/${repositoryName.value}:$tag"

      val tags = Seq(version.value) ++ additionalTags.value

      tags.foreach { tag =>
        val dst = destination(tag)
        val command = List("docker", "tag", src, dst)
        Process(command) ! match {
          case 0 =>
            val push = List("docker", "push", dst)
            Process(push) ! match {
              case 0 =>
              case _ =>
                sys.error(s"Pushing failed. Command: ${push.mkString(" ")}")
            }
          case _ =>
            sys.error(s"Tagging failed. Command: ${command.mkString(" ")}")
        }
      }
    }
  )
}
