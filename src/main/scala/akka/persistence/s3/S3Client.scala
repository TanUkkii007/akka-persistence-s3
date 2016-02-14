package akka.persistence.s3

import java.io.InputStream

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.{ S3ClientOptions, AmazonS3Client }
import com.amazonaws.services.s3.model._

import scala.concurrent.{ Future, ExecutionContext }

trait S3Client {
  val s3ClientConfig: S3ClientConfig

  lazy val client: AmazonS3Client = {
    val client = new AmazonS3Client(new BasicAWSCredentials(s3ClientConfig.awsKey, s3ClientConfig.awsSecret))
    s3ClientConfig.endpoint.foreach { endpoint =>
      client.withEndpoint(endpoint)
      ()
    }
    client.setS3ClientOptions(new S3ClientOptions()
      .withPathStyleAccess(s3ClientConfig.options.pathStyleAccess)
      .withChunkedEncodingDisabled(s3ClientConfig.options.chunkedEncodingDisabled))
    client
  }

  def createBucket(bucketName: String)(implicit ec: ExecutionContext): Future[Bucket] = Future {
    client.createBucket(bucketName)
  }

  def putObject(bucketName: String, key: String, input: InputStream, metadata: ObjectMetadata)(implicit ec: ExecutionContext): Future[PutObjectResult] = Future {
    client.putObject(new PutObjectRequest(bucketName, key, input, metadata))
  }

  def getObject(bucketName: String, key: String)(implicit ec: ExecutionContext): Future[S3Object] = Future {
    client.getObject(new GetObjectRequest(bucketName, key))
  }

  def listObjects(request: ListObjectsRequest)(implicit ec: ExecutionContext): Future[ObjectListing] = Future {
    client.listObjects(request)
  }

  def deleteObject(bucketName: String, key: String)(implicit ec: ExecutionContext): Future[Unit] = Future {
    client.deleteObject(bucketName, key)
  }
}