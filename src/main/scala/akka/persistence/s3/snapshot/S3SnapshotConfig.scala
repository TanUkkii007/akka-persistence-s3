package akka.persistence.s3
package snapshot

import com.typesafe.config.Config

class S3SnapshotConfig(config: Config) {
  val bucketName = config getString "bucket-name"
  val extension = config getString "extension"
  val maxLoadAttempts = config getInt "max-load-attempts"
}
