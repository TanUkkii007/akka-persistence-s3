package akka.persistence.s3
package snapshot

import java.io.ByteArrayInputStream
import akka.actor.ActorLogging
import akka.persistence.serialization.Snapshot
import akka.persistence.{ SelectedSnapshot, SnapshotMetadata, SnapshotSelectionCriteria }
import akka.persistence.snapshot.SnapshotStore
import akka.serialization.SerializationExtension
import com.amazonaws.services.s3.model.{ ObjectMetadata, S3ObjectInputStream, ListObjectsRequest }
import com.typesafe.config.Config
import scala.collection.JavaConversions._
import scala.collection.immutable
import scala.concurrent.Future
import scala.util.control.NonFatal

case class SerializationResult(stream: ByteArrayInputStream, size: Int)

class S3SnapshotStore(config: Config) extends SnapshotStore with ActorLogging with SnapshotKeySupport {
  import context.dispatcher

  val settings = new S3SnapshotConfig(config)

  val s3Client: S3Client = new S3Client {
    val s3ClientConfig = new S3ClientConfig(context.system.settings.config.getConfig("s3-client"))
  }

  private val serializationExtension = SerializationExtension(context.system)

  private val s3Dispatcher = context.system.dispatchers.lookup("s3-snapshot-store.s3-client-dispatcher")

  val extensionName = settings.extension

  override def loadAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] = {
    snapshotMetadatas(persistenceId, criteria)
      .map(_.sorted.takeRight(settings.maxLoadAttempts))
      .flatMap(load)
  }

  private def load(metadata: immutable.Seq[SnapshotMetadata]): Future[Option[SelectedSnapshot]] = metadata.lastOption match {
    case None => Future.successful(None)
    case Some(md) =>
      s3Client.getObject(settings.bucketName, snapshotKey(md))(s3Dispatcher)
        .map { obj =>
          val snapshot = deserialize(obj.getObjectContent)
          Some(SelectedSnapshot(md, snapshot.data))
        } recoverWith {
          case NonFatal(e) =>
            log.error(e, s"Error loading snapshot [${md}]")
            load(metadata.init) // try older snapshot
        }
  }

  override def saveAsync(metadata: SnapshotMetadata, snapshot: Any): Future[Unit] = {
    val serialized = serialize(Snapshot(snapshot))
    val objectMetadata = new ObjectMetadata()
    objectMetadata.setContentLength(serialized.size)
    s3Client.putObject(
      settings.bucketName,
      snapshotKey(metadata),
      serialized.stream,
      objectMetadata
    )(s3Dispatcher).map(_ => ())
  }

  override def deleteAsync(metadata: SnapshotMetadata): Future[Unit] = {
    if (metadata.timestamp == 0L)
      deleteAsync(metadata.persistenceId, SnapshotSelectionCriteria(metadata.sequenceNr, Long.MaxValue, metadata.sequenceNr, Long.MinValue))
    else
      s3Client.deleteObject(settings.bucketName, snapshotKey(metadata))(s3Dispatcher)
  }

  override def deleteAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Unit] = {
    val metadatas = snapshotMetadatas(persistenceId, criteria)
    metadatas.map(list => Future.sequence(list.map(deleteAsync)))
  }

  private def snapshotMetadatas(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[List[SnapshotMetadata]] = {
    s3Client.listObjects(
      new ListObjectsRequest()
        .withBucketName(settings.bucketName)
        .withPrefix(prefixFromPersistenceId(persistenceId))
        .withDelimiter("/")
    )(s3Dispatcher)
      .map(_.getObjectSummaries.toList.map(s => parseKeyToMetadata(s.getKey))
        .filter(m => m.sequenceNr >= criteria.minSequenceNr && m.sequenceNr <= criteria.maxSequenceNr && m.timestamp >= criteria.minTimestamp && m.timestamp <= criteria.maxTimestamp))

  }

  protected def deserialize(inputStream: S3ObjectInputStream): Snapshot =
    serializationExtension.deserialize(akka.persistence.serialization.streamToBytes(inputStream), classOf[Snapshot]).get

  protected def serialize(snapshot: Snapshot): SerializationResult = {
    val serialized = serializationExtension.findSerializerFor(snapshot).toBinary(snapshot)
    SerializationResult(new ByteArrayInputStream(serializationExtension.findSerializerFor(snapshot).toBinary(snapshot)), serialized.size)
  }
}
