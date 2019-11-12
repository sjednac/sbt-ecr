package sbtecr

import com.amazonaws.regions.Region
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest
import sbt.Logger

private[sbtecr] object AwsSts extends Aws {

  def accountId(region: Region): String = {
    val request = new GetCallerIdentityRequest()
    val response = sts(region).getCallerIdentity(request)

    response.getAccount
  }

  private def sts(region: Region) = {
    AWSSecurityTokenServiceClientBuilder.standard()
                                        .withRegion(region.getName())
                                        .withCredentials(credentialsProvider())
                                        .build()
  }
}
