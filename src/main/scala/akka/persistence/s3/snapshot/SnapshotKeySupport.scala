package akka.persistence.s3.snapshot

import akka.persistence.SnapshotMetadata

trait SnapshotKeySupport {

  val extension: String

  lazy val Pattern = ("""^snapshot-(.+)/(\d+)-(\d+)\.""" + extension + "$").r

  final def snapshotKey(metadata: SnapshotMetadata): String = {
    s"snapshot-${metadata.persistenceId}/${metadata.sequenceNr}-${metadata.timestamp}.$extension"
  }

  def parseKeyToMetadata(key: String): SnapshotMetadata = {
    key match {
      case Pattern(persistenceId: String, sequenceNr: String, timestamp: String) =>
        SnapshotMetadata(persistenceId, sequenceNr.toLong, timestamp.toLong)
    }
  }
}
