package sbtecr

import java.io.File

import com.amazonaws.AmazonWebServiceClient
import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.profile.internal.{AllProfiles, BasicProfileConfigLoader}
import com.amazonaws.profile.path.AwsProfileFileLocationProvider
import com.amazonaws.regions.Region
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import sbt.Logger


private[sbtecr] trait Aws {

  private val profile = sys.env.getOrElse("AWS_DEFAULT_PROFILE", "default")

  def profileRole: Option[String] = {
    val file: File = AwsProfileFileLocationProvider.DEFAULT_CONFIG_LOCATION_PROVIDER.getLocation
    val allProfiles: AllProfiles = BasicProfileConfigLoader.INSTANCE.loadProfiles(file)
    Option(allProfiles.getProfile(profile).getRoleArn)
  }

  def client[T <: AmazonWebServiceClient](clientClass: Class[T], region: Region)(implicit logger: Logger): T = {
    if (profileRole.isDefined) {
      assumedRoleClient(clientClass, region, profileRole.get)
    } else {
      region.createClient(clientClass, credentialsProvider(), null)
    }
  }

  private[this] def assumedRoleClient[T <: AmazonWebServiceClient](clientClass: Class[T], region: Region, awsRole: String)(implicit logger: Logger): T = {

    logger.info(s"Getting credentials for role: $awsRole")

    val assumeRequest: AssumeRoleRequest = new AssumeRoleRequest()
      .withRoleArn(awsRole)
      .withDurationSeconds(900)
      .withRoleSessionName("sbt-ecr")

    val stsClient = new AWSSecurityTokenServiceClient(credentialsProvider())
    val assumeRoleResult = stsClient.assumeRole(assumeRequest)

    region.createClient(clientClass, new AWSCredentialsProvider {
      override def refresh(): Unit = {}

      override def getCredentials: AWSCredentials = {
        val credentials = assumeRoleResult.getCredentials
        logger.debug(s"Got credentials: $credentials")
        new BasicSessionCredentials(credentials.getAccessKeyId, credentials.getSecretAccessKey, credentials.getSessionToken)
      }
    }, null)
  }

  private def credentialsProvider(): AWSCredentialsProvider =
    new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new SystemPropertiesCredentialsProvider(),
      new ProfileCredentialsProvider(profile),
      new InstanceProfileCredentialsProvider()
    )
}
