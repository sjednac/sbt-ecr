package sbtecr

import com.amazonaws.AmazonWebServiceClient
import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Region

private[sbtecr] trait Aws {

  def client[T <: AmazonWebServiceClient](clientClass: Class[T], region: Region): T = {
    region.createClient(clientClass, credentialsProvider(), null)
  }

  private def credentialsProvider(): AWSCredentialsProvider =
    new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new SystemPropertiesCredentialsProvider(),
      new ProfileCredentialsProvider(sys.env.getOrElse("AWS_DEFAULT_PROFILE", "default")),
      new InstanceProfileCredentialsProvider()
    )
}
