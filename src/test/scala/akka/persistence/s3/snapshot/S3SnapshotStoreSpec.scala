package akka.persistence.s3.snapshot

import akka.persistence.s3.{ S3ClientConfig, S3Client }
import akka.persistence.snapshot.SnapshotStoreSpec
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.concurrent.Await

class S3SnapshotStoreSpec extends SnapshotStoreSpec(ConfigFactory.parseString(
  """
    |akka.persistence.snapshot-store.plugin = "s3-snapshot-store"
    |s3-client{
    |  aws-access-key-id = "test"
    |  aws-secret-access-key = "test"
    |  region = "us-west-2"
    |  endpoint = "http://localhost:4567"
    |  options {
    |    path-style-access = true
    |  }
    |}
  """.stripMargin
).withFallback(ConfigFactory.load())) {

  override def beforeAll() = {
    import system.dispatcher
    val s3Client = new S3Client {
      override val s3ClientConfig: S3ClientConfig = new S3ClientConfig(system.settings.config.getConfig("s3-client"))
    }
    Await.result(s3Client.createBucket("snapshot"), 5 seconds)
    println("bucket snapshot created")
    super.beforeAll()
  }
}
