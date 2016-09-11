package sbtecr

import com.amazonaws.regions.Region
import com.amazonaws.services.ecr.AmazonECRClient
import com.amazonaws.services.ecr.model._
import sbt.Logger

object Ecr extends Aws {

  def createRepository(region: Region, repositoryName: String)(implicit logger: Logger): Unit = {
    val request = new CreateRepositoryRequest()
    request.setRepositoryName(repositoryName)

    try {
      val result = ecr(region).createRepository(request)
      logger.info(s"Repository created in ${region}: arn=${result.getRepository.getRepositoryArn}")
    } catch {
      case e: RepositoryAlreadyExistsException =>
        logger.info(s"Repository exists: ${region}/${repositoryName}")
    }
  }

  private def ecr(region: Region) = client(classOf[AmazonECRClient], region)
}
