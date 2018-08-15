package sbtecr

import java.util.Base64

import com.amazonaws.regions.Region
import com.amazonaws.services.ecr.{AmazonECR, AmazonECRClientBuilder}
import com.amazonaws.services.ecr.model._
import sbt.Logger

import scala.collection.JavaConverters._

private[sbtecr] object AwsEcr {

  import Aws._

  def createRepository(region: Region, repositoryName: String)(implicit logger: Logger): Unit =
    try {
      val request = new CreateRepositoryRequest().withRepositoryName(repositoryName)

      val result = ecr(region).createRepository(request)
      logger.info(s"Repository created in $region: arn=${result.getRepository.getRepositoryArn}")
    } catch {
      case _: RepositoryAlreadyExistsException => logger.info(s"Repository exists: $region/$repositoryName")
    }

  def dockerCredentials(region: Region)(implicit logger: Logger): (String, String) = {
    val request  = new GetAuthorizationTokenRequest()
    val response = ecr(region).getAuthorizationToken(request)

    response.getAuthorizationData.asScala
      .map(_.getAuthorizationToken)
      .map(Base64.getDecoder.decode(_))
      .map(new String(_, "UTF-8"))
      .map(_.split(":"))
      .headOption match {
      case Some(Array(user, pass)) => user -> pass
      case _ =>
        throw new IllegalStateException("Authorization token not found.")
    }
  }

  private def ecr(region: Region): AmazonECR =
    AmazonECRClientBuilder
      .standard()
      .withRegion(region.getName)
      .withCredentials(credentialsProvider())
      .build()

}
