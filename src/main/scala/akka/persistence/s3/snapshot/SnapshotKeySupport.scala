package akka.persistence.s3.snapshot

import akka.persistence.SnapshotMetadata

trait SnapshotKeySupport {

  val extensionName: String

  lazy val Pattern = ("""^(.+)/(\d+)-(\d+)\.""" + extensionName + "$").r

  final def snapshotKey(metadata: SnapshotMetadata): String = {
    s"${metadata.persistenceId}/${metadata.sequenceNr.toString.reverse}-${metadata.timestamp}.$extensionName"
  }

  def parseKeyToMetadata(key: String): SnapshotMetadata = {
    key match {
      case Pattern(persistenceId: String, sequenceNr: String, timestamp: String) =>
        SnapshotMetadata(persistenceId, sequenceNr.reverse.toLong, timestamp.toLong)
    }
  }

  def prefixFromPersistenceId(persistenceId: String): String = persistenceId + "/"
}
