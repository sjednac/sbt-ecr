package sbtecr

import sbt._

import scala.language.postfixOps

private[sbtecr] object Commands {

  def exec(cmd: String)(implicit logger: Logger): Int = {
    logger.debug(s"Executing (2.10): ${cmd}")
    Process(cmd) !
  }

}
