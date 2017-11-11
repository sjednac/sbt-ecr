package sbtecr

import sbt.Logger

import scala.language.postfixOps
import scala.sys.process._

private[sbtecr] object Commands {

  def exec(cmd: String)(implicit logger: Logger): Int = {
    logger.debug(s"Executing (2.12): ${cmd}")
    cmd!
  }

}
