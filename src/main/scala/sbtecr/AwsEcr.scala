package sbtecr

import java.util.Base64

import com.amazonaws.regions.Region
import com.amazonaws.services.ecr.AmazonECRClientBuilder
import com.amazonaws.services.ecr.model._
import sbt.Logger

import scala.collection.JavaConverters._

private[sbtecr] object AwsEcr extends Aws {

  def domain(region: Region, accountId: String) = s"${accountId}.dkr.ecr.${region}.${region.getDomain}"

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

  def dockerCredentials(region: Region)(implicit logger: Logger): (String, String) = {
    val request = new GetAuthorizationTokenRequest()
    val response = ecr(region).getAuthorizationToken(request)

    response
      .getAuthorizationData
      .asScala
      .map(_.getAuthorizationToken)
      .map(Base64.getDecoder.decode(_))
      .map(new String(_, "UTF-8"))
      .map(_.split(":"))
      .headOption match {
        case Some(creds) if creds.size == 2 =>
          (creds(0), creds(1))
        case _ =>
          throw new IllegalStateException("Authorization token not found.")
      }
  }

  private def ecr(region: Region) = {
    AmazonECRClientBuilder.standard()
                          .withRegion(region.getName())
                          .withCredentials(credentialsProvider())
                          .build()
  }
}
