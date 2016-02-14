package akka.persistence.s3.snapshot

import akka.persistence.SnapshotMetadata
import org.scalatest.{ DiagrammedAssertions, WordSpecLike }

class SnapshotKeySpec extends WordSpecLike
    with DiagrammedAssertions with SnapshotKeySupport {

  val extension = "ss"

  "parseKeyToMetadata" must {
    "parse snapshot object key to SnapshotMetadata" in {
      assert(parseKeyToMetadata("snapshot-p-9/15-1455442923252.ss") == SnapshotMetadata("p-9", 15L, 1455442923252L))
    }
  }
}
