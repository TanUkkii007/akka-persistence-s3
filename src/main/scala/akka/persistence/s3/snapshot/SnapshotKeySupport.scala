package akka.persistence.s3.snapshot

import akka.persistence.SnapshotMetadata

trait SnapshotKeySupport {

  val extension: String

  lazy val Pattern = ("""^(.+)/(\d+)-(\d+)\.""" + extension + "$").r

  final def snapshotKey(metadata: SnapshotMetadata): String = {
    s"${metadata.persistenceId}/${metadata.sequenceNr.toString.reverse}-${metadata.timestamp}.$extension"
  }

  def parseKeyToMetadata(key: String): SnapshotMetadata = {
    key match {
      case Pattern(persistenceId: String, sequenceNr: String, timestamp: String) =>
        SnapshotMetadata(persistenceId, sequenceNr.reverse.toLong, timestamp.toLong)
    }
  }

  def prefixFromPersistenceId(persistenceId: String): String = persistenceId + "/"
}
