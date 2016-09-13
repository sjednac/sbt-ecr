package sbtecr

import com.amazonaws.regions.Region
import sbt.Keys._
import sbt._

import scala.language.postfixOps

object EcrPlugin extends AutoPlugin {

  object autoImport {
      lazy val ecr = config("ecr")

      lazy val accountId        = settingKey[String]("Amazon account ID.")
      lazy val region           = settingKey[Region]("Amazon EC2 region.")
      lazy val repositoryName   = settingKey[String]("Amazon ECR repository name.")

      lazy val createRepository = taskKey[Unit]("Create a repository in Amazon ECR.")
      lazy val login            = taskKey[Unit]("Login to Amazon ECR.")
  }

  override def trigger = allRequirements

  import autoImport._
  override lazy val projectSettings = inConfig(ecr)(defaultSettings ++ tasks)

  lazy val defaultSettings: Seq[Def.Setting[_]] = Seq()

  lazy val tasks: Seq[Def.Setting[_]] = Seq(
    createRepository := {
      implicit val logger = streams.value.log
      Ecr.createRepository(region.value, repositoryName.value)
    },
    login := {
      implicit val logger = streams.value.log
      val (user, pass) = Ecr.dockerCredentials(region.value)
      val cmd = List("docker", "login", "-u", user, "-p", pass, "-e", "none", s"https://${accountId.value}.dkr.ecr.${region.value}.amazonaws.com")
      Process(cmd)!
    }
  )
}
