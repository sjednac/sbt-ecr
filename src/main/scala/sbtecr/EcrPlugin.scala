package sbtecr

import com.amazonaws.regions.Region
import sbt.Keys._
import sbt._

import scala.language.postfixOps

object EcrPlugin extends AutoPlugin {

  object autoImport {
      lazy val ecr = config("ecr")

      lazy val region           = settingKey[Region]("Amazon EC2 region.")
      lazy val repositoryName   = settingKey[String]("Amazon ECR repository name.")
      lazy val localDockerImage = settingKey[String]("Local Docker image.")

      lazy val createRepository = taskKey[Unit]("Create a repository in Amazon ECR.")
      lazy val login            = taskKey[Unit]("Login to Amazon ECR.")
      lazy val push             = taskKey[Unit]("Push a Docker image to Amazon ECR.")
  }

  import autoImport._
  override lazy val projectSettings = inConfig(ecr)(defaultSettings ++ tasks)

  lazy val defaultSettings: Seq[Def.Setting[_]] = Seq(
    localDockerImage := s"${repositoryName.value}:latest"
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
      val dst = s"${Ecr.domain(region.value, accountId)}/${repositoryName.value}"

      val tag = List("docker", "tag", src, dst)
      Process(tag)! match {
        case 0 =>
          val push = List("docker", "push", dst)
          Process(push)! match {
            case 0 =>
            case _ =>
              sys.error(s"Pushing failed. Command: ${push.mkString(" ")}")
          }
        case _ =>
          sys.error(s"Tagging failed. Command: ${tag.mkString(" ")}")
      }
    }
  )
}
