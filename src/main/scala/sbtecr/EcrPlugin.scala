package sbtecr

import com.amazonaws.regions.{Region, Regions}
import com.typesafe.sbt.packager.docker.DockerPlugin
import sbt.Keys._
import sbt.internal.util.ManagedLogger
import sbt.{Def, _}

import scala.language.postfixOps
import scala.sys.process._

object EcrPlugin extends AutoPlugin {

  object autoImport {
    lazy val Ecr = config("ecr")

    lazy val region = settingKey[Regions]("Amazon EC2 region.")
  }
  import autoImport._

  override def requires: Plugins = DockerPlugin

  import DockerPlugin.autoImport._

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    inConfig(Docker) {
      Seq(
        dockerRepository := {
          val regionV   = getRegion.value
          val accountId = AwsSts.accountId(getRegion.value)

          Some(s"$accountId.dkr.ecr.$regionV.${regionV.getDomain}")
        },
        publish := ((Docker / publish) dependsOn (createRepository, login)).value
      )
    }

  private lazy val createRepository: Def.Initialize[Task[Unit]] = Def.task {
    implicit val logger: ManagedLogger = streams.value.log
    AwsEcr.createRepository(getRegion.value, (Docker / name).value)
  }

  private lazy val login: Def.Initialize[Task[Unit]] = Def.task {
    implicit val logger: ManagedLogger = streams.value.log

    val (user, pass) = AwsEcr.dockerCredentials(getRegion.value)
    val cmd          = s"docker login -u $user -p $pass https://${(Docker / dockerRepository).value.get}"

    cmd ! logger match {
      case 0 => ()
      case _ => sys.error(s"AWS ECR login failed. Command: $cmd")
    }
  }

  private lazy val getRegion: Def.Initialize[Region] = Def.setting { Region.getRegion((Ecr / region).value) }

}
