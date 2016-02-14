package akka.persistence.s3

import com.amazonaws.regions.{ Regions, Region }
import com.typesafe.config.Config

private object AWSRegionNames {
  val GovCloud = Regions.GovCloud.getName
  val US_EAST_1 = Regions.US_EAST_1.getName
  val US_WEST_1 = Regions.US_WEST_1.getName
  val US_WEST_2 = Regions.US_WEST_2.getName
  val EU_WEST_1 = Regions.EU_WEST_1.getName
  val EU_CENTRAL_1 = Regions.EU_CENTRAL_1.getName
  val AP_SOUTHEAST_1 = Regions.AP_SOUTHEAST_1.getName
  val AP_SOUTHEAST_2 = Regions.AP_SOUTHEAST_2.getName
  val AP_NORTHEAST_1 = Regions.AP_NORTHEAST_1.getName
  val AP_NORTHEAST_2 = Regions.AP_NORTHEAST_2.getName
  val SA_EAST_1 = Regions.SA_EAST_1.getName
  val CN_NORTH_1 = Regions.CN_NORTH_1.getName
}

class S3ClientConfig(config: Config) {
  import AWSRegionNames._
  val awsKey = config getString "aws-access-key-id"
  val awsSecret = config getString "aws-secret-access-key"
  val region: Region = config getString "region" match {
    case GovCloud       => Region.getRegion(Regions.GovCloud)
    case US_EAST_1      => Region.getRegion(Regions.US_EAST_1)
    case US_WEST_1      => Region.getRegion(Regions.US_WEST_1)
    case US_WEST_2      => Region.getRegion(Regions.US_WEST_2)
    case EU_WEST_1      => Region.getRegion(Regions.EU_WEST_1)
    case EU_CENTRAL_1   => Region.getRegion(Regions.EU_CENTRAL_1)
    case AP_SOUTHEAST_1 => Region.getRegion(Regions.AP_SOUTHEAST_1)
    case AP_SOUTHEAST_2 => Region.getRegion(Regions.AP_SOUTHEAST_2)
    case AP_NORTHEAST_1 => Region.getRegion(Regions.AP_NORTHEAST_1)
    case AP_NORTHEAST_2 => Region.getRegion(Regions.AP_NORTHEAST_2)
    case SA_EAST_1      => Region.getRegion(Regions.SA_EAST_1)
    case CN_NORTH_1     => Region.getRegion(Regions.CN_NORTH_1)
  }
  val endpoint: Option[String] = {
    val e = config getString "endpoint"
    if (e == "default") None else Some(e)
  }
  object options {
    val pathStyleAccess = config getBoolean "options.path-style-access"
    val chunkedEncodingDisabled = config getBoolean "options.chunked-encoding-disabled"
  }
}
