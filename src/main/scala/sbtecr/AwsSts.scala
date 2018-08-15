package sbtecr

import com.amazonaws.regions.Region
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest

private[sbtecr] object AwsSts {

  import Aws._

  def accountId(region: Region): String = {
    val request = new GetCallerIdentityRequest()

    AWSSecurityTokenServiceClientBuilder
      .standard()
      .withRegion(region.getName)
      .withCredentials(credentialsProvider())
      .build()
      .getCallerIdentity(request)
      .getAccount
  }

}
